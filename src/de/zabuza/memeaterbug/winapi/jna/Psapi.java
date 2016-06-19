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

public interface Psapi extends com.sun.jna.platform.win32.Psapi {
	/*
	 * http://msdn.microsoft.com/en-us/library/ms684229(VS.85).aspx
	 */
	public static class LPMODULEINFO extends Structure {
		public Pointer EntryPoint;
		public Pointer lpBaseOfDll;
		public int SizeOfImage;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(new String[] { "lpBaseOfDll", "SizeOfImage", "EntryPoint" });
		}
	}

	Psapi INSTANCE = (Psapi) Native.loadLibrary("Psapi", Psapi.class, W32APIOptions.DEFAULT_OPTIONS);
	
	/*
	 * http://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx
	 */
	public static final int LIST_MODULES_32BIT = 0x01;
	/*
	 * http://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx
	 */
	public static final int LIST_MODULES_64BIT = 0x02;
	/*
	 * http://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx
	 */
	public static final int LIST_MODULES_ALL = 0x03;
	/*
	 * http://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx
	 */
	public static final int LIST_MODULES_DEFAULT = 0x0;

	/*
	 * http://msdn.microsoft.com/en-us/library/ms682631(VS.85).aspx
	 */
	boolean EnumProcessModules(HANDLE hProcess, HMODULE[] lphModule, int cb, IntByReference lpcbNeededs);
	
	/*
	 * http://msdn.microsoft.com/en-us/library/ms682633(v=vs.85).aspx
	 */
	boolean EnumProcessModulesEx(HANDLE hProcess, HMODULE[] lphModule, int cb, IntByReference lpcbNeededs, int dwFilterFlag);

	/*
	 * http://msdn.microsoft.com/en-us/library/ms683201(VS.85).aspx
	 */
	boolean GetModuleInformation(HANDLE hProcess, HMODULE hModule, LPMODULEINFO lpmodinfo, int cb);

	/*
	 * http://msdn.microsoft.com/en-us/library/ms683217(VS.85).aspx
	 */
	int GetProcessImageFileName(HANDLE hProcess, byte[] lpImageFileName, int nSize);

}