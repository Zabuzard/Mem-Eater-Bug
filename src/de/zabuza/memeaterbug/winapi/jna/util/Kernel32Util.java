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

import de.zabuza.memeaterbug.util.Masks;
import de.zabuza.memeaterbug.util.SystemProperties;
import de.zabuza.memeaterbug.winapi.Process;
import de.zabuza.memeaterbug.winapi.ProcessList;
import de.zabuza.memeaterbug.winapi.jna.Kernel32;
import de.zabuza.memeaterbug.winapi.jna.User32;

/**
 * Provides various utility methods that use the JNA interface for Windows
 * KERNEL32.DLL, which exposes to applications most of the Win32 base APIs, such
 * as memory management, input/output (I/O) operations, process and thread
 * creation, and synchronization functions.
 * 
 * @author Zabuza
 *
 */
public final class Kernel32Util {

	/**
	 * Pages in the region become guard pages.<br/>
	 * Any attempt to access a guard page causes the system to raise a
	 * STATUS_GUARD_PAGE_VIOLATION exception and turn off the guard page status.
	 * <br>
	 * Guard pages thus act as a one-time access alarm.<br/>
	 * For more information, see Creating Guard Pages.<br/>
	 * When an access attempt leads the system to turn off guard page status,
	 * the underlying page protection takes over.<br/>
	 * If a guard page exception occurs during a system service, the service
	 * typically returns a failure status indicator.<br/>
	 * This value cannot be used with {@link #PAGE_NOACCESS}. This flag is not
	 * supported by the CreateFileMapping function.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/aa366786(v=vs.85).aspx">
	 *      MSDN webpage#Memory Protection Constants</a>
	 */
	public static final int PAGE_GUARD = 0x100;

	/**
	 * Disables all access to the committed region of pages.<br/>
	 * An attempt to read from, write to, or execute the committed region
	 * results in an access violation.<br/>
	 * This flag is not supported by the CreateFileMapping function.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/aa366786(v=vs.85).aspx">
	 *      MSDN webpage#Memory Protection Constants</a>
	 */
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
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684880(v=VS.85).aspx">
	 *      MSDN webpage#Process Security and Access Rights</a>
	 */
	public static final int PROCESS_ALL_ACCESS = WinNT.PROCESS_CREATE_PROCESS | WinNT.PROCESS_CREATE_THREAD
			| WinNT.PROCESS_DUP_HANDLE | WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_QUERY_LIMITED_INFORMATION
			| WinNT.PROCESS_SET_INFORMATION | WinNT.PROCESS_SET_QUOTA | WinNT.PROCESS_SUSPEND_RESUME
			| WinNT.PROCESS_SYNCHRONIZE | WinNT.PROCESS_TERMINATE | WinNT.PROCESS_VM_OPERATION | WinNT.PROCESS_VM_READ
			| WinNT.PROCESS_VM_WRITE | WinNT.DELETE | WinNT.READ_CONTROL | WinNT.WRITE_DAC | WinNT.WRITE_OWNER
			| WinNT.SYNCHRONIZE;

	/**
	 * Closes an open object handle.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms724211(v=vs.85).aspx">
	 *      MSDN webpage#CloseHandle function</a>
	 * @param processHandle
	 *            A valid handle to an open object.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static void closeHandle(final HANDLE processHandle) throws Win32Exception {
		boolean success = Kernel32.INSTANCE.CloseHandle(processHandle);
		if (!success) {
			throw new Win32Exception(Native.getLastError());
		}
	}

	/**
	 * Gets a list of currently active processes by creating a snapshot.
	 * 
	 * @return List of currently active processes
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static ProcessList getProcessList() throws Win32Exception {
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

	/**
	 * Whether the given process is a 64-bit application or not. A 32-bit
	 * application that runs in the WoW64 environment is not considered as
	 * 64-bit application, since they are restricted to the 32-bit memory space.
	 * 
	 * @param hProcess
	 *            Handle to the process in question
	 * @return <tt>True</tt> if the given process is a 64-bit application,
	 *         <tt>false</tt> otherwise.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static boolean is64Bit(final HANDLE hProcess) throws Win32Exception {
		if (System.getenv(SystemProperties.PRC_ARCH) == Masks.PRC_ARCH_32BIT) {
			return false;
		}

		IntByReference isWow64 = new IntByReference();
		boolean success = Kernel32.INSTANCE.IsWow64Process(hProcess, isWow64);
		if (!success) {
			throw new Win32Exception(Native.getLastError());
		}
		return isWow64.getValue() == 0;
	}

	/**
	 * Opens an existing local process object.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684320(v=vs.85).aspx">
	 *      MSDN webpage#OpenProcess function</a>
	 * @param dwDesiredAccess
	 *            The access to the process object. This access right is checked
	 *            against the security descriptor for the process. This
	 *            parameter can be one or more of the process access rights.
	 *            <br/>
	 *            <br/>
	 *            If the caller has enabled the SeDebugPrivilege privilege, the
	 *            requested access is granted regardless of the contents of the
	 *            security descriptor.
	 * @param bInheritHandle
	 *            If this value is <tt>true</tt>, processes created by this
	 *            process will inherit the handle. Otherwise, the processes do
	 *            not inherit this handle.
	 * @param dwProcessId
	 *            The identifier of the local process to be opened.
	 * @return If the function succeeds, the return value is an open handle to
	 *         the specified process.<br/>
	 *         <br/>
	 *         If the function fails, the return value is <tt>null</tt>. To get
	 *         extended error information, call {@link #Native.GetLastError()}.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static HANDLE openProcess(final int dwDesiredAccess, final boolean bInheritHandle, final int dwProcessId)
			throws Win32Exception {
		HANDLE process = Kernel32.INSTANCE.OpenProcess(dwDesiredAccess, false, dwProcessId);
		if (process == null) {
			throw new Win32Exception(Native.getLastError());
		}
		return process;
	}

	/**
	 * Reads a number of bytes starting at a given address from the memory space
	 * of a given process.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms680553(v=vs.85).aspx">
	 *      MSDN webpage#ReadProcessMemory function</a>
	 * @param process
	 *            Handle of the process to read from
	 * @param address
	 *            Starting address in the memory space of the given process to
	 *            start reading from
	 * @param bytesToRead
	 *            Number of bytes to read from the starting address
	 * @return Memory object that holds the read data.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static Memory readMemory(final HANDLE process, final long address, final int bytesToRead)
			throws Win32Exception {
		IntByReference read = new IntByReference(0);
		Memory output = new Memory(bytesToRead);

		Kernel32Util.readProcessMemory(process, address, output, bytesToRead, read);
		return output;
	}

	/**
	 * Reads data from an area of memory in a specified process. The entire area
	 * to be read must be accessible or the operation fails.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms680553(v=vs.85).aspx">
	 *      MSDN webpage#ReadProcessMemory function</a>
	 * @param hProcess
	 *            A handle to the process with memory that is being read. The
	 *            handle must have
	 *            {@link de.zabuza.memeaterbug.winapi.Process#PROCESS_VM_READ
	 *            PROCESS_VM_READ} access to the process.
	 * @param pAddress
	 *            The base address in the specified process from which to read.
	 *            Before any data transfer occurs, the system verifies that all
	 *            data in the base address and memory of the specified size is
	 *            accessible for read access, and if it is not accessible the
	 *            function fails.
	 * @param outputBuffer
	 *            A pointer to a buffer that receives the contents from the
	 *            address space of the specified process.
	 * @param nSize
	 *            The number of bytes to be read from the specified process.
	 * @param outNumberOfBytesRead
	 *            A pointer to a variable that receives the number of bytes
	 *            transferred into the specified buffer. If lpNumberOfBytesRead
	 *            is <tt>null</tt>, the parameter is ignored.
	 * @throws Win32Exception
	 *             If the operation is not successful
	 */
	public static void readProcessMemory(final HANDLE hProcess, final long pAddress, final Pointer outputBuffer,
			final int nSize, final IntByReference outNumberOfBytesRead) throws Win32Exception {
		boolean success = Kernel32.INSTANCE.ReadProcessMemory(hProcess, pAddress, outputBuffer, nSize,
				outNumberOfBytesRead);
		if (!success) {
			throw new Win32Exception(Native.getLastError());
		}
	}

	/**
	 * Retrieves information about a range of pages within the virtual address
	 * space of a specified process.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/aa366907(v=vs.85).aspx">
	 *      MSDN webpage#VirtualQueryEx function</a>
	 * @param hProcess
	 *            A handle to the process whose memory information is queried.
	 *            The handle must have been opened with the
	 *            {@link de.zabuza.memeaterbug.winapi.Process#PROCESS_QUERY_INFORMATION
	 *            PROCESS_QUERY_INFORMATION} access right, which enables using
	 *            the handle to read information from the process object.
	 * @param lpAddress
	 *            A pointer to the base address of the region of pages to be
	 *            queried. This value is rounded down to the next page boundary.
	 *            To determine the size of a page on the host computer, use the
	 *            GetSystemInfo function.
	 * @return A pointer to a {@link MEMORY_BASIC_INFORMATION} structure in
	 *         which information about the specified page range is returned.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static MEMORY_BASIC_INFORMATION virtualQueryEx(final HANDLE hProcess, final Pointer lpAddress)
			throws Win32Exception {
		MEMORY_BASIC_INFORMATION lpBuffer = new MEMORY_BASIC_INFORMATION();
		SIZE_T ret = Kernel32.INSTANCE.VirtualQueryEx(hProcess, lpAddress, lpBuffer, new SIZE_T(lpBuffer.size()));
		if (ret.intValue() == 0) {
			throw new Win32Exception(Native.getLastError());
		}
		return lpBuffer;
	}

	/**
	 * Writes data to an area of memory in a specified process. The entire area
	 * to be written to must be accessible or the operation fails.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms681674(v=vs.85).aspx">
	 *      MSDN webpage#WriteProcessMemory function</a>
	 * @param process
	 *            A handle to the process memory to be modified. The handle must
	 *            have
	 *            {@link de.zabuza.memeaterbug.winapi.Process#PROCESS_VM_WRITE
	 *            PROCESS_VM_WRITE} and
	 *            {@link de.zabuza.memeaterbug.winapi.Process#PROCESS_VM_OPERATION
	 *            PROCESS_VM_OPERATION} access to the process.
	 * @param address
	 *            A pointer to the base address in the specified process to
	 *            which data is written. Before data transfer occurs, the system
	 *            verifies that all data in the base address and memory of the
	 *            specified size is accessible for write access, and if it is
	 *            not accessible, the function fails.
	 * @param data
	 *            A buffer that contains data to be written in the address space
	 *            of the specified process. Read from left to right, i.e. from
	 *            the lower to the higher indices.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static void writeMemory(final HANDLE process, final long address, final byte[] data) throws Win32Exception {
		int size = data.length;
		Memory toWrite = new Memory(size);

		for (int i = 0; i < size; i++) {
			toWrite.setByte(i, data[i]);
		}

		Kernel32Util.writeProcessMemory(process, address, toWrite, size, null);
	}

	/**
	 * Writes data reversely to an area of memory in a specified process. The
	 * entire area to be written to must be accessible or the operation fails.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms681674(v=vs.85).aspx">
	 *      MSDN webpage#WriteProcessMemory function</a>
	 * @param process
	 *            A handle to the process memory to be modified. The handle must
	 *            have
	 *            {@link de.zabuza.memeaterbug.winapi.Process#PROCESS_VM_WRITE
	 *            PROCESS_VM_WRITE} and
	 *            {@link de.zabuza.memeaterbug.winapi.Process#PROCESS_VM_OPERATION
	 *            PROCESS_VM_OPERATION} access to the process.
	 * @param address
	 *            A pointer to the base address in the specified process to
	 *            which data is written. Before data transfer occurs, the system
	 *            verifies that all data in the base address and memory of the
	 *            specified size is accessible for write access, and if it is
	 *            not accessible, the function fails.
	 * @param data
	 *            A buffer that contains data to be written reversely in the
	 *            address space of the specified process. Read from right to
	 *            left, i.e. from the higher to the lower indices.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static void writeMemoryReversely(final HANDLE process, final long address, final byte[] data)
			throws Win32Exception {
		int size = data.length;
		Memory toWrite = new Memory(size);

		int lastIndex = size - 1;
		for (int i = 0; i < size; i++) {
			toWrite.setByte(i, data[lastIndex - i]);
		}

		Kernel32Util.writeProcessMemory(process, address, toWrite, size, null);
	}

	/**
	 * Writes data to an area of memory in a specified process. The entire area
	 * to be written to must be accessible or the operation fails.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms681674(v=vs.85).aspx">
	 *      MSDN webpage#WriteProcessMemory function</a>
	 * 
	 * @param hProcess
	 *            A handle to the process memory to be modified. The handle must
	 *            have
	 *            {@link de.zabuza.memeaterbug.winapi.Process#PROCESS_VM_WRITE
	 *            PROCESS_VM_WRITE} and
	 *            {@link de.zabuza.memeaterbug.winapi.Process#PROCESS_VM_OPERATION
	 *            PROCESS_VM_OPERATION} access to the process.
	 * @param lpBaseAddress
	 *            A pointer to the base address in the specified process to
	 *            which data is written. Before data transfer occurs, the system
	 *            verifies that all data in the base address and memory of the
	 *            specified size is accessible for write access, and if it is
	 *            not accessible, the function fails.
	 * @param lpBuffer
	 *            A buffer that contains data to be written in the address space
	 *            of the specified process.
	 * @param nSize
	 *            The number of bytes to be written to the specified process.
	 * @param lpNumberOfBytesWritten
	 *            A pointer to a variable that receives the number of bytes
	 *            transferred into the specified process. This parameter is
	 *            optional. If lpNumberOfBytesWritten is <tt>null</tt>, the
	 *            parameter is ignored.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static void writeProcessMemory(final HANDLE hProcess, final long lpBaseAddress, final Pointer lpBuffer,
			final int nSize, final IntByReference lpNumberOfBytesWritten) throws Win32Exception {
		boolean success = Kernel32.INSTANCE.WriteProcessMemory(hProcess, lpBaseAddress, lpBuffer, nSize,
				lpNumberOfBytesWritten);
		if (!success) {
			throw new Win32Exception(Native.getLastError());
		}
	}

	/**
	 * Utility class. No implementation.
	 */
	private Kernel32Util() {

	}
}
