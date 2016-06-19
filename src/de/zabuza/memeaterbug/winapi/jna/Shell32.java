package de.zabuza.memeaterbug.winapi.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface Shell32 extends com.sun.jna.platform.win32.Shell32 {
	Shell32 INSTANCE = (Shell32) Native.loadLibrary("shell32", Shell32.class);

	/*
	 * http://msdn.microsoft.com/en-us/library/ms648069(VS.85).aspx
	 */
	public int ExtractIconEx(String lpszFile, int nIconIndex, Pointer[] phiconLarge, Pointer[] phiconSmall, int nIcons);
}