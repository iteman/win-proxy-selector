package jp.co.fusions.win_proxy_selector.win.jna;

import jp.co.fusions.win_proxy_selector.util.Logger;
import com.sun.jna.LastErrorException;

/**
 * Static helper methods for Windows {@code WinHttp} functions.
 *
 * @author Kei Sugimoto, Copyright 2018
 * @author phansson
 */
public class WinHttpHelpers {

    private WinHttpHelpers() {
    }

    /**
     * Finds the URL for the Proxy Auto-Configuration (PAC) file using WPAD.
     * This is merely a wrapper around
     * {@link WinHttp#WinHttpDetectAutoProxyConfigUrl(WinDef.DWORD, WinDef.LPWSTRByReference)
     *
     * WinHttpDetectAutoProxyConfigUrl}
     *
     * <p>
     * This method is blocking and may take some time to execute.
     * 
     * @param dwAutoDetectFlags flags for auto detection
     * @return the url of the PAC file or {@code null} if it cannot be located
     *         using WPAD method.
     */
    public static String detectAutoProxyConfigUrl(WinDef.DWORD dwAutoDetectFlags) {
        WinDef.LPWSTRByReference ppwszAutoConfigUrl = new WinDef.LPWSTRByReference();
        boolean result = false;
        try {
            result = WinHttp.INSTANCE.WinHttpDetectAutoProxyConfigUrl(dwAutoDetectFlags, ppwszAutoConfigUrl);
        } catch (LastErrorException ex) {
            if (ex.getErrorCode() == WinHttp.ERROR_WINHTTP_AUTODETECTION_FAILED) {
                // This error is to be expected. It just means that the lookup
                // using either DHCP, DNS or both, failed because there wasn't
                // a useful reply from DHCP / DNS. (meaning the site hasn't
                // configured their DHCP Server or their DNS Server for WPAD)
                return null;
            }
            // Something more serious is wrong. There isn't much we can do
            // about it but at least we would like to log it.
            Logger.log(WinHttpHelpers.class, Logger.LogLevel.ERROR,
                    "Windows function WinHttpDetectAutoProxyConfigUrl returned error : {0}", ex.getMessage());
            return null;
        }
        if (result) {
            return ppwszAutoConfigUrl.getString();
        } else {
            return null;
        }
    }
}
