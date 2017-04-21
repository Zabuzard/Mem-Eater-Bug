package de.zabuza.memeaterbug.winapi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sun.jna.platform.win32.WinDef.HWND;

/**
 * List that holds {@link de.zabuza.memeaterbug.winapi.Process Process} objects.
 * Implemented as linked and hashed map.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class ProcessList implements Iterable<Process> {
	/**
	 * Data structure that provides a fast access to the stored objects by
	 * iterating.
	 */
	private final List<Process> mList = new LinkedList<>();
	/**
	 * Data structure that provides a fast direct access to the stored objects
	 * by their process ids.
	 */
	private final Map<Integer, Process> mMap = new HashMap<>();

	/**
	 * Adds a window handle to an already added process.
	 * 
	 * @param pid
	 *            Id of the process to add the handle to
	 * @param hWnd
	 *            Handle of the window to add
	 */
	public void add(final int pid, final HWND hWnd) {
		this.mMap.get(Integer.valueOf(pid)).addHwnd(hWnd);
	}

	/**
	 * Adds a given process to the list.
	 * 
	 * @param process
	 *            Process to add
	 */
	public void add(final Process process) {
		this.mMap.put(Integer.valueOf(process.getPid()), process);
		this.mList.add(process);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Process> iterator() {
		return this.mList.iterator();
	}

	/**
	 * Gets the size of the list.
	 * 
	 * @return The size of the list
	 */
	public int size() {
		return this.mList.size();
	}

}
