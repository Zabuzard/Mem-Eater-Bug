package de.zabuza.memeaterbug;

import com.sun.jna.platform.win32.WinNT.HANDLE;

import de.zabuza.memeaterbug.locale.ErrorMessages;
import de.zabuza.memeaterbug.util.Masks;
import de.zabuza.memeaterbug.util.SystemProperties;
import de.zabuza.memeaterbug.winapi.api.Process;
import de.zabuza.memeaterbug.winapi.jna.util.User32Util;
import de.zabuza.memeaterbug.winapi.jna.util.Kernel32Util;
import de.zabuza.memeaterbug.winapi.jna.util.PsapiUtil;

/**
 * Provides various methods for memory manipulation on Windows systems using
 * JNA. After creation it needs to be hooked to the given process, by using
 * {@link #hookProcess()}. Before shutdown the process handle should be closed
 * by using {@link #unhookProcess()}.
 * 
 * @author Zabuza
 *
 */
public final class MemEaterBug {

	/**
	 * If the current process runs in an 64 bit environment or not. Can only be
	 * accessed after the the Mem-Eater-Bug was hooked to a process using
	 * {@link #hookProcess()}.
	 */
	private boolean mIs64BitProcess;
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
	 * @throws IllegalStateException
	 *             If the operation system is not a Windows system
	 * @throws IllegalArgumentException
	 *             If the given process id is less than zero or such a process
	 *             could not be found
	 */
	public MemEaterBug(final int processId) {
		ensureOsIsWindows();

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
	 * the name of its exe-file.<br/>
	 * <br/>
	 * After creation, {@link #hookProcess()} must be used before Mem-Eater-Bug
	 * is able to interact. Before shutdown, {@link #unhookProcess()} should be
	 * used to free resources.
	 * 
	 * @param szExeFile
	 *            Name of the exe-File of the process.
	 */
	public MemEaterBug(final String szExeFile) {
		this(PsapiUtil.getProcessIdBySzExeFile(szExeFile));
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
		this(User32Util.getWindowThreadProcessIdByClassAndTitle(processClassName, windowTitle).getValue());
	}

	/**
	 * Hooks the Mem-Eater-Bug to the given process. After that, it is able to
	 * interact with the process and, for example, manipulate its memory. Before
	 * shutdown, {@link #unhookProcess()} should be used to free resources.<br/>
	 * <br/>
	 * It requests the following permissions for interaction with the process:
	 * <ul>
	 * <li>
	 * {@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_QUERY_INFORMATION
	 * PROCESS_QUERY_INFORMATION}</li>
	 * <li>{@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_VM_READ
	 * PROCESS_VM_READ}</li>
	 * <li>{@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_VM_WRITE
	 * PROCESS_VM_WRITE}</li>
	 * <li>{@link de.zabuza.memeaterbug.winapi.api.Process#PROCESS_VM_OPERATION
	 * PROCESS_VM_OPERATION}</li>
	 * </ul>
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx">
	 *      MSDN webpage#Process Security and Access Rights</a>
	 * 
	 * @throws IllegalStateException
	 *             If the Mem-Eater-Bug is already hooked to a process
	 */
	public void hookProcess() {
		hookProcess(Process.PROCESS_QUERY_INFORMATION | Process.PROCESS_VM_READ | Process.PROCESS_VM_WRITE
				| Process.PROCESS_VM_OPERATION);
	}

	/**
	 * Hooks the Mem-Eater-Bug to the given process. After that, it is able to
	 * interact with the process and, for example, manipulate its memory. Before
	 * shutdown, {@link #unhookProcess()} should be used to free resources.
	 * 
	 * @param permissions
	 *            Requested permissions for interaction with the process.
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684880(v=vs.85).aspx">
	 *      MSDN webpage#Process Security and Access Rights</a>
	 * @throws IllegalStateException
	 *             If the Mem-Eater-Bug is already hooked to a process
	 */
	public void hookProcess(final int permissions) {
		if (!mIsHooked) {
			mProcessHandle = Kernel32Util.openProcess(permissions, true, mProcessId);
		} else {
			throw new IllegalStateException(ErrorMessages.PROCESS_UNABLE_TO_HOOK_SINCE_ALREADY_HOOKED);
		}
		mIsHooked = true;
		mIs64BitProcess = Kernel32Util.is64Bit(mProcessHandle);
	}

	/**
	 * Whether the hooked process is a 64-bit application or not. A 32-bit
	 * application that runs in the WoW64 environment is not considered as
	 * 64-bit application, since they are restricted to the 32-bit memory space.
	 * 
	 * @return <tt>True</tt> if the hooked process is a 64-bit application,
	 *         <tt>false</tt> otherwise.
	 * 
	 * @throws IllegalStateException
	 *             If the Mem-Eater-Bug is not hooked to a process
	 */
	public boolean is64BitProcess() {
		if (!mIsHooked) {
			throw new IllegalStateException(ErrorMessages.UNABLE_SINCE_NOT_HOOKED);
		}
		return mIs64BitProcess;
	}

	/**
	 * Whether the Mem-Eater-Bug is hooked to a process or not.
	 * 
	 * @return <tt>True</tt> if the Mem-Eater-Bug is hooked to a process,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean isHooked() {
		return mIsHooked;
	}

	/**
	 * Unhooks the Mem-Eater-Bug from the given process to free resources.
	 * Before that, {@link #hookProcess()} must have been used to hook to a
	 * process.
	 * 
	 * @throws IllegalStateException
	 *             If the Mem-Eater-Bug is not hooked to a process
	 */
	public void unhookProcess() {
		if (mIsHooked) {
			Kernel32Util.closeHandle(mProcessHandle);
		} else {
			throw new IllegalStateException(ErrorMessages.PROCESS_UNABLE_TO_UNHOOK_SINCE_NOT_HOOKED);
		}
		mProcessHandle = null;
		mIsHooked = false;
	}

	/**
	 * Ensures that the operating system is a Windows system.
	 * 
	 * @throws IllegalStateException
	 *             If the operating system is not a Windows system
	 */
	private void ensureOsIsWindows() throws IllegalStateException {
		String osName = System.getProperty(SystemProperties.OS_NAME);
		if (!osName.toLowerCase().contains(Masks.OS_NAME_WINDOWS)) {
			throw new IllegalStateException(ErrorMessages.OS_IS_NOT_WINDOWS + osName);
		}
	}
}
