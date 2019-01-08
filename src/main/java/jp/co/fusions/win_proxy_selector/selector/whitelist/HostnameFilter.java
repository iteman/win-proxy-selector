package jp.co.fusions.win_proxy_selector.selector.whitelist;

import java.net.URI;

import jp.co.fusions.win_proxy_selector.util.UriFilter;
/**
 * Tests if a host name of a given URI matches some criteria.
 *
 * @author Kei Sugimoto, Copyright 2019
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 *
 * @see <a href="https://docs.microsoft.com/en-us/previous-versions/windows/it-pro/windows-2000-server/cc939852(v=technet.10)">https://docs.microsoft.com/en-us/previous-versions/windows/it-pro/windows-2000-server/cc939852(v=technet.10)</a>
 *
 */
final class HostnameFilter implements UriFilter {

	private static final String PROTOCOL_ENDING = "://";

	private String matchTo;
	private String protocolFilter;

	/*************************************************************************
	 * Constructor
	 *
	 * @param matchTo
	 *            the match criteria.
	 ************************************************************************/

	HostnameFilter(String matchTo) {
		super();
		this.matchTo = matchTo.toLowerCase();

		extractProtocolFilter();

		this.matchTo = toRegex(this.matchTo);
	}

	/*************************************************************************
	 * Extracts the protocol if one is given to initialize the protocol matcher.
	 ************************************************************************/

	private void extractProtocolFilter() {
		int protocolIndex = this.matchTo.indexOf(PROTOCOL_ENDING);
		if (protocolIndex != -1) {
			this.protocolFilter = this.matchTo.substring(0, protocolIndex);
			this.matchTo = this.matchTo.substring(protocolIndex + PROTOCOL_ENDING.length());
		}
	}

	/*************************************************************************
	 * accept
	 * 
	 * @see UriFilter#accept(java.net.URI)
	 ************************************************************************/

	public boolean accept(URI uri) {
		if (uri == null || uri.getAuthority() == null) {
			return false;
		}

		if (!isProtocolMatching(uri)) {
			return false;
		}

		String host = uri.getAuthority();

		// Strip away port take special care for IP6.
		int index = host.indexOf(':');
		int index2 = host.lastIndexOf(']');
		if (index != -1 && index2 < index) {
			host = host.substring(0, index);
		}

		return host.toLowerCase().matches(this.matchTo);
	}
	private String toRegex(String string){
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			switch (ch) {
				case '*':
					b.append("(.*)");
					break;
				default:
					String s = Integer.toString(ch, 16);
					int numOfZeroes = 4 - s.length();
					b.append("\\u");
					for (int j = 0; j < numOfZeroes; j++) b.append('0');
					b.append(s);
			}
		}
		return b.toString();

	}
	/*************************************************************************
	 * Applies the protocol filter if available to see if we have a match.
	 * 
	 * @param uri
	 *            to test for a correct protocol.
	 * @return true if passed else false.
	 ************************************************************************/

	private boolean isProtocolMatching(URI uri) {
		return this.protocolFilter == null || uri.getScheme() == null
		        || uri.getScheme().equalsIgnoreCase(this.protocolFilter);
	}

}
