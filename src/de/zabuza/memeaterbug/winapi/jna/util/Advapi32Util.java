package de.zabuza.memeaterbug.winapi.jna.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.WinNT.LUID;
import com.sun.jna.platform.win32.WinNT.LUID_AND_ATTRIBUTES;
import com.sun.jna.platform.win32.WinNT.TOKEN_PRIVILEGES;

/**
 * Provides various utility methods that use the JNA interface for Windows
 * ADVAPI32.DLL, which is the advanced Windows API. It provides access to
 * functions beyond the kernel. Included are things like the Windows registry,
 * shutdown/restart the system (or abort), start/stop/create a Windows service
 * or manage user accounts.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class Advapi32Util {
	/**
	 * Enables the SeDebugPrivilege privilege for the given process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx">
	 *      MSDN webpage#Process Security and Access Rights</a>
	 * @param hProcess
	 *            Process to enable SeDebugPrivilege privilege for
	 * @throws Win32Exception
	 *             If the method was not successful
	 */
	public static void enableDebugPrivilege(final HANDLE hProcess) throws Win32Exception {
		final HANDLEByReference hToken = new HANDLEByReference();
		Win32Exception winException = null;
		try {
			if (!Advapi32.INSTANCE.OpenProcessToken(hProcess, WinNT.TOKEN_QUERY | WinNT.TOKEN_ADJUST_PRIVILEGES,
					hToken)) {
				throw new Win32Exception(Native.getLastError());
			}

			final LUID luid = new LUID();
			if (!Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_DEBUG_NAME, luid)) {
				throw new Win32Exception(Native.getLastError());
			}

			final TOKEN_PRIVILEGES tkp = new TOKEN_PRIVILEGES(1);
			tkp.Privileges[0] = new LUID_AND_ATTRIBUTES(luid, new DWORD(WinNT.SE_PRIVILEGE_ENABLED));
			if (!Advapi32.INSTANCE.AdjustTokenPrivileges(hToken.getValue(), false, tkp, 0, null, null)) {
				throw new Win32Exception(Native.getLastError());
			}
		} catch (final Win32Exception e) {
			winException = e;
		} finally {
			if (!Kernel32.INSTANCE.CloseHandle(hToken.getValue())) {
				final Win32Exception e = new Win32Exception(Native.getLastError());
				if (winException != null) {
					e.addSuppressed(winException);
				}
				winException = e;
			}
		}

		if (winException != null) {
			throw winException;
		}
	}

	/**
	 * Utility class. No implementation.
	 */
	private Advapi32Util() {

	}
}
