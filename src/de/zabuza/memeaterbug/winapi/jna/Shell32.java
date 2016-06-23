package de.zabuza.memeaterbug.winapi.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * JNA interface for Windows SHELL32.DLL, which is a component of the Windows
 * API that allows applications to access functions provided by the operating
 * system shell, and to change and enhance it.
 * 
 * @author Zabuza
 *
 */
public interface Shell32 extends com.sun.jna.platform.win32.Shell32 {
	/**
	 * Instance of the Shell32.dll JNA interface.
	 */
	public Shell32 INSTANCE = (Shell32) Native.loadLibrary("shell32", Shell32.class);

	/**
	 * Creates an array of handles to large or small icons extracted from the
	 * specified executable file, DLL, or icon file.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms648069(v=vs.85).aspx">
	 *      MSDN webpage#ExtractIconEx function</a>
	 * @param lpszFile
	 *            The name of an executable file, DLL, or icon file from which
	 *            icons will be extracted.
	 * @param nIconIndex
	 *            The zero-based index of the first icon to extract. For
	 *            example, if this value is zero, the function extracts the
	 *            first icon in the specified file.
	 * @param phiconLarge
	 *            An array of icon handles that receives handles to the large
	 *            icons extracted from the file. If this parameter is
	 *            <tt>null</tt>, no large icons are extracted from the file.
	 * @param phiconSmall
	 *            An array of icon handles that receives handles to the small
	 *            icons extracted from the file. If this parameter is
	 *            <tt>null</tt>, no small icons are extracted from the file.
	 * @param nIcons
	 *            The number of icons to be extracted from the file.
	 * @return If the nIconIndex parameter is -1, the phiconLarge parameter is
	 *         <tt>null</tt>, and the phiconSmall parameter is <tt>null</tt>,
	 *         then the return value is the number of icons contained in the
	 *         specified file. Otherwise, the return value is the number of
	 *         icons successfully extracted from the file.
	 */
	public int ExtractIconEx(String lpszFile, int nIconIndex, Pointer[] phiconLarge, Pointer[] phiconSmall, int nIcons);
}