package jp.co.fusions.win_proxy_selector.win.jna;

import com.sun.jna.IntegerType;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.ByReference;
import jp.co.fusions.win_proxy_selector.util.Logger;

/**
 * Pointer wrapper classes for various Windows SDK types. The JNA {@code WTypes}
 * class already have a few of these, but oddly not for all.
 *
 * <p>
 * TODO: Implement pointer wrapper classes for more WTypes, if and when needed.
 *
 * @author Kei Sugimoto, Copyright 2018
 * @author phansson
 */
public class WinDef {
	/**
	 * 16-bit unsigned integer.
	 */
	public static class WORD extends IntegerType implements Comparable<WORD> {

		/** The Constant SIZE. */
		public static final int SIZE = 2;

		/**
		 * Instantiates a new word.
		 */
		public WORD() {
			this(0);
		}

		/**
		 * Instantiates a new word.
		 *
		 * @param value
		 *            the value
		 */
		public WORD(long value) {
			super(SIZE, value, true);
		}

		@Override
		public int compareTo(WORD other) {
			return compare(this, other);
		}
	}
	/**
	 * 32-bit unsigned integer.
	 */
	public static class DWORD extends IntegerType implements Comparable<DWORD> {

		/** The Constant SIZE. */
		public static final int SIZE = 4;

		/**
		 * Instantiates a new dword.
		 */
		public DWORD() {
			this(0);
		}

		/**
		 * Instantiates a new dword.
		 *
		 * @param value
		 *            the value
		 */
		public DWORD(long value) {
			super(SIZE, value, true);
		}

		/**
		 * Low WORD.
		 *
		 * @return Low WORD.
		 */
		public WORD getLow() {
			return new WORD(longValue() & 0xFFFF);
		}

		/**
		 * High WORD.
		 *
		 * @return High WORD.
		 */
		public WORD getHigh() {
			return new WORD((longValue() >> 16) & 0xFFFF);
		}

		@Override
		public int compareTo(DWORD other) {
			return compare(this, other);
		}
	}
	public static class LPWSTR extends PointerType {
//		public static class ByReference extends LPWSTR implements Structure.ByReference {
//		}

		public LPWSTR() {
			super(Pointer.NULL);
		}

		public LPWSTR(Pointer pointer) {
			super(pointer);
		}

		public LPWSTR(String value) {
			this(new Memory((value.length() + 1L) * Native.WCHAR_SIZE));
			this.setValue(value);
		}

		public void setValue(String value) {
			this.getPointer().setWideString(0, value);
		}

		public String getValue() {
			Pointer pointer = this.getPointer();
			String str = null;
			if (pointer != null)
				str = pointer.getWideString(0);

			return str;
		}

		@Override
		public String toString() {
			return this.getValue();
		}
	}
	/**
	 * A pointer to a LPWSTR.
	 *
	 * <p>
	 * LPWSTR is itself a pointer, so a pointer to an LPWSTR is really a
	 * pointer-to-pointer. This class hides this complexity and also takes care
	 * of memory disposal.
	 *
	 * <p>
	 * The class is useful where the Windows function <i>returns</i> a result
	 * into a variable of type {@code LPWSTR*}. The class currently has no
	 * setters so it isn't useful for the opposite case, i.e. where a Windows
	 * function <i>accepts</i> a {@code LPWSTR*} as its input.
	 *
	 *
	 * @author phansson
	 */
	public static class LPWSTRByReference extends ByReference {

		public LPWSTRByReference() {
			super(Pointer.SIZE);
			// memory cleanup
			getPointer().setPointer(0, null);
		}

		/**
		 * Gets the LPWSTR from this pointer. In general its a lot more
		 * convenient simply to use {@link #getString() getString}.
		 *
		 * @return LPWSTR from this pointer
		 */
		public LPWSTR getValue() {
			Pointer p = getPointerToString();
			if (p == null) {
				return null;
			}
			LPWSTR h = new LPWSTR(p);
			return h;
		}

		/**
		 * Gets the string as pointed to by the LPWSTR or {@code null} if
		 * there's no LPWSTR.
		 *
		 * @return LPWSTR from this pointer
		 */
		public String getString() {
			return getValue() == null ? null : getValue().getValue();
		}

		private Pointer getPointerToString() {
			return getPointer().getPointer(0);
		}

		/**
		 * Memory disposal.
		 *
		 * @throws Throwable Something went wrong when cleaning up the memory.
		 */
		@Override
		protected void finalize() throws Throwable {
			try {
				// Free the memory occupied by the string returned
				// from the Win32 function.
				Pointer strPointer = getPointerToString();
				if (strPointer != null) {
					Pointer result = Kernel32.INSTANCE.GlobalFree(strPointer);
					if (result != null) {
						// The call to GlobalFree has failed. This should never
						// happen. If it really does happen, there isn't much we
						// can do about it other than logging it.
						Logger.log(getClass(), Logger.LogLevel.ERROR,
							"Windows function GlobalFree failed while freeing memory for {0} object",
							getClass().getSimpleName());
					}
				}
			} finally {
				// This will free the memory of the pointer-to-pointer
				super.finalize();
			}
		}

	}

}
