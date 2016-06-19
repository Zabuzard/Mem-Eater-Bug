package de.zabuza.memeaterbug.winapi.jna.util;

import java.util.LinkedList;
import java.util.List;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.BaseTSD.SIZE_T;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.Tlhelp32.PROCESSENTRY32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.MEMORY_BASIC_INFORMATION;
import com.sun.jna.ptr.IntByReference;

import de.zabuza.memeaterbug.winapi.api.Process;
import de.zabuza.memeaterbug.winapi.api.ProcessList;
import de.zabuza.memeaterbug.winapi.jna.Kernel32;
import de.zabuza.memeaterbug.winapi.jna.User32;

public abstract class Kernel32Util {

	/**
	 * Pages in the region become guard pages. <br>
	 * Any attempt to access a guard page causes the system to raise a
	 * STATUS_GUARD_PAGE_VIOLATION exception and turn off the guard page status.
	 * <br>
	 * Guard pages thus act as a one-time access alarm. <br>
	 * For more information, see Creating Guard Pages. <br>
	 * When an access attempt leads the system to turn off guard page status,
	 * the underlying page protection takes over.<br>
	 * If a guard page exception occurs during a system service, the service
	 * typically returns a failure status indicator. <br>
	 * This value cannot be used with PAGE_NOACCESS. This flag is not supported
	 * by the CreateFileMapping function.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/windows/desktop/aa366786(v=vs.85).aspx">
	 *      MSDN</a>
	 */
	// belongs in WinNT.h
	public static final int PAGE_GUARD = 0x100;

	/**
	 * Disables all access to the committed region of pages.<br>
	 * An attempt to read from, write to, or execute the committed region
	 * results in an access violation.<br>
	 * This flag is not supported by the CreateFileMapping function.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/windows/desktop/aa366786(v=vs.85).aspx">
	 *      MSDN</a>
	 */
	// belongs in WinNT.h
	public static final int PAGE_NOACCESS = 0x01;

	/**
	 * All possible access rights for a process object. Windows Server 2003 and
	 * Windows XP: The size of the PROCESS_ALL_ACCESS flag increased on Windows
	 * Server 2008 and Windows Vista. <br>
	 * If an application compiled for Windows Server 2008 and Windows Vista is
	 * run on Windows Server 2003 or Windows XP, the PROCESS_ALL_ACCESS flag is
	 * too large and the function specifying this flag fails with
	 * ERROR_ACCESS_DENIED.<br>
	 * To avoid this problem, specify the minimum set of access rights required
	 * for the operation.<br>
	 * If PROCESS_ALL_ACCESS must be used, set _WIN32_WINNT to the minimum
	 * operating system targeted by your application (for example, #define
	 * _WIN32_WINNT _WIN32_WINNT_WINXP).<br>
	 * For more information, see Using the Windows Headers.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684880(v=VS.85).aspx">
	 *      MSDN</a>
	 */
	// belongs in WinNT.h
	public static final int PROCESS_ALL_ACCESS = WinNT.PROCESS_CREATE_PROCESS | WinNT.PROCESS_CREATE_THREAD
			| WinNT.PROCESS_DUP_HANDLE | WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_QUERY_LIMITED_INFORMATION
			| WinNT.PROCESS_SET_INFORMATION | WinNT.PROCESS_SET_QUOTA | WinNT.PROCESS_SUSPEND_RESUME
			| WinNT.PROCESS_SYNCHRONIZE | WinNT.PROCESS_TERMINATE | WinNT.PROCESS_VM_OPERATION | WinNT.PROCESS_VM_READ
			| WinNT.PROCESS_VM_WRITE | WinNT.DELETE | WinNT.READ_CONTROL | WinNT.WRITE_DAC | WinNT.WRITE_OWNER
			| WinNT.SYNCHRONIZE;

	public static ProcessList getProcessList() {
		ProcessList plist = new ProcessList();

		List<PROCESSENTRY32> list = new LinkedList<PROCESSENTRY32>();

		HANDLE hProcessSnap = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new DWORD(0));

		PROCESSENTRY32 pe32 = new PROCESSENTRY32();
		if (!Kernel32.INSTANCE.Process32First(hProcessSnap, pe32)) {
			throw new Win32Exception(Native.getLastError());
		}

		do {
			if (pe32.th32ProcessID.intValue() != 0) {
				list.add(pe32);
			}
			pe32 = new PROCESSENTRY32();
		} while (Kernel32.INSTANCE.Process32Next(hProcessSnap, pe32));

		for (PROCESSENTRY32 pe : list) {
			plist.add(new Process(pe));
		}

		Kernel32.INSTANCE.CloseHandle(hProcessSnap);

		List<DesktopWindow> windows = WindowUtils.getAllWindows(false);
		IntByReference lpdwProcessId = new IntByReference();
		int pid = 0;
		for (DesktopWindow window : windows) {
			User32.INSTANCE.GetWindowThreadProcessId(window.getHWND(), lpdwProcessId);
			pid = lpdwProcessId.getValue();
			plist.add(pid, window.getHWND());
		}

		return plist;
	}

	public static HANDLE OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId) {
		HANDLE process = Kernel32.INSTANCE.OpenProcess(dwDesiredAccess, false, dwProcessId);
		if (process == null) {
			throw new Win32Exception(Native.getLastError());
		}
		return process;
	}
	
	public static void CloseHandle(HANDLE processHandle) {
		boolean success = Kernel32.INSTANCE.CloseHandle(processHandle);
		if (!success) {
			throw new Win32Exception(Native.getLastError());
		}
	}

	public static void ReadProcessMemory(HANDLE hProcess, long pAddress, Pointer outputBuffer, int nSize,
			IntByReference outNumberOfBytesRead) {
		boolean success = Kernel32.INSTANCE.ReadProcessMemory(hProcess, pAddress, outputBuffer, nSize,
				outNumberOfBytesRead);
		if (!success) {
			throw new Win32Exception(Native.getLastError());
		}
	}
	
	public static Memory readMemory(HANDLE process, long address, int bytesToRead) {
		IntByReference read = new IntByReference(0);
		Memory output = new Memory(bytesToRead);

		Kernel32Util.ReadProcessMemory(process, address, output, bytesToRead, read);
		return output;
	}

	public static void writeMemory(HANDLE process, long address, byte[] data) {
		int size = data.length;
		Memory toWrite = new Memory(size);

		for (int i = 0; i < size; i++) {
			toWrite.setByte(i, data[i]);
		}

		Kernel32Util.WriteProcessMemory(process, address, toWrite, size, null);
	}
	
	public static void WriteProcessMemory(HANDLE hProcess, long lpBaseAddress, Pointer lpBuffer, int nSize,
			IntByReference lpNumberOfBytesWritten) {
		boolean success = Kernel32.INSTANCE.WriteProcessMemory(hProcess, lpBaseAddress, lpBuffer, nSize, lpNumberOfBytesWritten);
		if (!success) {
			throw new Win32Exception(Native.getLastError());
		}
	}
	
	public static boolean Is64Bit(HANDLE hProcess) {
		if (System.getenv("PROCESSOR_ARCHITECTURE") == "x86") {
			return false;
		}
		boolean isWow64 = false;
		boolean success = Kernel32.INSTANCE.IsWow64Process(hProcess, isWow64);
		if (!success) {
			throw new Win32Exception(Native.getLastError());
		}
		return !isWow64;
	}

	public static MEMORY_BASIC_INFORMATION VirtualQueryEx(HANDLE hProcess, Pointer lpAddress) {
		MEMORY_BASIC_INFORMATION lpBuffer = new MEMORY_BASIC_INFORMATION();
		SIZE_T ret = Kernel32.INSTANCE.VirtualQueryEx(hProcess, lpAddress, lpBuffer, new SIZE_T(lpBuffer.size()));
		if (ret.intValue() == 0) {
			throw new Win32Exception(Native.getLastError());
		}
		return lpBuffer;
	}

}
