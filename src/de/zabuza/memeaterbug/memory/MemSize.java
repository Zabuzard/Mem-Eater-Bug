package de.zabuza.memeaterbug.memory;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import de.zabuza.memeaterbug.winapi.jna.util.Kernel32Util;

/**
 * Provides methods for getting the size of different data types that are used
 * at memory interaction.
 * 
 * @author Zabuza
 *
 */
public final class MemSize {

	/**
	 * Cached constant for the size of an integer in bytes.
	 */
	private static int intSize = -1;

	/**
	 * Size of a module handle {@link HMODULE} in a 32 bit process.
	 */
	private static final int MODULE_SIZE_32 = 4;

	/**
	 * Size of a module handle {@link HMODULE} in a 64 bit process.
	 */
	private static final int MODULE_SIZE_64 = 8;

	/**
	 * Gets the size of an integer in bytes on the current platform.
	 * 
	 * @return The size of an integer in bytes
	 */
	public static int getIntSize() {
		if (intSize == -1) {
			intSize = Native.getNativeSize(int.class);
		}
		return intSize;
	}

	/**
	 * Gets the size of a module handle {@link HMODULE} in the given process. It
	 * is determined by the architecture the process is using.
	 * 
	 * @param hProcess
	 *            Handle to the process
	 * @return The size of a module handle {@link HMODULE} in the given process,
	 *         in bytes
	 */
	public static int getSizeOfModule(final HANDLE hProcess) {
		if (Kernel32Util.is64Bit(hProcess)) {
			return MODULE_SIZE_64;
		} else {
			return MODULE_SIZE_32;
		}
	}

	/**
	 * Utility class. No implementation.
	 */
	private MemSize() {

	}
}
