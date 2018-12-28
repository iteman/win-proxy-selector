package jp.co.fusions.win_proxy_selector.win;

import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import jp.co.fusions.win_proxy_selector.ProxySelectorFactory;
import jp.co.fusions.win_proxy_selector.selector.misc.ListProxySelector;
import jp.co.fusions.win_proxy_selector.selector.misc.ProtocolDispatchSelector;
import jp.co.fusions.win_proxy_selector.util.Logger;
import jp.co.fusions.win_proxy_selector.util.Logger.LogLevel;
import jp.co.fusions.win_proxy_selector.util.ProxyUtil;
import jp.co.fusions.win_proxy_selector.win.jna.WinDef.DWORD;
import jp.co.fusions.win_proxy_selector.win.jna.WinHttp;
import jp.co.fusions.win_proxy_selector.win.jna.WinHttpCurrentUserIEProxyConfig;
import jp.co.fusions.win_proxy_selector.win.jna.WinHttpHelpers;
import jp.co.fusions.win_proxy_selector.win.jna.WinHttpProxyInfo;


/*****************************************************************************
 * Provides a ProxySelector which extracts the proxy settings for
 * Microsoft Internet Explorer.
 * The settings are read by invoking native Windows API methods.
 *
 * @author Kei Sugimoto, Copyright 2018
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class WinProxySelectorFactory implements ProxySelectorFactory {

	/*************************************************************************
	 * getProxySelector
	 *
	 * @see ProxySelectorFactory#getProxySelector()
	 ************************************************************************/

	@Override
	public ProxySelector getProxySelector() {

		Logger.log(getClass(), LogLevel.TRACE, "Detecting Windows proxy settings");


		List<ProxySelector> selectors = new ArrayList<>();

		IEProxyConfig ieProxyConfig = readIEProxyConfig();
		if (ieProxyConfig == null) {
			Logger.log(getClass(), LogLevel.TRACE, "ieProxyConfig is null.");
		} else {
			addIfNotNull(selectors, createAutoDetectableProxySelectors(ieProxyConfig));
			addIfNotNull(selectors, createAutoConfigProxySelectors(ieProxyConfig));
			addIfNotNull(selectors, createFixedProxySelector(ieProxyConfig));
		}

		WinHttpProxyConfig winHttpProxyConfig = readWinHttpProxyConfig();
		if (winHttpProxyConfig == null) {
			Logger.log(getClass(), LogLevel.TRACE, "winHttpProxyConfig is null.");
		} else {
			addIfNotNull(selectors, createWinHttpProxySelector(winHttpProxyConfig));
		}

		return new ListProxySelector(selectors, ProxySelector.getDefault());
	}

	private void addIfNotNull(List<ProxySelector> l, ProxySelector selector) {
		if (selector == null) return;
		l.add(selector);
	}

	/*************************************************************************
	 * Loads the settings from the windows registry.
	 *
	 * @return WinIESettings containing all proxy settings.
	 ************************************************************************/

	private IEProxyConfig readIEProxyConfig() {

		// Retrieve the IE proxy configuration.
		WinHttpCurrentUserIEProxyConfig winHttpCurrentUserIeProxyConfig = new WinHttpCurrentUserIEProxyConfig();
		boolean successful = WinHttp.INSTANCE.WinHttpGetIEProxyConfigForCurrentUser(winHttpCurrentUserIeProxyConfig);
		if (!successful) {
			return null;
		}

		// Create IEProxyConfig instance
		return new IEProxyConfig(winHttpCurrentUserIeProxyConfig.fAutoDetect,
			winHttpCurrentUserIeProxyConfig.lpszAutoConfigUrl != null
				? winHttpCurrentUserIeProxyConfig.lpszAutoConfigUrl.getValue() : null,
			winHttpCurrentUserIeProxyConfig.lpszProxy != null ? winHttpCurrentUserIeProxyConfig.lpszProxy.getValue()
				: null,
			winHttpCurrentUserIeProxyConfig.lpszProxyBypass != null
				? winHttpCurrentUserIeProxyConfig.lpszProxyBypass.getValue() : null);

	}

	private WinHttpProxyConfig readWinHttpProxyConfig() {

		// Retrieve the WinHttp proxy configuration.
		WinHttpProxyInfo winHttpProxyInfo = new WinHttpProxyInfo();
		boolean successful = WinHttp.INSTANCE.WinHttpGetDefaultProxyConfiguration(winHttpProxyInfo);
		if (!successful) {
			return null;
		}

		// Create WinProxyConfig instance
		return new WinHttpProxyConfig(
			winHttpProxyInfo.dwAccessType != null ? winHttpProxyInfo.dwAccessType.intValue() : null,
			winHttpProxyInfo.lpszProxy != null ? winHttpProxyInfo.lpszProxy.getValue() : null,
			winHttpProxyInfo.lpszProxyBypass != null ? winHttpProxyInfo.lpszProxyBypass.getValue() : null);
	}

	private ProxySelector createAutoDetectableProxySelectors(IEProxyConfig ieProxyConfig) {

		if (!ieProxyConfig.isAutoDetect()) {
			Logger.log(getClass(), LogLevel.TRACE, "Auto-detecting not requested.");
			return null;
		}

		Logger.log(getClass(), LogLevel.TRACE, "Auto-detecting script URL.");
		// This will take some time.
		DWORD dwAutoDetectFlags = new DWORD(WinHttp.WINHTTP_AUTO_DETECT_TYPE_DHCP | WinHttp.WINHTTP_AUTO_DETECT_TYPE_DNS_A);
		String pacUrl = WinHttpHelpers.detectAutoProxyConfigUrl(dwAutoDetectFlags);

		if (pacUrl == null) {
			Logger.log(getClass(), LogLevel.TRACE, "PAC url not auto-detectable.");
			return null;
		}
		Logger.log(getClass(), LogLevel.TRACE, "IE uses script: " + pacUrl);

		Logger.log(getClass(), LogLevel.TRACE, "Created Auto-detecting proxy selector.");
		return ProxyUtil.buildPacSelectorForUrl(pacUrl);

	}

	private ProxySelector createAutoConfigProxySelectors(IEProxyConfig ieProxyConfig) {

		String pacUrl = ieProxyConfig.getAutoConfigUrl();
		if (pacUrl == null || pacUrl.trim().length() == 0) {
			Logger.log(getClass(), LogLevel.TRACE, "Auto-config not requested.");
			return null;
		}
		Logger.log(getClass(), LogLevel.TRACE, "IE uses script: " + pacUrl);

		// Fix for issue 9
		// If the IE has a file URL and it only starts has 2 slashes,
		// add a third so it can be properly converted to the URL class
		if (pacUrl.startsWith("file://") && !pacUrl.startsWith("file:///")) {
			pacUrl = pacUrl.replace("file://", "file:///");
			Logger.log(getClass(), LogLevel.TRACE, "PAC URL modified to " + pacUrl);
		}

		Logger.log(getClass(), LogLevel.TRACE, "Created Auto-config proxy selector.");
		return ProxyUtil.buildPacSelectorForUrl(pacUrl);
	}


	/*************************************************************************
	 * Parses the proxy settings into an ProxySelector.
	 *
	 * @param ieProxyConfig
	 *            the settings to use.
	 * @return a ProxySelector, null if no settings are set.
	 * @
	 *             on error.
	 ************************************************************************/

	private ProxySelector createFixedProxySelector(IEProxyConfig ieProxyConfig) {
		String proxyString = ieProxyConfig.getProxy();
		String bypassList = ieProxyConfig.getProxyBypass();
		if (proxyString == null) {
			return null;
		}
		Logger.log(getClass(), LogLevel.TRACE, "IE uses manual settings: {0} with bypass list: {1}", proxyString,
			bypassList);

		Properties p = ProxyUtil.parseProxyList(proxyString);

		ProtocolDispatchSelector ps = ProxyUtil.buildProtocolDispatchSelector(p);

		return ProxyUtil.setByPassListOnSelector(bypassList, ps);
	}

	private ProxySelector createWinHttpProxySelector(WinHttpProxyConfig proxyInfo) {
		if (proxyInfo.getAccessType() != WinHttp.WINHTTP_ACCESS_TYPE_NAMED_PROXY) return null;
		String proxyString = proxyInfo.getProxy();
		String bypassList = proxyInfo.getProxyBypass();
		if (proxyString == null) {
			return null;
		}
		Logger.log(getClass(), LogLevel.TRACE, "WinHttp uses manual settings: {0} with bypass list: {1}", proxyString,
			bypassList);

		Properties p = ProxyUtil.parseProxyList(proxyString);

		ProtocolDispatchSelector ps = ProxyUtil.buildProtocolDispatchSelector(p);

		return ProxyUtil.setByPassListOnSelector(bypassList, ps);
	}
}
