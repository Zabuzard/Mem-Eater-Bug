package de.zabuza.memeaterbug.winapi.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

/**
 * JNA interface for Windows PSAPI.DLL, which is the process status application
 * programming interface. The Psapi is a helper library that makes it easier to
 * obtain information about processes and device drivers.
 * 
 * @see <a href=
 *      "https://msdn.microsoft.com/en-us/library/ms684884(v=vs.85).aspx"> MSDN
 *      webpage#Process Status API</a>
 * 
 * @author Zabuza
 *
 */
public interface Psapi extends com.sun.jna.platform.win32.Psapi {
	/**
	 * Representation of the MODULEINFO structure of Psapi.dll.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 * 
	 * @author Zabuza
	 *
	 */
	public static final class LPMODULEINFO extends Structure {
		/**
		 * The entry point of the module.
		 * 
		 * @see <a href=
		 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
		 *      MSDN webpage#MODULEINFO structure</a>
		 */
		public Pointer EntryPoint;
		/**
		 * The load address of the module.
		 * 
		 * @see <a href=
		 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
		 *      MSDN webpage#MODULEINFO structure</a>
		 */
		public Pointer lpBaseOfDll;
		/**
		 * The size of the linear space that the module occupies, in bytes.
		 * 
		 * @see <a href=
		 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
		 *      MSDN webpage#MODULEINFO structure</a>
		 */
		public int SizeOfImage;

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.jna.Structure#getFieldOrder()
		 */
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(new String[] { "lpBaseOfDll", "SizeOfImage", "EntryPoint" });
		}
	}

	/**
	 * Instance of the Psapi.dll JNA interface.
	 */
	public Psapi INSTANCE = (Psapi) Native.loadLibrary("Psapi", Psapi.class, W32APIOptions.DEFAULT_OPTIONS);

	/**
	 * Flag for listing the 32-bit modules.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx">
	 *      MSDN webpage#EnumProcessModulesEx function</a>
	 */
	public static final int LIST_MODULES_32BIT = 0x01;
	/**
	 * Flag for listing the 64-bit modules.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx">
	 *      MSDN webpage#EnumProcessModulesEx function</a>
	 */
	public static final int LIST_MODULES_64BIT = 0x02;
	/**
	 * Flag for listing all modules.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx">
	 *      MSDN webpage#EnumProcessModulesEx function</a>
	 */
	public static final int LIST_MODULES_ALL = 0x03;
	/**
	 * Flag for using the default behavior for listing modules.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx">
	 *      MSDN webpage#EnumProcessModulesEx function</a>
	 */
	public static final int LIST_MODULES_DEFAULT = 0x0;

	/**
	 * Retrieves a handle for each module in the specified process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms682631(v=vs.85).aspx">
	 *      MSDN webpage#EnumProcessModules function</a>
	 * 
	 * @param hProcess
	 *            A handle to the process.
	 * @param lphModule
	 *            An array that receives the list of module handles.
	 * @param cb
	 *            The size of the lphModule array, in bytes.
	 * @param lpcbNeededs
	 *            The number of bytes required to store all module handles in
	 *            the lphModule array.
	 * @return If the function succeeds, the return value is nonzero.<br/>
	 *         <br/>
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call {@link #Native.GetLastError()}.
	 */
	boolean EnumProcessModules(final HANDLE hProcess, final HMODULE[] lphModule, final int cb,
			final IntByReference lpcbNeededs);

	/**
	 * Retrieves a handle for each module in the specified process that meets
	 * the specified filter criteria.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx">
	 *      MSDN webpage#EnumProcessModulesEx function</a>
	 * 
	 * @param hProcess
	 *            A handle to the process.
	 * @param lphModule
	 *            An array that receives the list of module handles.
	 * @param cb
	 *            The size of the lphModule array, in bytes.
	 * @param lpcbNeededs
	 *            The number of bytes required to store all module handles in
	 *            the lphModule array.
	 * @param dwFilterFlag
	 *            The filter criteria. This parameter can be one of the
	 *            following values.
	 *            <ul>
	 *            <li>{@link #LIST_MODULES_32BIT}</li>
	 *            <li>{@link #LIST_MODULES_64BIT}</li>
	 *            <li>{@link #LIST_MODULES_ALL}</li>
	 *            <li>{@link #LIST_MODULES_DEFAULT}</li>
	 *            </ul>
	 * @return If the function succeeds, the return value is nonzero.<br/>
	 *         <br/>
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call {@link #Native.GetLastError()}.
	 */
	boolean EnumProcessModulesEx(final HANDLE hProcess, final HMODULE[] lphModule, final int cb,
			final IntByReference lpcbNeededs, final int dwFilterFlag);

	/**
	 * Retrieves information about the specified module in the
	 * {@link LPMODULEINFO} structure.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms683201(v=vs.85).aspx">
	 *      MSDN webpage#GetModuleInformation function</a>
	 * 
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
	 * @param lpmodinfo
	 *            A pointer to the {@link LPMODULEINFO} structure that receives
	 *            information about the module.
	 * @param cb
	 *            The size of the {@link LPMODULEINFO} structure, in bytes.
	 * @return If the function succeeds, the return value is nonzero.<br/>
	 *         <br/>
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call {@link #Native.GetLastError()}.
	 */
	boolean GetModuleInformation(final HANDLE hProcess, final HMODULE hModule, final LPMODULEINFO lpmodinfo,
			final int cb);

	/**
	 * Retrieves the name of the executable file for the specified process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms683217(v=vs.85).aspx">
	 *      MSDN webpage#GetProcessImageFileName function</a>
	 * 
	 * @param hProcess
	 *            A handle to the process. The handle must have the
	 *            {@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_QUERY_INFORMATION
	 *            PROCESS_QUERY_INFORMATION} or
	 *            {@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_QUERY_LIMITED_INFORMATION
	 *            PROCESS_QUERY_LIMITED_INFORMATION} access right.
	 * @param lpImageFileName
	 *            A pointer to a buffer that receives the full path to the
	 *            executable file.
	 * @param nSize
	 *            The size of the lpImageFileName buffer, in characters.
	 * @return If the function succeeds, the return value specifies the length
	 *         of the string copied to the buffer.<br/>
	 *         <br/>
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call {@link #Native.GetLastError()}.
	 */
	int GetProcessImageFileName(final HANDLE hProcess, final byte[] lpImageFileName, final int nSize);

}