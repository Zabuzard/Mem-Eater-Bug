package de.zabuza.memeaterbug.winapi;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import de.zabuza.memeaterbug.winapi.jna.Psapi.LPMODULEINFO;
import de.zabuza.memeaterbug.winapi.jna.util.PsapiUtil;

/**
 * Represents Windows MODULEINFO structure.
 * 
 * @see <a href=
 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx"> MSDN
 *      webpage#MODULEINFO structure</a>
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class Module {
	/**
	 * The entry point of the module.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 */
	private Pointer mEntryPoint;
	/**
	 * Handle to this module.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/aa383751(v=vs.85).aspx">
	 *      MSDN webpage#Windows Data Types</a>
	 */
	private final HMODULE mHModule;
	/**
	 * Handle to the process this module belongs to.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684868(v=vs.85).aspx">
	 *      MSDN webpage#Process Handles and Identifiers</a>
	 */
	private final HANDLE mHProcess;
	/**
	 * Pointer to the load address of this module.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 */
	private Pointer mLpBaseOfDll;
	/**
	 * The size of the linear space that the module occupies.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 */
	private int mSizeOfImage;

	/**
	 * Creates a new module that is able to read information from the module of
	 * the given process, by the given module handle.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684868(v=vs.85).aspx">
	 *      MSDN webpage#Process Handles and Identifiers</a>
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/aa383751(v=vs.85).aspx">
	 *      MSDN webpage#Windows Data Types</a>
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 * 
	 * @param hProcess
	 *            Handle to the process of this module
	 * @param hModule
	 *            Handle of the module to create around
	 */
	public Module(final HANDLE hProcess, final HMODULE hModule) {
		this.mHProcess = hProcess;
		this.mHModule = hModule;

		this.mLpBaseOfDll = null;
		this.mEntryPoint = null;
		this.mSizeOfImage = 0;
	}

	/**
	 * The entry point of the module.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 * 
	 * @return The entry point of the module
	 */
	public Pointer getEntryPoint() {
		extractModuleInformation();
		return this.mEntryPoint;
	}

	/**
	 * Gets the fully qualified path for the file containing the current module.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms683198(v=vs.85).aspx">
	 *      MSDN webpage#GetModuleFileNameEx function</a>
	 * 
	 * @return The fully qualified path for the file containing the current
	 *         module
	 */
	public String getFileName() {
		return PsapiUtil.getModuleFileNameEx(this.mHProcess, this.mHModule);
	}

	/**
	 * Gets a handle to this module.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/aa383751(v=vs.85).aspx">
	 *      MSDN webpage#Windows Data Types</a>
	 * 
	 * @return A handle to this module
	 */
	public HMODULE getHModule() {
		return this.mHModule;
	}

	/**
	 * Pointer to the load address of this module.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 * 
	 * @return Pointer to the load address of this module
	 */
	public Pointer getLpBaseOfDll() {
		extractModuleInformation();
		return this.mLpBaseOfDll;
	}

	/**
	 * The size of the linear space that the module occupies.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
	 *      MSDN webpage#MODULEINFO structure</a>
	 * 
	 * @return The size of the linear space that the module occupies
	 */
	public int getSizeOfImage() {
		extractModuleInformation();
		return this.mSizeOfImage;
	}

	/**
	 * Extracts and updates all properties for the module.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms683201(v=vs.85).aspx">
	 *      MSDN webpage#GetModuleInformation function</a>
	 */
	private void extractModuleInformation() {
		if (this.mEntryPoint == null) {
			try {
				final LPMODULEINFO x = PsapiUtil.getModuleInformation(this.mHProcess, this.mHModule);
				this.mLpBaseOfDll = x.lpBaseOfDll;
				this.mSizeOfImage = x.SizeOfImage;
				this.mEntryPoint = x.EntryPoint;
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

}
