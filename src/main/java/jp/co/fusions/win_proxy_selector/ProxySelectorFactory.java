package jp.co.fusions.win_proxy_selector;

import java.net.ProxySelector;

/*****************************************************************************
 * Interface for a proxy selector factory.
 *
 * @author Kei Sugimoto, Copyright 2018
 ****************************************************************************/

public interface ProxySelectorFactory {

	/*************************************************************************
	 * Gets a ProxySelector.
	 * 
	 * @return a ProxySelector
	 ************************************************************************/
	ProxySelector getProxySelector();

}
