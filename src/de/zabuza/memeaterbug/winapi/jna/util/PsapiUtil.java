package de.zabuza.memeaterbug.winapi.jna.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

import de.zabuza.memeaterbug.winapi.api.Module;
import de.zabuza.memeaterbug.winapi.jna.Psapi;
import de.zabuza.memeaterbug.winapi.jna.Psapi.LPMODULEINFO;
import de.zabuza.memeaterbug.winapi.api.Process;

/**
 * Provides various utility methods that use the JNA interface for Windows
 * PSAPI.DLL, which is the process status application programming interface. The
 * Psapi is a helper library that makes it easier to obtain information about
 * processes and device drivers.
 * 
 * @author Zabuza
 *
 */
public final class PsapiUtil {
	/**
	 * Amount of module handles a module buffer should store.
	 */
	private static int MODULE_BUFFER_AMOUNT = 100;

	/**
	 * Retrieves a list of handles for each module in the specified process.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms682631(v=vs.85).aspx">
	 *      MSDN webpage#EnumProcessModules function</a>
	 * @param hProcess
	 *            A handle to the process.
	 * @return A list of handles for each module in the specified process
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static List<HMODULE> enumProcessModules(final HANDLE hProcess) throws Win32Exception {
		return enumProcessModulesEx(hProcess, null);
	}

	/**
	 * Retrieves a list of handles for each module in the specified process,
	 * that meets the filter criteria specified by the list flag.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms682631(v=vs.85).aspx">
	 *      MSDN webpage#EnumProcessModules function</a>
	 * @param hProcess
	 *            A handle to the process.
	 * @param listFlag
	 *            Specifies the modules to list. Possible values are the
	 *            following.
	 *            <ul>
	 *            <li>
	 *            {@link de.zabuza.memeaterbug.winapi.jna.Psapi#LIST_MODULES_32BIT
	 *            LIST_MODULES_32BIT}</li>
	 *            <li>
	 *            {@link de.zabuza.memeaterbug.winapi.jna.Psapi#LIST_MODULES_64BIT
	 *            LIST_MODULES_64BIT}</li>
	 *            <li>
	 *            {@link de.zabuza.memeaterbug.winapi.jna.Psapi#LIST_MODULES_ALL
	 *            LIST_MODULES_ALL} or <tt>null</tt></li>
	 *            <li>
	 *            {@link de.zabuza.memeaterbug.winapi.jna.Psapi#LIST_MODULES_DEFAULT
	 *            LIST_MODULES_DEFAULT}</li>
	 *            </ul>
	 * @return A list of handles for each module in the specified process, that
	 *         meets the filter criteria specified by the list flag.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static List<HMODULE> enumProcessModulesEx(final HANDLE hProcess, final Integer listFlag)
			throws Win32Exception {
		int moduleSize = Module.getSizeOfModule(hProcess);
		List<HMODULE> list = new LinkedList<HMODULE>();

		HMODULE[] lphModule = new HMODULE[MODULE_BUFFER_AMOUNT * moduleSize];
		IntByReference lpcbNeededs = new IntByReference();

		if (listFlag == null) {
			if (!Psapi.INSTANCE.EnumProcessModules(hProcess, lphModule, lphModule.length, lpcbNeededs)) {
				throw new Win32Exception(Native.getLastError());
			}
		} else {
			if (!Psapi.INSTANCE.EnumProcessModulesEx(hProcess, lphModule, lphModule.length, lpcbNeededs, listFlag)) {
				throw new Win32Exception(Native.getLastError());
			}
		}

		for (int i = 0; i < lpcbNeededs.getValue() / moduleSize; i++) {
			list.add(lphModule[i]);
		}

		return list;
	}

	/**
	 * Retrieves a list of handles for each 32-bit module in the specified
	 * process.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx">
	 *      MSDN webpage#EnumProcessModulesEx function</a>
	 * @param hProcess
	 *            A handle to the process.
	 * @return A list of handles for each 32-bit module in the specified process
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static List<HMODULE> enumProcessModulesEx32(final HANDLE hProcess) throws Win32Exception {
		return enumProcessModulesEx(hProcess, Psapi.LIST_MODULES_32BIT);
	}

	/**
	 * Retrieves a list of handles for each 64-bit module in the specified
	 * process.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx">
	 *      MSDN webpage#EnumProcessModulesEx function</a>
	 * @param hProcess
	 *            A handle to the process.
	 * @return A list of handles for each 64-bit module in the specified process
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static List<HMODULE> enumProcessModulesEx64(final HANDLE hProcess) throws Win32Exception {
		return enumProcessModulesEx(hProcess, Psapi.LIST_MODULES_64BIT);
	}

	/**
	 * Retrieves the fully qualified path for the file containing the specified
	 * module.
	 * 
	 * @see <a href= <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms683198(v=vs.85).aspx">
	 *      MSDN webpage#GetModuleFileNameEx function</a>
	 * @param hProcess
	 *            A handle to the process that contains the module. The handle
	 *            must have the
	 *            {@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_QUERY_INFORMATION
	 *            PROCESS_QUERY_INFORMATION} and
	 *            {@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_VM_READ
	 *            PROCESS_VM_READ} access rights.
	 * @param hModule
	 *            A handle to the module. If this parameter is <tt>null</tt>,
	 *            getModuleFileNameEx returns the path of the executable file of
	 *            the process specified in hProcess.
	 * @return The fully qualified path for the file containing the specified
	 *         module.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static String getModuleFileNameEx(final HANDLE hProcess, final HANDLE hModule) throws Win32Exception {
		Memory lpImageFileName = new Memory(512);
		if (Psapi.INSTANCE.GetModuleFileNameEx(hProcess, hModule, lpImageFileName, 256) == 0) {
			throw new Win32Exception(Native.getLastError());
		}
		return Native.toString(lpImageFileName.getCharArray(0, 256));
	}

	/**
	 * Retrieves information about the specified module in the
	 * {@link LPMODULEINFO} structure.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms683201(v=vs.85).aspx">
	 *      MSDN webpage#GetModuleInformation function</a>
	 * @param hProcess
	 *            A handle to the process that contains the module.<br/>
	 *            <br/>
	 *            The handle must have the
	 *            {@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_QUERY_INFORMATION
	 *            PROCESS_QUERY_INFORMATION} and
	 *            {@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_VM_READ
	 *            PROCESS_VM_READ} access rights.
	 * @param hModule
	 *            A handle to the module.
	 * @return Information about the specified module as {@link LPMODULEINFO}
	 *         structure.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static LPMODULEINFO getModuleInformation(final HANDLE hProcess, final HMODULE hModule)
			throws Win32Exception {
		LPMODULEINFO lpmodinfo = new LPMODULEINFO();

		if (!Psapi.INSTANCE.GetModuleInformation(hProcess, hModule, lpmodinfo, lpmodinfo.size())) {
			throw new Win32Exception(Native.getLastError());
		}
		return lpmodinfo;
	}

	/**
	 * Retrieves the id of the process that belongs to the given exe-file name.
	 * 
	 * @param szExeFile
	 *            Name of the exe-File.
	 * @return The id of the process that belongs to the given exe-file name or
	 *         <tt>0</tt> (zero) if not found.
	 */
	public static int getProcessIdBySzExeFile(final String szExeFile) {
		try {
			Iterator<Process> processes = Kernel32Util.getProcessList().iterator();
			while (processes.hasNext()) {
				Process process = processes.next();
				if (process.getSzExeFile().equalsIgnoreCase(szExeFile)) {
					return process.getPid();
				}
			}
		} catch (Win32Exception e) {

		}

		return 0;
	}

	/**
	 * Retrieves the name of the executable file for the specified process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms683217(v=vs.85).aspx">
	 *      MSDN webpage#GetProcessImageFileName function</a>
	 * @param hProcess
	 *            A handle to the process. The handle must have the
	 *            {@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_QUERY_INFORMATION
	 *            PROCESS_QUERY_INFORMATION} or
	 *            {@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_QUERY_LIMITED_INFORMATION
	 *            PROCESS_QUERY_LIMITED_INFORMATION} access right.
	 * @return The name of the executable file for the specified process.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static String getProcessImageFileName(final HANDLE hProcess) throws Win32Exception {
		byte[] lpImageFileName = new byte[256];
		if (Psapi.INSTANCE.GetProcessImageFileName(hProcess, lpImageFileName, 256) == 0) {
			throw new Win32Exception(Native.getLastError());
		}
		return Native.toString(lpImageFileName);
	}

	/**
	 * Utility class. No implementation.
	 */
	private PsapiUtil() {

	}
}
