package de.zabuza.memeaterbug.examples;

import java.util.List;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import de.zabuza.memeaterbug.winapi.api.Module;
import de.zabuza.memeaterbug.winapi.jna.util.Kernel32Util;
import de.zabuza.memeaterbug.winapi.jna.util.PsapiUtil;
import de.zabuza.memeaterbug.winapi.jna.util.User32Util;

/**
 * Hack for the popular game Solitaire that allows arbitrary changes of the user score.
 * 
 * @author Zabuza
 *
 */
public final class SoliScorer {

	// final static long baseAddress = 0x10002AFA8L;
	final static long baseAddress = 0x341B220L;
	final static int[] offsets = new int[] { 0x50, 0x14 };

	public static int PROCESS_VM_READ = 0x0010;
	public static int PROCESS_VM_WRITE = 0x0020;
	public static int PROCESS_VM_OPERATION = 0x0008;
	public static int PROCESS_ALL_ACCESS = 0x001F0FFF;
	public static int PROCESS_QUERY_INFORMATION = 0x0400;

	/**
	 * 
	 * @param args
	 *            Not supported
	 */
	public static void main(final String[] args) {
		int pid = User32Util.getWindowThreadProcessIdByClass("Solitaire").getValue();
		System.out.println("PID: " + pid);
		if (pid == 0) {
			throw new RuntimeException("Process with window class not found: Solitaire");
		}
		HANDLE process = openProcess(
				PROCESS_QUERY_INFORMATION | PROCESS_VM_READ | PROCESS_VM_WRITE | PROCESS_VM_OPERATION, pid);
		long baseAddress = getBaseAddress(process);
		if (baseAddress == -1) {
			throw new RuntimeException("Error while finding base address.");
		}
		System.out.println("Base adress is: 0x" + Long.toHexString(baseAddress));
		System.out.println("Offsetting base: 0x" + Long.toHexString(baseAddress) + " + 0xBAFA8 = " + Long.toHexString(baseAddress + 0xBAFA8));
		baseAddress += 0xBAFA8;
		long dynAddress = findDynAddress(process, offsets, baseAddress);
		System.out.println("DynAddress: 0x" + Long.toHexString(dynAddress));

		Memory scoreMem = Kernel32Util.readMemory(process, dynAddress, 4);
		int score = scoreMem.getInt(0);
		System.out.println("Score: " + score);
		
		byte[] newScore = new byte[] { 0x22, 0x22, 0x22, 0x22 };
		Kernel32Util.writeMemory(process, dynAddress, newScore);
		
		scoreMem = Kernel32Util.readMemory(process, dynAddress, 4);
		score = scoreMem.getInt(0);
		System.out.println("New score: " + score);
	}

	public static HANDLE openProcess(int permissions, int pid) {
		HANDLE process = Kernel32Util.openProcess(permissions, true, pid);
		return process;
	}

	public static long findDynAddress(HANDLE process, int[] offsets, long baseAddress) {
		int size = Pointer.SIZE;
		Memory pTemp = new Memory(size);
		long pointerAddress = -1;
		
		for (int i = 0; i < offsets.length; i++) {
			if (i == 0) {
				pTemp = Kernel32Util.readMemory(process, baseAddress, size);
				System.out.println(">Finding dyn address...");
				System.out.println("\tStarting at [0x" + Long.toHexString(baseAddress) + "] = 0x" + Long.toHexString(pTemp.getInt(0)));
			}
			
			pointerAddress = pTemp.getInt(0) + offsets[i];
			
			if (i != offsets.length - 1) {
				System.out.print("\tNext is [0x" + Long.toHexString(pTemp.getInt(0)) + " + 0x" + Long.toHexString(offsets[i]) + " = 0x" + Long.toHexString(pointerAddress) + "] =");
				pTemp = Kernel32Util.readMemory(process, pointerAddress, size);
				System.out.println(" 0x" + Long.toHexString(pTemp.getInt(0)));
			}
		}
		
		System.out.println(">Found dyn address 0x" +  Long.toHexString(pTemp.getInt(0)) + " + 0x" + Long.toHexString(offsets[offsets.length - 1]) + " = 0x" + Long.toHexString(pointerAddress));

		return pointerAddress;
	}
	
	public static long getBaseAddress(HANDLE process) {
		try {
			// TODO 64 or 32? 64 seems to yield wrong addresses, 32 yields error 299...
			// Solitaire seems to be a 64bit application on 64bit windows.
			List<HMODULE> hModulePointer = PsapiUtil.enumProcessModules(process);

			for (HMODULE hModule : hModulePointer) {
				Module module = new Module(process, hModule);
				System.out.println("Name: " + module.getFileName());
				if (module.getFileName().contains("Solitaire.exe")) {
					System.out.println((module.getFileName() + ": entry point at - 0x"
							+ Long.toHexString(Pointer.nativeValue(module.getEntryPoint()))));
					System.out.println("Base of dll : " + module.getLpBaseOfDll());
					System.out.println("0x" + Long.toHexString(Pointer.nativeValue(module.getLpBaseOfDll())));
					return Long.valueOf("" + Pointer.nativeValue(module.getLpBaseOfDll()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
}
