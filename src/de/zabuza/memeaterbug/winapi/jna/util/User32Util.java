package de.zabuza.memeaterbug.winapi.jna.util;

import java.awt.image.BufferedImage;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFOHEADER;
import com.sun.jna.platform.win32.WinGDI.ICONINFO;
import com.sun.jna.ptr.IntByReference;

import de.zabuza.memeaterbug.winapi.jna.User32;

import com.sun.jna.platform.win32.WinUser;

/**
 * Provides various utility methods that use the JNA interface for Windows
 * USER32.DLL, which implements the Windows USER component that creates and
 * manipulates the standard elements of the Windows user interface, such as the
 * desktop, windows, and menus. It thus enables programs to implement a
 * graphical user interface (GUI) that matches the Windows look and feel.
 * Programs call functions from Windows USER to perform operations such as
 * creating and managing windows, receiving window messages (which are mostly
 * user input such as mouse and keyboard events, but also notifications from the
 * operating system), displaying text in a window, and displaying message boxes.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class User32Util {

	/**
	 * Size of a single information in icons.
	 */
	private static final int ICON_BYTE_SIZE = 8;
	/**
	 * Depth of an icon.
	 */
	private static final int ICON_DEPTH = 24;
	/**
	 * Size of an icon in pixel.
	 */
	private static final int ICON_SIZE = 16;
	/**
	 * Standard message timeout to use in milliseconds.
	 */
	private static final int STD_MESSAGE_TIMEOUT = 20;

	/**
	 * Retrieves a handle to the top-level window whose class name and window
	 * name match the specified strings. This function does not search child
	 * windows. This function does not perform a case-sensitive search.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633499(v=vs.85).aspx">
	 *      MSDN webpage#FindWindow function</a>
	 * @param windowClass
	 *            The class name can be any name registered with RegisterClass
	 *            or RegisterClassEx, or any of the predefined control-class
	 *            names.<br/>
	 *            <br/>
	 *            If lpClassName is <tt>null</tt>, it finds any window whose
	 *            title matches the lpWindowName parameter.
	 * @param windowTitle
	 *            The window name (the window's title). If this parameter is
	 *            <tt>null</tt>, all window names match.
	 * @return If the function succeeds, the return value is a handle to the
	 *         window that has the specified class name and window name.<br/>
	 *         <br/>
	 *         If the function fails, the return value is <tt>null</tt>. To get
	 *         extended error information, call {@link Native#getLastError()}.
	 */
	public static Pointer findWindowA(final String windowClass, final String windowTitle) {
		Pointer hWnd = User32.INSTANCE.FindWindowA(windowClass, windowTitle);
		return hWnd;
	}

	/**
	 * Gets the handle to the icon of the given window.
	 * 
	 * @param hWnd
	 *            Handle of the window in question
	 * @return A handle to the icon of the given window or <tt>null</tt> if the
	 *         operation was not successful
	 */
	public static HICON getHIcon(final HWND hWnd) {
		try {
			Pointer icon = sendMessageTimeoutA(hWnd, WinUser.WM_GETICON, WinUser.ICON_SMALL, 0, WinUser.SMTO_NORMAL,
					STD_MESSAGE_TIMEOUT);
			if (Pointer.nativeValue(icon) != 0) {
				return User32.INSTANCE.CopyIcon(new HICON(icon));
			}
		} catch (final Win32Exception e) {
			// Just catch the exception and return an error code
		}

		try {
			Pointer icon = sendMessageTimeoutA(hWnd, WinUser.WM_GETICON, WinUser.ICON_BIG, 0, WinUser.SMTO_NORMAL,
					STD_MESSAGE_TIMEOUT);
			if (Pointer.nativeValue(icon) != 0) {
				return User32.INSTANCE.CopyIcon(new HICON(icon));
			}
		} catch (final Win32Exception e) {
			// Just catch the exception and return an error code
		}

		try {
			Pointer icon = sendMessageTimeoutA(hWnd, WinUser.WM_GETICON, WinUser.ICON_SMALL2, 0, WinUser.SMTO_NORMAL,
					STD_MESSAGE_TIMEOUT);
			if (Pointer.nativeValue(icon) != 0) {
				return User32.INSTANCE.CopyIcon(new HICON(icon));
			}
		} catch (final Win32Exception e) {
			// Just catch the exception and return an error code
		}

		try {
			long hiconSM = getClassLong(hWnd, User32.GCL_HICONSM);
			if (hiconSM != 0) {
				return User32.INSTANCE.CopyIcon(new HICON(Pointer.createConstant(hiconSM)));
			}
		} catch (final Win32Exception e) {
			// Just catch the exception and return an error code
		}

		try {
			long hicon = getClassLong(hWnd, User32.GCL_HICON);
			if (hicon != 0) {
				return User32.INSTANCE.CopyIcon(new HICON(Pointer.createConstant(hicon)));
			}
		} catch (final Win32Exception e) {
			// Just catch the exception and return an error code
		}

		return null;
	}

	/**
	 * Gets the icon that corresponds to a given icon handler.
	 * 
	 * @param hIcon
	 *            Handler to the icon to get
	 * @return The icon that corresponds to a given icon handler
	 */
	public static BufferedImage getIcon(final HICON hIcon) {
		int width = ICON_SIZE;
		int height = ICON_SIZE;
		short depth = ICON_DEPTH;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Memory lpBitsColor = new Memory(width * height * depth / ICON_BYTE_SIZE);
		Memory lpBitsMask = new Memory(width * height * depth / ICON_BYTE_SIZE);
		BITMAPINFO info = new BITMAPINFO();
		BITMAPINFOHEADER hdr = new BITMAPINFOHEADER();
		info.bmiHeader = hdr;
		hdr.biWidth = width;
		hdr.biHeight = height;
		hdr.biPlanes = 1;
		hdr.biBitCount = depth;
		hdr.biCompression = WinGDI.BI_RGB;

		HDC hDC = User32.INSTANCE.GetDC(null);
		ICONINFO piconinfo = new ICONINFO();
		User32.INSTANCE.GetIconInfo(hIcon, piconinfo);

		GDI32.INSTANCE.GetDIBits(hDC, piconinfo.hbmColor, 0, height, lpBitsColor, info, WinGDI.DIB_RGB_COLORS);
		GDI32.INSTANCE.GetDIBits(hDC, piconinfo.hbmMask, 0, height, lpBitsMask, info, WinGDI.DIB_RGB_COLORS);

		int r, g, b, a, argb;
		int x = 0, y = height - 1;
		for (int i = 0; i < lpBitsColor.size(); i = i + 3) {
			b = lpBitsColor.getByte(i) & 0xFF;
			g = lpBitsColor.getByte(i + 1) & 0xFF;
			r = lpBitsColor.getByte(i + 2) & 0xFF;
			a = 0xFF - lpBitsMask.getByte(i) & 0xFF;

			argb = a << 24 | r << 16 | g << 8 | b;
			image.setRGB(x, y, argb);
			x = (x + 1) % width;
			if (x == 0) {
				y--;
			}
		}

		User32.INSTANCE.ReleaseDC(null, hDC);
		GDI32.INSTANCE.DeleteObject(piconinfo.hbmColor);
		GDI32.INSTANCE.DeleteObject(piconinfo.hbmMask);

		return image;
	}

	/**
	 * Retrieves the identifier of the thread that created the specified window
	 * and, optionally, the identifier of the process that created the window.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633522(v=vs.85).aspx">
	 *      MSDN webpage#GetWindowThreadProcessId function</a>
	 * @param hWnd
	 *            A handle to the window.
	 * @return The identifier of the thread that created the specified window
	 *         and, optionally, the identifier of the process that created the
	 *         window.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static IntByReference getWindowThreadProcessId(final Pointer hWnd) throws Win32Exception {
		IntByReference pid = new IntByReference(0);
		User32.INSTANCE.GetWindowThreadProcessId(hWnd, pid);
		if (pid.getValue() <= 0) {
			throw new Win32Exception(Native.getLastError());
		}
		return pid;
	}

	/**
	 * Retrieves the identifier of the thread that created the specified window
	 * and, optionally, the identifier of the process that created the window.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633522(v=vs.85).aspx">
	 *      MSDN webpage#GetWindowThreadProcessId function</a>
	 * @param processClassName
	 *            The class name can be any name registered with RegisterClass
	 *            or RegisterClassEx, or any of the predefined control-class
	 *            names.
	 * @return The identifier of the thread that created the specified window
	 *         and, optionally, the identifier of the process that created the
	 *         window.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static IntByReference getWindowThreadProcessIdByClass(final String processClassName) throws Win32Exception {
		return getWindowThreadProcessId(findWindowA(processClassName, null));
	}

	/**
	 * Retrieves the identifier of the thread that created the specified window
	 * and, optionally, the identifier of the process that created the window.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633522(v=vs.85).aspx">
	 *      MSDN webpage#GetWindowThreadProcessId function</a>
	 * @param windowClass
	 *            The class name can be any name registered with RegisterClass
	 *            or RegisterClassEx, or any of the predefined control-class
	 *            names.<br/>
	 *            <br/>
	 *            If lpClassName is <tt>null</tt>, it finds any window whose
	 *            title matches the lpWindowName parameter.
	 * @param windowTitle
	 *            The window name (the window's title). If this parameter is
	 *            <tt>null</tt>, all window names match.
	 * @return The identifier of the thread that created the specified window
	 *         and, optionally, the identifier of the process that created the
	 *         window.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static IntByReference getWindowThreadProcessIdByClassAndTitle(final String windowClass,
			final String windowTitle) throws Win32Exception {
		return getWindowThreadProcessId(findWindowA(windowClass, windowTitle));
	}

	/**
	 * Retrieves the identifier of the thread that created the specified window
	 * and, optionally, the identifier of the process that created the window.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633522(v=vs.85).aspx">
	 *      MSDN webpage#GetWindowThreadProcessId function</a>
	 * @param windowTitle
	 *            The window name (the window's title). If this parameter is
	 *            <tt>null</tt>, all window names match.
	 * @return The identifier of the thread that created the specified window
	 *         and, optionally, the identifier of the process that created the
	 *         window.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	public static IntByReference getWindowThreadProcessIdByTitle(final String windowTitle) throws Win32Exception {
		return getWindowThreadProcessId(findWindowA(null, windowTitle));
	}

	/**
	 * Retrieves the specified 32-bit (DWORD) value from the WNDCLASSEX
	 * structure associated with the specified window.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633580(v=vs.85).aspx">
	 *      MSDN webpage#GetClassLong function</a>
	 * @param hWnd
	 *            A handle to the window and, indirectly, the class to which the
	 *            window belongs.
	 * @param nIndex
	 *            The value to be retrieved. To retrieve a value from the extra
	 *            class memory, specify the positive, zero-based byte offset of
	 *            the value to be retrieved. Valid values are in the range zero
	 *            through the number of bytes of extra class memory, minus four;
	 *            for example, if you specified 12 or more bytes of extra class
	 *            memory, a value of 8 would be an index to the third integer.
	 *            To retrieve any other value from the WNDCLASSEX structure,
	 *            specify one of the following values.
	 *            <ul>
	 *            <li>{@link User32#GCW_ATOM}</li>
	 *            <li>{@link User32#GCL_CBCLSEXTRA}</li>
	 *            <li>{@link User32#GCL_CBWNDEXTRA}</li>
	 *            <li>{@link User32#GCL_HBRBACKGROUND}</li>
	 *            <li>{@link User32#GCL_HCURSOR}</li>
	 *            <li>{@link User32#GCL_HICON}</li>
	 *            <li>{@link User32#GCL_HICONSM}</li>
	 *            <li>{@link User32#GCL_HMODULE}</li>
	 *            <li>{@link User32#GCL_MENUNAME}</li>
	 *            <li>{@link User32#GCL_STYLE}</li>
	 *            <li>{@link User32#GCL_WNDPROC}</li>
	 *            </ul>
	 * @return The specified 32-bit (DWORD) value from the WNDCLASSEX structure
	 *         associated with the specified window.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	private static long getClassLong(final HWND hWnd, final int nIndex) throws Win32Exception {
		long ret = User32.INSTANCE.GetClassLong(hWnd, nIndex);
		if (ret == 0) {
			throw new Win32Exception(Native.getLastError());
		}
		return ret;
	}

	/**
	 * Sends the specified message to one or more windows.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms644952(v=vs.85).aspx">
	 *      MSDN webpage#SendMessageTimeout function</a>
	 * @param hWnd
	 *            A handle to the window whose window procedure will receive the
	 *            message.
	 * @param msg
	 *            The message to be sent.
	 * @param wParam
	 *            Any additional message-specific information.
	 * @param lParam
	 *            Any additional message-specific information.
	 * @param fuFlags
	 *            The behavior of this function.
	 * @param uTimeout
	 *            The duration of the time-out period, in milliseconds. If the
	 *            message is a broadcast message, each window can use the full
	 *            time-out period. For example, if you specify a five second
	 *            time-out period and there are three top-level windows that
	 *            fail to process the message, you could have up to a 15 second
	 *            delay.
	 * @return The result of the message processing. The value of this parameter
	 *         depends on the message that is specified.
	 * @throws Win32Exception
	 *             If the operation was not successful
	 */
	private static Pointer sendMessageTimeoutA(final HWND hWnd, final int msg, final int wParam, final int lParam,
			final int fuFlags, final int uTimeout) throws Win32Exception {
		DWORDByReference lpdwResult = new DWORDByReference();
		long ret = User32.INSTANCE.SendMessageTimeout(hWnd, msg, wParam, lParam, fuFlags, uTimeout, lpdwResult);
		if (ret == 0) {
			throw new Win32Exception(Native.getLastError());
		}
		return Pointer.createConstant(lpdwResult.getValue().longValue());
	}
}
