package jp.co.fusions.win_proxy_selector.selector.whitelist;

import jp.co.fusions.win_proxy_selector.TestUtil;
import jp.co.fusions.win_proxy_selector.util.UriFilter;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;


/*****************************************************************************
 * Some unit tests for the UriFilter class.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class UriFilterTest {

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testBeginsWithFilter1() {
		UriFilter filter = new HostnameFilter("no_proxy*");

		assertTrue(filter.accept(TestUtil.NO_PROXY_TEST_URI));
	}

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testBeginsWithFilter2() {
		UriFilter filter = new HostnameFilter("no_proxy*");

		assertFalse(filter.accept(TestUtil.HTTP_TEST_URI));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testBeginsWithFilter3() throws URISyntaxException {
		UriFilter filter = new HostnameFilter("192.168.0*");

		assertTrue(filter.accept(new URI("http://192.168.0.100:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testBeginsWithFilter4() throws URISyntaxException {
		UriFilter filter = new HostnameFilter("192.168.0*");

		assertFalse(filter.accept(new URI("http://192.168.1.100:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testBeginsWithFilter() {
		UriFilter filter = new HostnameFilter("no_proxy*");

		assertTrue(filter.accept(TestUtil.NO_PROXY_TEST_URI));
		assertFalse(filter.accept(TestUtil.HTTP_TEST_URI));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testEndsWithFilter() throws URISyntaxException {
		UriFilter filter = new HostnameFilter("*.unit-test.invalid");

		assertTrue(filter.accept(TestUtil.NO_PROXY_TEST_URI));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testEndsWithFilter2() throws URISyntaxException {
		UriFilter filter = new HostnameFilter("*.unit-test.invalid");

		assertFalse(filter.accept(new URI("http://test.no-host.invalid:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testEndsWithFilter3() throws URISyntaxException {
		UriFilter filter = new HostnameFilter("*.100");

		assertTrue(filter.accept(new URI("http://192.168.1.100:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testIpRangeFilter() throws URISyntaxException {
		UriFilter filter = new IpRangeFilter("192.168.0.0/24");

		assertTrue(filter.accept(new URI("http://192.168.0.100:81/test.data")));
		assertFalse(filter.accept(new URI("http://192.168.1.100:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testIp6RangeFilter() throws URISyntaxException {
		UriFilter filter = new IpRangeFilter("2001:4860:0:2001::/24");

		assertTrue(filter.accept(new URI("http://[2001:4860:0:2001::68]:81/test.data")));
		assertFalse(filter.accept(new URI("http://[3001:4860:0:2001::68]:81/test.data")));
	}
	/*************************************************************************
	 * Test method
	 *
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testIp4MappedIp6RangeFilter() throws URISyntaxException {
		UriFilter filter = new IpRangeFilter("::ffff:192.1.8.2/128");
		assertTrue(filter.accept(new URI("http://[::ffff:192.1.8.2]:81/test.data")));
		assertFalse(filter.accept(new URI("http://[::ffff:192.1.8.3]:81/test.data")));

		filter = new IpRangeFilter("::ffff:192.0.2.0/120");
		assertTrue(filter.accept(new URI("http://[::ffff:192.0.2.1]:81/test.data")));
		assertFalse(filter.accept(new URI("http://[::ffff:192.0.1.1]:81/test.data")));

		filter = new IpRangeFilter("::ffff:255.255.255.255/96");
		assertTrue(filter.accept(new URI("http://[::ffff:0.0.0.0]:81/test.data")));

		filter = new IpRangeFilter("::ffff:255.255.255.255/95");
		assertTrue(filter.accept(new URI("http://[::ffff:0.0.0.0]:81/test.data")));

		filter = new IpRangeFilter("::ffff:255.255.255.255/97");
		assertFalse(filter.accept(new URI("http://[::ffff:0.0.0.0]:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testWithProtocolFilter() throws URISyntaxException {
		UriFilter filter = new HostnameFilter("http://192.168.0.100");

		assertTrue(filter.accept(new URI("http://192.168.0.100:81/test.data")));
		assertFalse(filter.accept(new URI("ftp://192.168.0.100:81/test.data")));
		assertFalse(filter.accept(new URI("http://192.168.1.100:81/test.data")));
	}

}
