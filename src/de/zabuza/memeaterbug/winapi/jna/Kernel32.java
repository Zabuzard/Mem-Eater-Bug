package de.zabuza.memeaterbug.winapi.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {

	Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

	/*
	 * http://msdn.microsoft.com/en-us/library/ms681674(v=vs.85).aspx
	 */
	boolean WriteProcessMemory(HANDLE hProcess, long lpBaseAddress, Pointer lpBuffer, int nSize, IntByReference lpNumberOfBytesWritten);

	/*
	 * http://msdn.microsoft.com/en-us/library/ms680553(v=vs.85).aspx
	 */
	boolean ReadProcessMemory(HANDLE hProcess, long lpBaseAddress, Pointer lpBuffer, int nSize,
			IntByReference lpNumberOfBytesRead);
	/*
	 * http://msdn.microsoft.com/en-us/library/ms684139(v=vs.85).aspx
	 */
	boolean IsWow64Process(HANDLE hProcess, boolean Wow64Process);
}
