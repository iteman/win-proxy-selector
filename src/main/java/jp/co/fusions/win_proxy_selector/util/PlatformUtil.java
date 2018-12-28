package jp.co.fusions.win_proxy_selector.util;

/*****************************************************************************
 * Defines some helper methods to find the correct platform.
 *
 * @author Kei Sugimoto, Copyright 2018
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class PlatformUtil {

	public static final String OVERRIDE_HOME_DIR = "com.btr.proxy.user.home";

	public enum Platform {
		WIN, LINUX, MAC_OS, SOLARIS, OTHER
	}

	/*************************************************************************
	 * Gets the platform we are currently running on.
	 * 
	 * @return a win code.
	 ************************************************************************/

	public static Platform getCurrentPlatform() {
		String osName = System.getProperty("os.name");
		Logger.log(PlatformUtil.class, Logger.LogLevel.TRACE, "Detecting win. Name is: {0}", osName);

		if (osName.toLowerCase().contains("windows")) {
			Logger.log(PlatformUtil.class, Logger.LogLevel.TRACE, "Detected Windows win: {0}", osName);
			return Platform.WIN;
		}
		if (osName.toLowerCase().contains("linux")) {
			Logger.log(PlatformUtil.class, Logger.LogLevel.TRACE, "Detected Linux win: {0}", osName);
			return Platform.LINUX;
		}
		if (osName.startsWith("Mac OS")) {
			Logger.log(PlatformUtil.class, Logger.LogLevel.TRACE, "Detected Mac OS win: {0}", osName);
			return Platform.MAC_OS;
		}
		if (osName.startsWith("SunOS")) {
			Logger.log(PlatformUtil.class, Logger.LogLevel.TRACE, "Detected Solaris win: {0}", osName);
			return Platform.SOLARIS;
		}

		return Platform.OTHER;
	}

}
