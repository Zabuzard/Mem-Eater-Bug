package de.zabuza.memeaterbug.winapi.api;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Tlhelp32.PROCESSENTRY32;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

import de.zabuza.memeaterbug.util.Formats;
import de.zabuza.memeaterbug.winapi.jna.User32;
import de.zabuza.memeaterbug.winapi.jna.util.Kernel32Util;
import de.zabuza.memeaterbug.winapi.jna.util.PsapiUtil;
import de.zabuza.memeaterbug.winapi.jna.util.Shell32Util;
import de.zabuza.memeaterbug.winapi.jna.util.User32Util;

/**
 * Provides various process related methods for interaction with the Windows
 * API.
 * 
 * @see <a href= <a href=
 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx"> MSDN
 *      webpage#PROCESSENTRY32 structure</a>
 * 
 * @author Zabuza
 *
 */
public final class Process {
	/**
	 * All possible access rights for a process object.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx">
	 *      MSDN webpage#Process Security and Access Rights</a>
	 */
	public static int PROCESS_ALL_ACCESS = 0x001F0FFF;
	/**
	 * Required to retrieve certain information about a process, such as its
	 * token, exit code, and priority class.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx">
	 *      MSDN webpage#Process Security and Access Rights</a>
	 */
	public static int PROCESS_QUERY_INFORMATION = 0x0400;
	/**
	 * Required to retrieve certain information about a process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx">
	 *      MSDN webpage#Process Security and Access Rights</a>
	 */
	public static int PROCESS_QUERY_LIMITED_INFORMATION = 0x1000;
	/**
	 * Required to perform an operation on the address space of a process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx">
	 *      MSDN webpage#Process Security and Access Rights</a>
	 */
	public static int PROCESS_VM_OPERATION = 0x0008;
	/**
	 * Required to read memory in a process using Windows ReadProcessMemory
	 * function.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx">
	 *      MSDN webpage#Process Security and Access Rights</a>
	 */
	public static int PROCESS_VM_READ = 0x0010;
	/**
	 * Required to write to memory in a process using Windows WriteProcessMemory
	 * function.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx">
	 *      MSDN webpage#Process Security and Access Rights</a>
	 */
	public static int PROCESS_VM_WRITE = 0x0020;
	/**
	 * Index of the module that corresponds to this process, in the module list
	 * of the process.
	 */
	private static int PROCESS_MODULE_INDEX = 0;

	/**
	 * The number of execution threads started by the process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
	 *      MSDN webpage#PROCESSENTRY32 structure</a>
	 */
	private final int mCntThreads;
	/**
	 * Cached handle to this process that has all access rights. Updated by
	 * {@link #getHandle()}.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684868(v=vs.85).aspx">
	 *      MSDN webpage#Process Handles and Identifiers</a>
	 */
	private HANDLE mHandleCache;
	/**
	 * List of windows the process has, as window handles. Can be added using
	 * {@link #addHwnd(HWND)}.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633515(v=vs.85).aspx">
	 *      MSDN webpage#GetWindow function</a>
	 */
	private List<HWND> mHWindows;
	/**
	 * A cached icon of this process. Updated by {@link #getIcon()}.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms648070(v=vs.85).aspx">
	 *      MSDN webpage#GetIconInfo function</a>
	 */
	private ImageIcon mIconCache;
	/**
	 * Cached first module of this process module list which is the module for
	 * this process. Updated by {@link #getModule()}.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 */
	private Module mModuleCache;
	/**
	 * The base priority of any threads created by this process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
	 *      MSDN webpage#PROCESSENTRY32 structure</a>
	 */
	private final int mPcPriClassBase;
	/**
	 * The process identifier.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
	 *      MSDN webpage#PROCESSENTRY32 structure</a>
	 */
	private final int mPid;
	/**
	 * The name of the executable file for the process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
	 *      MSDN webpage#PROCESSENTRY32 structure</a>
	 */
	private final String mSzExeFile;
	/**
	 * The identifier of the process that created this process (its parent
	 * process).
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
	 *      MSDN webpage#PROCESSENTRY32 structure</a>
	 */
	private final int mTh32ParentProcessID;

	/**
	 * Creates a new process wrapper that is able to read information from the
	 * process, given by its PROCESSENTRY32 structure.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
	 *      MSDN webpage#PROCESSENTRY32 structure</a>
	 * 
	 * @param pe32
	 *            PROCESSENTRY32 structure of the process to wrap around
	 */
	public Process(final PROCESSENTRY32 pe32) {
		mPid = pe32.th32ProcessID.intValue();
		mSzExeFile = Native.toString(pe32.szExeFile);
		mCntThreads = pe32.cntThreads.intValue();
		mPcPriClassBase = pe32.pcPriClassBase.intValue();
		mTh32ParentProcessID = pe32.th32ParentProcessID.intValue();

		mHandleCache = null;
		mIconCache = null;
		mModuleCache = null;
		mHWindows = new LinkedList<HWND>();
	}

	/**
	 * Adds the handle to a a window the process has.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633515(v=vs.85).aspx">
	 *      MSDN webpage#GetWindow function</a>
	 * 
	 * @param hWnd
	 *            Window handle to add
	 */
	public void addHwnd(final HWND hWnd) {
		mHWindows.add(hWnd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Process)) {
			return false;
		}
		Process other = (Process) obj;
		if (mPid != other.mPid) {
			return false;
		}
		return true;
	}

	/**
	 * Pointer to the load address of this process module.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 * 
	 * @return Pointer to the load address of this process module
	 */
	public Pointer getBase() {
		Module module = getModule();
		if (module != null) {
			return module.getLpBaseOfDll();
		} else {
			return Pointer.NULL;
		}
	}

	/**
	 * Gets the number of execution threads started by the process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
	 *      MSDN webpage#PROCESSENTRY32 structure</a>
	 * 
	 * @return The number of execution threads started by the process
	 */
	public int getCntThreads() {
		return mCntThreads;
	}

	/**
	 * Gets a handle to this process that has all access rights, caches output
	 * for further method calls.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684868(v=vs.85).aspx">
	 *      MSDN webpage#Process Handles and Identifiers</a>
	 * 
	 * @return A handle to this process that has all access rights
	 */
	public HANDLE getHandle() {
		if (mHandleCache != null) {
			return mHandleCache;
		}
		mHandleCache = Kernel32Util.openProcess(Kernel32Util.PROCESS_ALL_ACCESS, false, mPid);
		return mHandleCache;
	}

	/**
	 * Gets a list of windows the process has, as window handles. Can be added
	 * using {@link #addHwnd(HWND)}.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633515(v=vs.85).aspx">
	 *      MSDN webpage#GetWindow function</a>
	 * 
	 * @return A list of windows the process has, as window handles
	 */
	public List<HWND> getHwnds() {
		return mHWindows;
	}

	/**
	 * Gets the icon of this process. Caches the output for further method
	 * calls.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms648070(v=vs.85).aspx">
	 *      MSDN webpage#GetIconInfo function</a>
	 * 
	 * @return The icon of this process
	 */
	public ImageIcon getIcon() {
		if (mIconCache != null) {
			return mIconCache;
		}

		HICON hIcon = null;

		Pointer firstAttempt = Shell32Util.extractSmallIcon(getModuleFileNameExA(), 1);
		if (firstAttempt != null) {
			hIcon = new HICON(firstAttempt);
		}

		if (hIcon == null) {
			Pointer secondAttempt = Shell32Util.extractSmallIcon(mSzExeFile, 1);
			if (secondAttempt != null) {
				hIcon = new HICON(secondAttempt);
			}
		}

		if (hIcon == null) {
			if (mHWindows.size() > 0) {
				hIcon = User32Util.getHIcon(User32.INSTANCE.GetAncestor(mHWindows.get(0), User32.GA_ROOTOWNER));
			}
		}

		if (hIcon != null) {
			mIconCache = new ImageIcon(User32Util.getIcon(hIcon));
		} else {
			mIconCache = new ImageIcon();
		}
		return mIconCache;
	}

	/**
	 * Gets the first module of this process module list which is the module for
	 * this process. Caches the output for further method calls.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 * 
	 * @return The first module of this process module list which is the module
	 *         for this process.
	 */
	public Module getModule() {
		if (mModuleCache != null) {
			return mModuleCache;
		}
		List<Module> modules = getModules();
		if (modules != null && modules.size() > PROCESS_MODULE_INDEX) {
			mModuleCache = modules.get(PROCESS_MODULE_INDEX);
		}
		return mModuleCache;
	}

	/**
	 * Retrieves the fully qualified path for the file containing this process
	 * module.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms683198(v=vs.85).aspx">
	 *      MSDN webpage#GetModuleFileNameEx function</a>
	 * 
	 * @return The fully qualified path for the file containing this process
	 *         module
	 */
	public String getModuleFileNameExA() {
		try {
			return PsapiUtil.getModuleFileNameEx(getHandle(), null);
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Gets the list of modules that belong to this process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms682631(v=vs.85).aspx">
	 *      MSDN webpage#EnumProcessModules function</a>
	 * 
	 * @return The list of modules that belong to this process
	 */
	public List<Module> getModules() {
		try {
			List<HMODULE> pointers = PsapiUtil.enumProcessModules(getHandle());
			List<Module> modules = new LinkedList<Module>();
			for (HMODULE hModule : pointers) {
				modules.add(new Module(getHandle(), hModule));
			}
			return modules;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Gets the base priority of any threads created by this process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
	 *      MSDN webpage#PROCESSENTRY32 structure</a>
	 * 
	 * @return The base priority of any threads created by this process
	 */
	public int getPcPriClassBase() {
		return mPcPriClassBase;
	}

	/**
	 * Gets the process identifier.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
	 *      MSDN webpage#PROCESSENTRY32 structure</a>
	 * 
	 * @return The process identifier
	 */
	public int getPid() {
		return mPid;
	}

	/**
	 * Retrieves the name of the executable file for this process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms683217(v=vs.85).aspx">
	 *      MSDN webpage#GetProcessImageFileName function</a>
	 * 
	 * @return The name of the executable file for this process
	 */
	public String getProcessImageFileName() {
		try {
			return PsapiUtil.getProcessImageFileName(getHandle());
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Gets the size of the linear space that the process module occupies.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 * 
	 * @return The size of the linear space that the process module occupies
	 */
	public int getSize() {
		Module module = getModule();
		if (module != null) {
			return module.getSizeOfImage();
		} else {
			return 0;
		}
	}

	/**
	 * Gets the name of the module the given address belongs to and the address
	 * offset from the beginning of the module as 8 placed hexadecimal.
	 * 
	 * @param address
	 *            Address of interest
	 * @return The name of the module the given address belongs to and the
	 *         address offset from the beginning of the module as 8 placed
	 *         hexadecimal
	 */
	public String getStatic(final Long address) {
		if (address == null) {
			return null;
		}
		List<Module> modules = getModules();
		long begin;
		long end;
		for (Module module : modules) {
			begin = Pointer.nativeValue(module.getLpBaseOfDll());
			end = begin + module.getSizeOfImage();
			if (begin <= address && address <= end) {
				File f = new File(module.getFileName());
				return f.getName() + "+" + String.format(Formats.EIGHT_HEX_NUMBER, address - begin);
			}
		}
		return null;
	}

	/**
	 * Gets the name of the executable file for the process.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
	 *      MSDN webpage#PROCESSENTRY32 structure</a>
	 * 
	 * @return The name of the executable file for the process
	 */
	public String getSzExeFile() {
		return mSzExeFile;
	}

	/**
	 * Gets the identifier of the process that created this process (its parent
	 * process).
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
	 *      MSDN webpage#PROCESSENTRY32 structure</a>
	 * 
	 * @return The identifier of the process that created this process (its
	 *         parent process)
	 */
	public int getTh32ParentProcessID() {
		return mTh32ParentProcessID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mPid;
		return result;
	}

	/**
	 * Reads data from an area of memory in this process. The entire area to be
	 * read must be accessible or the operation fails.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms680553(v=vs.85).aspx">
	 *      MSDN webpage#ReadProcessMemory function</a>
	 * 
	 * @param pAddress
	 *            The address in this process from which to read. Before any
	 *            data transfer occurs, the system verifies that all data at
	 *            this address and memory of the specified size is accessible
	 *            for read access, and if it is not accessible the function
	 *            fails.
	 * @param outputBuffer
	 *            A pointer to a buffer that receives the contents from the
	 *            address space
	 * @param nSize
	 *            The number of bytes to be read
	 * @param outNumberOfBytesRead
	 *            A pointer to a variable that receives the number of bytes
	 *            transferred into the specified buffer. If outNumberOfBytesRead
	 *            is <tt>null</tt>, the parameter is ignored.
	 */
	public void readProcessMemory(final int pAddress, final Pointer outputBuffer, final int nSize,
			final IntByReference outNumberOfBytesRead) {
		Kernel32Util.readProcessMemory(getHandle(), pAddress, outputBuffer, nSize, outNumberOfBytesRead);
	}
}
