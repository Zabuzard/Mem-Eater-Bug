package de.zabuza.memeaterbug.winapi.jna.util;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Win32Exception;

import de.zabuza.memeaterbug.winapi.jna.Shell32;

/**
 * Provides various utility methods that use the JNA interface for Windows
 * SHELL32.DLL, which is a component of the Windows API that allows applications
 * to access functions provided by the operating system shell, and to change and
 * enhance it.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class Shell32Util {
	/**
	 * Extracts a pointer to the first small icon from the specified executable
	 * file, DLL, or icon file.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms648069(v=vs.85).aspx">
	 *      MSDN webpage#ExtractIconEx function</a>
	 * @param lpszFile
	 *            The name of an executable file, DLL, or icon file from which
	 *            the icon will be extracted.
	 * @param nIcons
	 *            The number of icons to be extracted from the file.
	 * @return A pointer to the first small icon from the specified executable
	 *         file, DLL, or icon file.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static Pointer extractSmallIcon(final String lpszFile, final int nIcons) throws Win32Exception {
		final Pointer[] hIcons = new Pointer[1];
		Shell32.INSTANCE.ExtractIconEx(lpszFile, 0, null, hIcons, nIcons);
		return hIcons[0];
	}

	/**
	 * Utility class. No implementation.
	 */
	private Shell32Util() {

	}
}
