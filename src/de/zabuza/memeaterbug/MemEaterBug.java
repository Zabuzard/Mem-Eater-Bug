package de.zabuza.memeaterbug;

import com.sun.jna.platform.win32.WinNT.HANDLE;

import de.zabuza.memeaterbug.locale.ErrorMessages;
import de.zabuza.memeaterbug.winapi.api.Process;
import de.zabuza.memeaterbug.winapi.jna.util.User32Util;
import de.zabuza.memeaterbug.winapi.jna.util.Kernel32Util;

/**
 * Provides various methods for memory manipulation using JNA. After creation it
 * needs to be hooked to the given process, by using {@link #hookProcess()}.
 * Before shutdown the process handle should be closed by using
 * {@link #unhookProcess()}.
 * 
 * @author Zabuza
 *
 */
public final class MemEaterBug {

	/**
	 * If the Mem-Eater-Bug is hooked to a process or not.
	 */
	private boolean mIsHooked;
	/**
	 * Handle to the current process, if hooked, <tt>null</tt> else.
	 */
	private HANDLE mProcessHandle;
	/**
	 * Id of the process this Mem-Eater-Bug belongs to.
	 */
	private final int mProcessId;
	/**
	 * If the current process runs in an 64 bit environment or not. Can only be
	 * accessed after the the Mem-Eater-Bug was hooked to a process using
	 * {@link #hookProcess()}.
	 */
	private boolean mIs64BitProcess;

	/**
	 * Creates a new Mem-Eater-Bug that can interact with a process, given by
	 * its process id.<br/>
	 * <br/>
	 * After creation, {@link #hookProcess()} must be used before Mem-Eater-Bug
	 * is able to interact. Before shutdown, {@link #unhookProcess()} should be
	 * used to free resources.
	 * 
	 * @param processId
	 *            Id of the process the Mem-Eater-Bug should interact with. Must
	 *            be strict greater than zero.
	 */
	public MemEaterBug(final int processId) {
		String osName = System.getProperty("os.name");
		if (!osName.toLowerCase().contains("windows")) {
			throw new IllegalStateException(ErrorMessages.OS_IS_NOT_WINDOWS + osName);
		}

		mIsHooked = false;
		mProcessHandle = null;

		if (processId == 0) {
			throw new IllegalArgumentException(ErrorMessages.PROCESS_NOT_FOUND);
		} else if (processId < 0) {
			throw new IllegalArgumentException(ErrorMessages.PROCESS_ID_INVALID + processId);
		}
		mProcessId = processId;
	}

	/**
	 * Creates a new Mem-Eater-Bug that can interact with a process, given by
	 * its process class name or the window title. In case only one argument
	 * should be used, set the other to <tt>null</tt>.<br/>
	 * <br/>
	 * After creation, {@link #hookProcess()} must be used before Mem-Eater-Bug
	 * is able to interact. Before shutdown, {@link #unhookProcess()} should be
	 * used to free resources.
	 * 
	 * @param processClassName
	 *            The class name of the process
	 * @param windowTitle
	 *            The window title of the process windows
	 */
	public MemEaterBug(final String processClassName, final String windowTitle) {
		this(User32Util.GetWindowThreadProcessIdByClassAndTitle(processClassName, windowTitle).getValue());
	}

	public void hookProcess() {
		hookProcess(Process.PROCESS_QUERY_INFORMATION | Process.PROCESS_VM_READ | Process.PROCESS_VM_WRITE
				| Process.PROCESS_VM_OPERATION);
	}

	public void hookProcess(final int permissions) {
		if (!mIsHooked) {
			mProcessHandle = Kernel32Util.OpenProcess(permissions, true, mProcessId);
		} else {
			throw new IllegalStateException(ErrorMessages.PROCESS_UNABLE_TO_HOOK_SINCE_ALREADY_HOOKED);
		}
		mIsHooked = true;
		mIs64BitProcess = Kernel32Util.Is64Bit(mProcessHandle);
	}

	public boolean isHooked() {
		return mIsHooked;
	}

	public void unhookProcess() {
		if (mIsHooked) {
			Kernel32Util.CloseHandle(mProcessHandle);
		} else {
			throw new IllegalStateException(ErrorMessages.PROCESS_UNABLE_TO_UNHOOK_SINCE_NOT_HOOKED);
		}
		mProcessHandle = null;
		mIsHooked = false;
	}

	public boolean is64BitProcess() {
		if (!mIsHooked) {
			throw new IllegalStateException(ErrorMessages.UNABLE_SINCE_NOT_HOOKED);
		}
		return mIs64BitProcess;
	}
}
