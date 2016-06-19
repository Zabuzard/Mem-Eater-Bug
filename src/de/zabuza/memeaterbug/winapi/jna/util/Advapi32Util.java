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

public abstract class Advapi32Util {
	public static void enableDebugPrivilege(HANDLE hProcess) {
		HANDLEByReference hToken = new HANDLEByReference();
		Win32Exception we = null;
		try {
			if (!Advapi32.INSTANCE.OpenProcessToken(hProcess, WinNT.TOKEN_QUERY | WinNT.TOKEN_ADJUST_PRIVILEGES,
					hToken)) {
				throw new Win32Exception(Native.getLastError());
			}

			LUID luid = new LUID();
			if (!Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_DEBUG_NAME, luid)) {
				throw new Win32Exception(Native.getLastError());
			}

			TOKEN_PRIVILEGES tkp = new TOKEN_PRIVILEGES(1);
			tkp.Privileges[0] = new LUID_AND_ATTRIBUTES(luid, new DWORD(WinNT.SE_PRIVILEGE_ENABLED));
			if (!Advapi32.INSTANCE.AdjustTokenPrivileges(hToken.getValue(), false, tkp, 0, null, null)) {
				throw new Win32Exception(Native.getLastError());
			}
		} catch (Win32Exception e) {
			we = e;
		} finally {
			if (!Kernel32.INSTANCE.CloseHandle(hToken.getValue())) {
				Win32Exception e = new Win32Exception(Native.getLastError());
				if (we != null) {
					e.addSuppressed(we);
				}
				we = e;
			}
		}

		if (we != null) {
			throw we;
		}
	}

}
