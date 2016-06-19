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

import de.zabuza.memeaterbug.winapi.jna.User32;
import de.zabuza.memeaterbug.winapi.jna.util.Kernel32Util;
import de.zabuza.memeaterbug.winapi.jna.util.PsapiUtil;
import de.zabuza.memeaterbug.winapi.jna.util.Shell32Util;
import de.zabuza.memeaterbug.winapi.jna.util.User32Util;

/**
 * Provides various process related methods for interaction with the Windows
 * API.<br/>
 * <br/>
 * See
 * <a href= "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx">
 * MSDN webpage#PROCESSENTRY32 structure</a> for more information.
 * 
 * @author Zabuza
 *
 */
public final class Process {
	/**
	 * All possible access rights for a process object.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx"> MSDN
	 * webpage#Process Security and Access Rights</a> for more information.
	 */
	public static int PROCESS_ALL_ACCESS = 0x001F0FFF;
	/**
	 * Required to retrieve certain information about a process, such as its
	 * token, exit code, and priority class.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx"> MSDN
	 * webpage#Process Security and Access Rights</a> for more information.
	 */
	public static int PROCESS_QUERY_INFORMATION = 0x0400;
	/**
	 * Required to perform an operation on the address space of a process.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx"> MSDN
	 * webpage#Process Security and Access Rights</a> for more information.
	 */
	public static int PROCESS_VM_OPERATION = 0x0008;
	/**
	 * Required to read memory in a process using Windows ReadProcessMemory
	 * function.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx"> MSDN
	 * webpage#Process Security and Access Rights</a> for more information.
	 */
	public static int PROCESS_VM_READ = 0x0010;
	/**
	 * Required to write to memory in a process using Windows WriteProcessMemory
	 * function.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx"> MSDN
	 * webpage#Process Security and Access Rights</a> for more information.
	 */
	public static int PROCESS_VM_WRITE = 0x0020;

	/**
	 * The number of execution threads started by the process.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx"> MSDN
	 * webpage#PROCESSENTRY32 structure</a> for more information.
	 */
	private final int mCntThreads;
	/**
	 * Cached handle to this process that has all access rights. Updated by
	 * {@link #getHandle()}.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684868(v=vs.85).aspx">MSDN
	 * webpage#Process Handles and Identifiers</a> for more information.
	 */
	private HANDLE mHandleCache;
	/**
	 * List of windows the process has, as window handles. Can be added using
	 * {@link #addHwnd(HWND)}.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms633515(v=vs.85).aspx">MSDN
	 * webpage#GetWindow function</a> for more information.
	 */
	private List<HWND> mHWindows;
	/**
	 * A cached icon of this process. Updated by {@link #getIcon()}.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms648070(v=vs.85).aspx">MSDN
	 * webpage#GetIconInfo function</a> for more information.
	 */
	private ImageIcon mIconCache;
	/**
	 * Cached first module of this process module list which is the module for
	 * this process. Updated by {@link #getModule()}.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx"> MSDN
	 * webpage#MODULEINFO structure</a> for more information.
	 */
	private Module mModuleCache;
	/**
	 * The base priority of any threads created by this process.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx"> MSDN
	 * webpage#PROCESSENTRY32 structure</a> for more information.
	 */
	private final int mPcPriClassBase;
	/**
	 * The process identifier.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx"> MSDN
	 * webpage#PROCESSENTRY32 structure</a> for more information.
	 */
	private final int mPid;
	/**
	 * The name of the executable file for the process.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx"> MSDN
	 * webpage#PROCESSENTRY32 structure</a> for more information.
	 */
	private final String mSzExeFile;
	/**
	 * The identifier of the process that created this process (its parent
	 * process).<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx"> MSDN
	 * webpage#PROCESSENTRY32 structure</a> for more information.
	 */
	private final int mTh32ParentProcessID;

	/**
	 * Creates a new process wrapper that is able to read information from the
	 * process, given by its PROCESSENTRY32 structure.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684839(v=vs.85).aspx"> MSDN
	 * webpage#PROCESSENTRY32 structure</a> for more information.
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

	public void addHwnd(final HWND hWnd) {
		mHWindows.add(hWnd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
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

	public Pointer getBase() {
		Module module = getModule();
		if (module != null) {
			return module.getLpBaseOfDll();
		} else {
			return Pointer.NULL;
		}
	}

	public int getCntThreads() {
		return mCntThreads;
	}

	public HANDLE getHandle() {
		if (mHandleCache != null) {
			return mHandleCache;
		}
		mHandleCache = Kernel32Util.OpenProcess(Kernel32Util.PROCESS_ALL_ACCESS, false, mPid);
		return mHandleCache;
	}

	public List<HWND> getHwnds() {
		return mHWindows;
	}

	public ImageIcon getIcon() {
		if (mIconCache != null) {
			return mIconCache;
		}

		HICON hIcon = null;

		Pointer attempt_1 = Shell32Util.ExtractSmallIcon(getModuleFileNameExA(), 1);
		if (attempt_1 != null) {
			hIcon = new HICON(attempt_1);
		}

		if (hIcon == null) {
			Pointer attempt_2 = Shell32Util.ExtractSmallIcon(mSzExeFile, 1);
			if (attempt_2 != null) {
				hIcon = new HICON(attempt_2);
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

	public Module getModule() {
		if (mModuleCache != null) {
			return mModuleCache;
		}
		List<Module> modules = getModules();
		if (modules != null && modules.size() > 0) {
			mModuleCache = modules.get(0);
		}
		return mModuleCache;
	}

	public String getModuleFileNameExA() {
		try {
			return PsapiUtil.GetModuleFileNameEx(getHandle(), null);
		} catch (Exception e) {
			return "";
		}
	}

	public List<Module> getModules() {
		try {
			List<HMODULE> pointers = PsapiUtil.EnumProcessModules(getHandle());
			List<Module> modules = new LinkedList<Module>();
			for (HMODULE hModule : pointers) {
				modules.add(new Module(getHandle(), hModule));
			}
			return modules;
		} catch (Exception e) {
			return null;
		}
	}

	public int getPcPriClassBase() {
		return mPcPriClassBase;
	}

	public int getPid() {
		return mPid;
	}

	public String getProcessImageFileName() {
		try {
			return PsapiUtil.GetProcessImageFileName(getHandle());
		} catch (Exception e) {
			return "";
		}
	}

	public int getSize() {
		Module module = getModule();
		if (module != null) {
			return module.getSizeOfImage();
		} else {
			return 0;
		}
	}

	public String getStatic(final Long address) {
		if (address == null) {
			return null;
		}
		List<Module> modules = getModules();
		long begin, end;
		for (Module module : modules) {
			begin = Pointer.nativeValue(module.getLpBaseOfDll());
			end = begin + module.getSizeOfImage();
			// log.trace("module "+begin+" "+end+" "+module.getFileName());
			if (begin <= address && address <= end) {
				File f = new File(module.getFileName());
				return f.getName() + "+" + String.format("%08X", address - begin);
			}
		}
		return null;
	}

	public String getSzExeFile() {
		return mSzExeFile;
	}

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

	public void readProcessMemory(final int pAddress, final Pointer outputBuffer, final int nSize,
			final IntByReference outNumberOfBytesRead) {
		Kernel32Util.ReadProcessMemory(getHandle(), pAddress, outputBuffer, nSize, outNumberOfBytesRead);
	}
}
