package jp.co.fusions.win_proxy_selector;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.text.MessageFormat;
import java.util.List;
import jp.co.fusions.win_proxy_selector.util.Logger;
import jp.co.fusions.win_proxy_selector.win.WinProxySelector;

/*****************************************************************************
 * Some examples on how to use the API
 *
 * @author Kei Sugimoto, Copyright 2018
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class Examples {

	public static void main(String[] args) throws Exception{
		Logger.setBackend(new Logger.LogBackEnd() {
			@Override
			public void log(Class<?> clazz, Logger.LogLevel loglevel, String msg, Object... params) {
				System.out.println(loglevel + "\t" + MessageFormat.format(msg, params));
			}
		});
		new Examples().execute();
	}

	private void execute() throws Exception{
		WinProxySelector myProxySelector = new WinProxySelector(ProxySelector.getDefault());
		List<Proxy> proxies = myProxySelector.select(new URI("http://www.fusions.co.jp"));
		System.out.println();
		for (Proxy proxy : proxies) {
			System.out.println(proxy);
		}

	}

}
