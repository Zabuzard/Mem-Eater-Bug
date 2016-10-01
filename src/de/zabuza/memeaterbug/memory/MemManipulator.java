package de.zabuza.memeaterbug.memory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import de.zabuza.memeaterbug.winapi.Process;
import de.zabuza.memeaterbug.winapi.jna.util.Kernel32Util;
import de.zabuza.memeaterbug.winapi.jna.util.PsapiUtil;

/**
 * Provides various methods for memory manipulation of a given process.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class MemManipulator {

	/**
	 * The process this object belongs to.
	 */
	private final Process mProcess;

	/**
	 * Creates a new object that is able to manipulate the memory of the given
	 * process.
	 * 
	 * @param processId
	 *            Id of the process to manipulate
	 */
	public MemManipulator(final int processId) {
		this(processId, null);
	}

	/**
	 * Creates a new object that is able to manipulate the memory of the given
	 * process.
	 * 
	 * @param processId
	 *            Id of the process to manipulate
	 * @param processHandle
	 *            An optional previously created handle object that must
	 *            correspond to the same process that is specified by processId.
	 *            Using <tt>null</tt> results in the creation of a default
	 *            handle, that has all access rights.
	 */
	public MemManipulator(final int processId, final HANDLE processHandle) {
		mProcess = PsapiUtil.getProcessById(processId);
		if (processHandle != null) {
			mProcess.setHandle(processHandle);
		}
	}

	/**
	 * Finds a dynamic address given by its offsets from the process base
	 * address. It is assumed that the value stored at the base address is a
	 * pointer. It is also assumed that the pointer received by this stored
	 * pointer plus the first offset stores another pointer. The same principle
	 * must hold for all offsets. The resulting dynamic address is the pointer
	 * stored at this location.
	 * 
	 * @param offsets
	 *            Offsets that need to be followed
	 * @return The dynamic address received by following all offsets starting at
	 *         the base address
	 */
	public long findDynAddress(final int[] offsets) {
		return findDynAddress(offsets, getBaseAddress());
	}

	/**
	 * Finds a dynamic address given by its offsets from the given starting
	 * address. It is assumed that the value stored at the base address is a
	 * pointer. It is also assumed that the pointer received by this stored
	 * pointer plus the first offset stores another pointer. The same principle
	 * must hold for all offsets. The resulting dynamic address is the pointer
	 * stored at this location.
	 * 
	 * @param offsets
	 *            Offsets that need to be followed
	 * @param startingAddress
	 *            Address from which to start following the offsets
	 * @return The dynamic address received by following all offsets starting at
	 *         the given starting address
	 */
	public long findDynAddress(final int[] offsets, final long startingAddress) {
		int size = Pointer.SIZE;
		Memory pTemp = new Memory(size);
		long pointerAddress = -1;

		for (int i = 0; i < offsets.length; i++) {
			if (i == 0) {
				pTemp = Kernel32Util.readMemory(mProcess.getHandle(), startingAddress, size);
			}

			pointerAddress = pTemp.getInt(0) + offsets[i];

			if (i != offsets.length - 1) {
				pTemp = Kernel32Util.readMemory(mProcess.getHandle(), pointerAddress, size);
			}
		}
		return pointerAddress;
	}

	/**
	 * Gets the load address of this process module.
	 * 
	 * @return The load address of this process module
	 */
	public long getBaseAddress() {
		return Pointer.nativeValue(mProcess.getBase());
	}

	/**
	 * Reads an integer from the given address.
	 * 
	 * @param address
	 *            Address to start reading from
	 * @return The integer read from the given address.
	 */
	public int readInt(final long address) {
		Memory value = readMemory(address, MemSize.getIntSize());
		return value.getInt(0);
	}

	/**
	 * Reads a number of bytes starting from a given address.
	 * 
	 * @param address
	 *            Address to start reading from
	 * @param bytesToRead
	 *            Number of bytes to read
	 * @return Object holding the read bytes
	 */
	public Memory readMemory(final long address, final int bytesToRead) {
		return Kernel32Util.readMemory(mProcess.getHandle(), address, bytesToRead);
	}

	/**
	 * Reads a string from the given address using the default platform
	 * encoding.
	 * 
	 * @param address
	 *            Address to start reading from
	 * @param size
	 *            The size of the string to read, in bytes
	 * @return The string read from the given address
	 */
	public String readString(final long address, final int size) {
		Memory output = Kernel32Util.readMemory(mProcess.getHandle(), address, size);
		return output.getString(0);
	}

	/**
	 * Reads a string from the given address using the given encoding.
	 * 
	 * @param address
	 *            Address to start reading from
	 * @param encoding
	 *            Encoding to use
	 * @param length
	 *            Length of the string to read, as amount of characters
	 * @param sizeOfOneChar
	 *            The size of one character in the given encoding, in bytes
	 * @return The string read from the given address
	 * @throws UnsupportedEncodingException
	 *             If the given encoding is not supported
	 */
	public String readString(final long address, final String encoding, final int length, final int sizeOfOneChar)
			throws UnsupportedEncodingException {
		if (!Charset.availableCharsets().keySet().contains(encoding)) {
			throw new UnsupportedEncodingException();
		}
		Memory output = Kernel32Util.readMemory(mProcess.getHandle(), address, sizeOfOneChar * length);
		return output.getString(0, encoding);
	}

	/**
	 * Writes the given integer to the given address.
	 * 
	 * @param address
	 *            The address to write at
	 * @param valueToWrite
	 *            The value to write
	 */
	public void writeInt(final long address, final int valueToWrite) {
		byte[] bytesToWriteReversed = ByteBuffer.allocate(MemSize.getIntSize()).putInt(valueToWrite).array();
		writeMemoryReversely(address, bytesToWriteReversed);
	}

	/**
	 * Writes the given bytes to the given address.
	 * 
	 * @param address
	 *            The address to write at
	 * @param bytesToWrite
	 *            The bytes to write. Read from left to right, i.e. from the
	 *            lower to the higher indices.
	 */
	public void writeMemory(final long address, final byte[] bytesToWrite) {
		Kernel32Util.writeMemory(mProcess.getHandle(), address, bytesToWrite);
	}

	/**
	 * Writes the given bytes reversely to the given address.
	 * 
	 * @param address
	 *            The address to write at
	 * @param bytesToWrite
	 *            The bytes to write reversely. Read from right to left, i.e.
	 *            from the higher to the lower indices.
	 */
	public void writeMemoryReversely(final long address, final byte[] bytesToWrite) {
		Kernel32Util.writeMemoryReversely(mProcess.getHandle(), address, bytesToWrite);
	}

	/**
	 * Writes the given string to the given address using the default platform
	 * encoding.
	 * 
	 * @param address
	 *            The address to write at
	 * @param toWrite
	 *            The string to write
	 */
	public void writeString(final long address, final String toWrite) {
		writeMemory(address, toWrite.getBytes());
	}

	/**
	 * Writes the given string to the given address using the given encoding.
	 * 
	 * @param address
	 *            The address to write at
	 * @param toWrite
	 *            The string to write
	 * @param encoding
	 *            The encoding to use for decoding the string
	 * @throws UnsupportedEncodingException
	 *             If the given encoding is not supported
	 */
	public void writeString(final long address, final String toWrite, final String encoding)
			throws UnsupportedEncodingException {
		writeMemory(address, toWrite.getBytes(encoding));
	}
}
