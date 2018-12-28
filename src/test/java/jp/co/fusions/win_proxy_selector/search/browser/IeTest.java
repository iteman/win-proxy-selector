package jp.co.fusions.win_proxy_selector.search.browser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import jp.co.fusions.win_proxy_selector.TestUtil;
import jp.co.fusions.win_proxy_selector.selector.whitelist.LocalByPassFilter;
import jp.co.fusions.win_proxy_selector.win.WinProxySelectorFactory;
import jp.co.fusions.win_proxy_selector.util.PlatformUtil;
import jp.co.fusions.win_proxy_selector.util.UriFilter;
import jp.co.fusions.win_proxy_selector.util.PlatformUtil.Platform;

/*****************************************************************************
 * Unit tests for the InternetExplorer search. Only limited testing as this only
 * runs on windwos and needs a installed IE and IE proxy settings written to the
 * registry.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class IeTest {

	/*************************************************************************
	 * Test method.
	 * 
	 * @
	 *             on proxy detection error.
	 ************************************************************************/
	@Test
	public void testInvoke()  {
		if (Platform.WIN.equals(PlatformUtil.getCurrentPlatform())) {
			WinProxySelectorFactory st = new WinProxySelectorFactory();

			// Try at least to invoke it and test if the dll does not crash
			st.getProxySelector();
		}
	}

	/*************************************************************************
	 * Test method.
	 * 
	 * @
	 *             on proxy detection error.
	 * @throws URISyntaxException
	 *             if url syntax is wrong.
	 * @throws MalformedURLException
	 *             on wrong url format.
	 ************************************************************************/
	@Test
	public void testLocalByPassFilter() throws MalformedURLException, URISyntaxException {
		UriFilter filter = new LocalByPassFilter();
		assertTrue(filter.accept(TestUtil.LOCAL_TEST_URI));
		assertFalse(filter.accept(TestUtil.HTTP_TEST_URI));
		assertFalse(filter.accept(new URL("http://123.45.55.6").toURI()));
	}

}
