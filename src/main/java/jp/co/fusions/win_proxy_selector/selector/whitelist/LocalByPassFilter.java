package jp.co.fusions.win_proxy_selector.selector.whitelist;

import java.net.URI;

import jp.co.fusions.win_proxy_selector.util.UriFilter;

/*****************************************************************************
 *
 * @author Kei Sugimoto, Copyright 2018
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public final class LocalByPassFilter implements UriFilter {

	/*************************************************************************
	 * accept
	 * 
	 * @see UriFilter#accept(java.net.URI)
	 ************************************************************************/

	public boolean accept(URI uri) {
		if (uri == null) {
			return false;
		}
		String host = uri.getAuthority();
		return host != null && !host.contains(".");
	}

}
