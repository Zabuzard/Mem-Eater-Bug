package de.zabuza.memeaterbug.winapi.api;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import de.zabuza.memeaterbug.winapi.jna.Psapi.LPMODULEINFO;
import de.zabuza.memeaterbug.winapi.jna.util.PsapiUtil;

/**
 * Represents Windows MODULEINFO structure.<br/>
 * <br/>
 * See
 * <a href= "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">
 * MSDN webpage#MODULEINFO structure</a> for more information.
 * 
 * @author Zabuza
 *
 */
public final class Module {

	/**
	 * The entry point of the module.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">MSDN
	 * webpage#MODULEINFO structure</a> for more information.
	 */
	private Pointer mEntryPoint;
	/**
	 * Handle to this module.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/aa383751(v=vs.85).aspx">MSDN
	 * webpage#Windows Data Types</a> for more information.
	 */
	private final HMODULE mHModule;
	/**
	 * Handle to the process this module belongs to.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684868(v=vs.85).aspx">MSDN
	 * webpage#Process Handles and Identifiers</a> for more information.
	 */
	private final HANDLE mHProcess;
	/**
	 * Pointer to the load address of this module.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">MSDN
	 * webpage#MODULEINFO structure</a> for more information.
	 */
	private Pointer mLpBaseOfDll;
	/**
	 * The size of the linear space that the module occupies.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">MSDN
	 * webpage#MODULEINFO structure</a> for more information.
	 */
	private int mSizeOfImage;

	/**
	 * Creates a new module that is able to read information from the module of
	 * the given process, by the given module handle.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684868(v=vs.85).aspx">MSDN
	 * webpage#Process Handles and Identifiers</a>, <a href=
	 * "https://msdn.microsoft.com/en-us/library/aa383751(v=vs.85).aspx">MSDN
	 * webpage#Windows Data Types</a> and <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">MSDN
	 * webpage#MODULEINFO structure</a> for more information.
	 * 
	 * @param hProcess
	 *            Handle to the process of this module
	 * @param hModule
	 *            Handle of the module to create around
	 */
	public Module(final HANDLE hProcess, final HMODULE hModule) {
		this.mHProcess = hProcess;
		this.mHModule = hModule;

		mLpBaseOfDll = null;
		mEntryPoint = null;
		mSizeOfImage = 0;
	}

	/**
	 * The entry point of the module.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">MSDN
	 * webpage#MODULEINFO structure</a> for more information.
	 * 
	 * @return The entry point of the module
	 */
	public Pointer getEntryPoint() {
		extractModuleInformation();
		return mEntryPoint;
	}

	/**
	 * Gets the fully qualified path for the file containing the current module.
	 * <br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms683198(v=vs.85).aspx">MSDN
	 * webpage#GetModuleFileNameEx function</a> for more information.
	 * 
	 * @return The fully qualified path for the file containing the current
	 *         module
	 */
	public String getFileName() {
		return PsapiUtil.GetModuleFileNameEx(mHProcess, mHModule);
	}

	/**
	 * Gets a handle to this module.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/aa383751(v=vs.85).aspx">MSDN
	 * webpage#Windows Data Types</a> for more information.
	 * 
	 * @return A handle to this module
	 */
	public HMODULE getHModule() {
		return mHModule;
	}

	/**
	 * Pointer to the load address of this module.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">MSDN
	 * webpage#MODULEINFO structure</a> for more information.
	 * 
	 * @return Pointer to the load address of this module
	 */
	public Pointer getLpBaseOfDll() {
		extractModuleInformation();
		return mLpBaseOfDll;
	}

	/**
	 * The size of the linear space that the module occupies.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms684229(v=vs.85).aspx">MSDN
	 * webpage#MODULEINFO structure</a> for more information.
	 * 
	 * @return The size of the linear space that the module occupies
	 */
	public int getSizeOfImage() {
		extractModuleInformation();
		return mSizeOfImage;
	}

	/**
	 * Extracts and updates all properties for the module.<br/>
	 * <br/>
	 * See <a href=
	 * "https://msdn.microsoft.com/en-us/library/ms683201(v=vs.85).aspx">MSDN
	 * webpage#GetModuleInformation function</a> for more information.
	 */
	private void extractModuleInformation() {
		if (mEntryPoint == null) {
			try {
				LPMODULEINFO x = PsapiUtil.GetModuleInformation(mHProcess, mHModule);
				mLpBaseOfDll = x.lpBaseOfDll;
				mSizeOfImage = x.SizeOfImage;
				mEntryPoint = x.EntryPoint;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
