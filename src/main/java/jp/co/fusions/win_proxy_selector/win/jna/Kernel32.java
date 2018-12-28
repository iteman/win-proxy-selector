package jp.co.fusions.win_proxy_selector.win.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * kernel32.dll Interface.
 *
 * @author Kei Sugimoto, Copyright 2018
 */
public interface Kernel32 extends StdCallLibrary {

	/** The instance. */
	Kernel32 INSTANCE = Native.loadLibrary("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

	/**
	 * Frees the specified global memory object and invalidates its handle.
	 *
	 * @param hGlobal
	 *            A handle to the global memory object.
	 * @return If the function succeeds, the return value is NULL If the
	 *         function fails, the return value is equal to a handle to the
	 *         global memory object. To get extended error information, call
	 *         {@code GetLastError}.
	 * @see <A HREF="https://msdn.microsoft.com/en-us/library/windows/desktop/aa366579(v=vs.85).aspx">GlobalFree</A>
	 */
	Pointer GlobalFree(Pointer hGlobal);
}
