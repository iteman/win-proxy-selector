package jp.co.fusions.win_proxy_selector.selector.whitelist;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import jp.co.fusions.win_proxy_selector.util.UriFilter;

/*****************************************************************************
 * Filters an URI by inspecting it's IP address is in a given range. The range
 * as must be defined in CIDR notation. e.g. 192.0.2.1/24,
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public final class IpRangeFilter implements UriFilter {
	private static final int IPv4_BIT_LENGTH = 32;
	private static final int IPv6_BIT_LENGTH = 128;
	private static final int IPv4_BYTE_LENGTH = IPv4_BIT_LENGTH / 8;
	private static final int IPv6_BYTE_LENGTH = IPv6_BIT_LENGTH / 8;

	private String originalMatchTo;
	private byte[] matchTo;
	private int numOfBits;

	/*************************************************************************
	 * Constructor
	 * 
	 * @param matchTo
	 *            the match subnet in CIDR notation.
	 ************************************************************************/

	public IpRangeFilter(String matchTo) {
		this.originalMatchTo = matchTo;
	}
	private void prepare(){
		String[] parts = this.originalMatchTo.split("/");
		if (parts.length != 2) {
			return;
		}

		try {
			InetAddress address = InetAddress.getByName(parts[0].trim());
			this.matchTo = address.getAddress();
		} catch (UnknownHostException e) {
			return;
		}


		this.numOfBits = Integer.parseInt(parts[1].trim());
		// In case of IPv4, convert to IPv4-mapped IPv6 address
		if (this.matchTo.length == IPv4_BYTE_LENGTH){
			this.matchTo = toIPv6(this.matchTo);
			if (!parts[0].contains(":")){
				this.numOfBits = this.numOfBits + (IPv6_BIT_LENGTH - IPv4_BIT_LENGTH);
			}
		}
	}

	/*************************************************************************
	 * accept
	 * 
	 * @see UriFilter#accept(java.net.URI)
	 ************************************************************************/

	public boolean accept(URI uri) {
		if (uri == null || uri.getHost() == null) {
			return false;
		}

		return acceptsHost(uri.getHost());
	}
	public boolean acceptsHost(String host) {
		if (this.matchTo == null){
			prepare();
		}

		try {
			InetAddress address = InetAddress.getByName(host);
			byte[] addr = address.getAddress();

			// We want to compare in IPv6-basis
			if (addr.length != this.matchTo.length) {
				if (addr.length == IPv4_BYTE_LENGTH){
					addr = toIPv6(addr);
				} else {
					return false;
				}
			}

			int bit = 0;
			for (int nibble = 0; nibble < addr.length; nibble++) {
				for (int nibblePos = 7; nibblePos >= 0; nibblePos--) {
					if (bit >= this.numOfBits) {
						return true;
					}
					int mask = 1 << nibblePos;
					if ((this.matchTo[nibble] & mask) != (addr[nibble] & mask)) {
						return false;
					}
					bit++;
				}
			}
			return true;

		} catch (UnknownHostException e) {
			// In this case we can not get the IP do not match.
			return false;
		}
	}
	private byte[] toIPv6(byte[] ipv4Address){
		byte ipv4asIpv6Address[] = new byte[IPv6_BYTE_LENGTH];
		int last = IPv6_BYTE_LENGTH - 1;
		ipv4asIpv6Address[last-5] = (byte)0xff;
		ipv4asIpv6Address[last-4] = (byte)0xff;
		ipv4asIpv6Address[last-3] = ipv4Address[0];
		ipv4asIpv6Address[last-2] = ipv4Address[1];
		ipv4asIpv6Address[last-1] = ipv4Address[2];
		ipv4asIpv6Address[last] = ipv4Address[3];

		return ipv4asIpv6Address;
	}
}
