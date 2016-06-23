package de.zabuza.memeaterbug.winapi.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

/**
 * JNA interface for Windows KERNEL32.DLL, which exposes to applications most of
 * the Win32 base APIs, such as memory management, input/output (I/O)
 * operations, process and thread creation, and synchronization functions.
 * 
 * @author Zabuza
 *
 */
public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {
	/**
	 * Instance of the Kernel32.dll JNA interface.
	 */
	public Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

	/**
	 * Reads data from an area of memory in a specified process. The entire area
	 * to be read must be accessible or the operation fails.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms680553(v=vs.85).aspx">
	 *      MSDN webpage#ReadProcessMemory function</a>
	 * 
	 * @param hProcess
	 *            A handle to the process with memory that is being read. The
	 *            handle must have
	 *            {@link de.zabuza.memeaterbug.winapi.Process#PROCESS_VM_READ
	 *            PROCESS_VM_READ} access to the process.
	 * @param lpBaseAddress
	 *            The base address in the specified process from which to read.
	 *            Before any data transfer occurs, the system verifies that all
	 *            data in the base address and memory of the specified size is
	 *            accessible for read access, and if it is not accessible the
	 *            function fails.
	 * @param lpBuffer
	 *            A pointer to a buffer that receives the contents from the
	 *            address space of the specified process.
	 * @param nSize
	 *            The number of bytes to be read from the specified process.
	 * @param lpNumberOfBytesRead
	 *            A pointer to a variable that receives the number of bytes
	 *            transferred into the specified buffer. If lpNumberOfBytesRead
	 *            is <tt>null</tt>, the parameter is ignored.
	 * @return If the function succeeds, the return value is nonzero.<br/>
	 *         <br/>
	 *         If the function fails, the return value is 0 (zero). To get
	 *         extended error information, call {@link #Native.GetLastError()}.
	 *         <br/>
	 *         <br/>
	 *         The function fails if the requested read operation crosses into
	 *         an area of the process that is inaccessible.
	 * 
	 */
	public boolean ReadProcessMemory(final HANDLE hProcess, final long lpBaseAddress, final Pointer lpBuffer,
			final int nSize, final IntByReference lpNumberOfBytesRead);

	/**
	 * Writes data to an area of memory in a specified process. The entire area
	 * to be written to must be accessible or the operation fails.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms681674(v=vs.85).aspx">
	 *      MSDN webpage#WriteProcessMemory function</a>
	 * 
	 * @param hProcess
	 *            A handle to the process memory to be modified. The handle must
	 *            have
	 *            {@link de.zabuza.memeaterbug.winapi.Process#PROCESS_VM_WRITE
	 *            PROCESS_VM_WRITE} and
	 *            {@link de.zabuza.memeaterbug.winapi.Process#PROCESS_VM_OPERATION
	 *            PROCESS_VM_OPERATION} access to the process.
	 * @param lpBaseAddress
	 *            The base address in the specified process to which data is
	 *            written. Before data transfer occurs, the system verifies that
	 *            all data in the base address and memory of the specified size
	 *            is accessible for write access, and if it is not accessible,
	 *            the function fails.
	 * @param lpBuffer
	 *            A pointer to the buffer that contains data to be written in
	 *            the address space of the specified process.
	 * @param nSize
	 *            The number of bytes to be written to the specified process.
	 * @param lpNumberOfBytesWritten
	 *            A pointer to a variable that receives the number of bytes
	 *            transferred into the specified process. This parameter is
	 *            optional. If lpNumberOfBytesWritten is <tt>null</tt>, the
	 *            parameter is ignored.
	 * @return If the function succeeds, the return value is nonzero.<br/>
	 *         <br/>
	 *         If the function fails, the return value is 0 (zero). To get
	 *         extended error information, call {@link #Native.GetLastError()}.
	 *         The function fails if the requested write operation crosses into
	 *         an area of the process that is inaccessible.
	 */
	public boolean WriteProcessMemory(final HANDLE hProcess, final long lpBaseAddress, final Pointer lpBuffer,
			final int nSize, final IntByReference lpNumberOfBytesWritten);
}
