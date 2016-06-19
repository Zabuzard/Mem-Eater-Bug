package de.zabuza.memeaterbug.winapi.api;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import de.zabuza.memeaterbug.winapi.jna.Psapi.LPMODULEINFO;
import de.zabuza.memeaterbug.winapi.jna.util.PsapiUtil;

public class Module {
	private HANDLE hProcess;
	private HMODULE hModule;
	private Pointer lpBaseOfDll;
	private int SizeOfImage = 0;
	private Pointer EntryPoint = null;

	protected Module() {
	}

	public Module(HANDLE hProcess, HMODULE hModule) {
		this.hProcess = hProcess;
		this.hModule = hModule;
	}

	public HMODULE getHMODULE() {
		return hModule;
	}

	public String getFileName() {
		return PsapiUtil.GetModuleFileNameEx(hProcess, hModule);
	}

	private void GetModuleInformation() {
		if (EntryPoint == null) {
			try {
				LPMODULEINFO x = PsapiUtil.GetModuleInformation(hProcess, hModule);
				lpBaseOfDll = x.lpBaseOfDll;
				SizeOfImage = x.SizeOfImage;
				EntryPoint = x.EntryPoint;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Pointer getLpBaseOfDll() {
		GetModuleInformation();
		return lpBaseOfDll;
	}

	public int getSizeOfImage() {
		GetModuleInformation();
		return SizeOfImage;
	}

	public Pointer getEntryPoint() {
		GetModuleInformation();
		return EntryPoint;
	}

}
