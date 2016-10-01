package de.zabuza.memeaterbug.winapi.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * JNA interface for Windows USER32.DLL, which implements the Windows USER
 * component that creates and manipulates the standard elements of the Windows
 * user interface, such as the desktop, windows, and menus. It thus enables
 * programs to implement a graphical user interface (GUI) that matches the
 * Windows look and feel. Programs call functions from Windows USER to perform
 * operations such as creating and managing windows, receiving window messages
 * (which are mostly user input such as mouse and keyboard events, but also
 * notifications from the operating system), displaying text in a window, and
 * displaying message boxes.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public interface User32 extends com.sun.jna.platform.win32.User32 {

	/**
	 * Flag for retrieving the parent window. This does not include the owner,
	 * as it does with the GetParent function.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633502(v=vs.85).aspx">
	 *      MSDN webpage#GetAncestor function</a>
	 */
	public static final int GA_PARENT = 1;
	/**
	 * Flag for retrieving the root window by walking the chain of parent
	 * windows.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633502(v=vs.85).aspx">
	 *      MSDN webpage#GetAncestor function</a>
	 */
	public static final int GA_ROOT = 2;
	/**
	 * Flag for retrieving the owned root window by walking the chain of parent
	 * and owner windows returned by GetParent.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633502(v=vs.85).aspx">
	 *      MSDN webpage#GetAncestor function</a>
	 */
	public static final int GA_ROOTOWNER = 3;

	/**
	 * Flag for retrieving the size, in bytes, of the extra memory associated
	 * with the class.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633588(v=vs.85).aspx">
	 *      MSDN webpage#SetClassLong function</a>
	 */
	public static final int GCL_CBCLSEXTRA = -20;

	/**
	 * Flag for retrieving the size, in bytes, of the extra window memory
	 * associated with each window in the class.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633588(v=vs.85).aspx">
	 *      MSDN webpage#SetClassLong function</a>
	 */
	public static final int GCL_CBWNDEXTRA = -18;
	/**
	 * Flag for retrieving a handle to the background brush associated with the
	 * class.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633588(v=vs.85).aspx">
	 *      MSDN webpage#SetClassLong function</a>
	 */
	public static final int GCL_HBRBACKGROUND = -10;
	/**
	 * Flag for retrieving a handle to the cursor associated with the class.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633588(v=vs.85).aspx">
	 *      MSDN webpage#SetClassLong function</a>
	 */
	public static final int GCL_HCURSOR = -12;
	/**
	 * Flag for retrieving a handle to the icon associated with the class.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633588(v=vs.85).aspx">
	 *      MSDN webpage#SetClassLong function</a>
	 */
	public static final int GCL_HICON = -14;
	/**
	 * Flag for retrieving a handle to the small icon associated with the class.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633588(v=vs.85).aspx">
	 *      MSDN webpage#SetClassLong function</a>
	 */
	public static final int GCL_HICONSM = -34;
	/**
	 * Flag for retrieving a handle to the module that registered the class.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633588(v=vs.85).aspx">
	 *      MSDN webpage#SetClassLong function</a>
	 */
	public static final int GCL_HMODULE = -16;
	/**
	 * Flag for retrieving the address of the menu name string. The string
	 * identifies the menu resource associated with the class.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633588(v=vs.85).aspx">
	 *      MSDN webpage#SetClassLong function</a>
	 */
	public static final int GCL_MENUNAME = -8;
	/**
	 * Flag for retrieving the window-class style bits.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633588(v=vs.85).aspx">
	 *      MSDN webpage#SetClassLong function</a>
	 */
	public static final int GCL_STYLE = -26;
	/**
	 * Flag for retrieving the address of the window procedure, or a handle
	 * representing the address of the window procedure. You must use the
	 * CallWindowProc function to call the window procedure.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633588(v=vs.85).aspx">
	 *      MSDN webpage#SetClassLong function</a>
	 */
	public static final int GCL_WNDPROC = -24;
	/**
	 * Flag for retrieving an ATOM value that uniquely identifies the window
	 * class. This is the same atom that the RegisterClassEx function returns.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633588(v=vs.85).aspx">
	 *      MSDN webpage#SetClassLong function</a>
	 */
	public static final int GCW_ATOM = -32;

	/**
	 * Instance of the User32.dll JNA interface.
	 */
	public User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

	/**
	 * Copies the specified icon from another module to the current module.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms648058(v=vs.85).aspx">
	 *      MSDN webpage#CopyIcon function</a>
	 * @param hIcon
	 *            A handle to the icon to be copied.
	 * @return If the function succeeds, the return value is a handle to the
	 *         duplicate icon.<br/>
	 *         <br/>
	 *         If the function fails, the return value is <tt>null</tt>. To get
	 *         extended error information, call {@link #Native.GetLastError()}.
	 */
	public HICON CopyIcon(final HICON hIcon);

	/**
	 * Retrieves a handle to the top-level window whose class name and window
	 * name match the specified strings. This function does not search child
	 * windows. This function does not perform a case-sensitive search.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633499(v=vs.85).aspx">
	 *      MSDN webpage#FindWindow function</a>
	 * @param lpClassName
	 *            The class name can be any name registered with RegisterClass
	 *            or RegisterClassEx, or any of the predefined control-class
	 *            names.<br/>
	 *            <br/>
	 *            If lpClassName is <tt>null</tt>, it finds any window whose
	 *            title matches the lpWindowName parameter.
	 * @param lpWindowName
	 *            The window name (the window's title). If this parameter is
	 *            <tt>null</tt>, all window names match.
	 * @return If the function succeeds, the return value is a handle to the
	 *         window that has the specified class name and window name.<br/>
	 *         <br/>
	 *         If the function fails, the return value is <tt>null</tt>. To get
	 *         extended error information, call {@link #Native.GetLastError()}.
	 */
	public Pointer FindWindowA(final String lpClassName, final String lpWindowName);

	/**
	 * Retrieves the handle to the ancestor of the specified window.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633502(v=vs.85).aspx">
	 *      MSDN webpage#GetAncestor function</a>
	 * @param hwnd
	 *            A handle to the window whose ancestor is to be retrieved. If
	 *            this parameter is the desktop window, the function returns
	 *            <tt>null</tt>.
	 * @param gaFlags
	 *            The ancestor to be retrieved. This parameter can be one of the
	 *            following values.
	 *            <ul>
	 *            <li>{@link #GA_PARENT}</li>
	 *            <li>{@link #GA_ROOT}</li>
	 *            <li>{@link #GA_ROOTOWNER}</li>
	 *            </ul>
	 * @return The return value is the handle to the ancestor window.
	 */
	public HWND GetAncestor(final HWND hwnd, final int gaFlags);

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
	 *            <li>{@link #GCW_ATOM}</li>
	 *            <li>{@link #GCL_CBCLSEXTRA}</li>
	 *            <li>{@link #GCL_CBWNDEXTRA}</li>
	 *            <li>{@link #GCL_HBRBACKGROUND}</li>
	 *            <li>{@link #GCL_HCURSOR}</li>
	 *            <li>{@link #GCL_HICON}</li>
	 *            <li>{@link #GCL_HICONSM}</li>
	 *            <li>{@link #GCL_HMODULE}</li>
	 *            <li>{@link #GCL_MENUNAME}</li>
	 *            <li>{@link #GCL_STYLE}</li>
	 *            <li>{@link #GCL_WNDPROC}</li>
	 *            </ul>
	 * @return If the function succeeds, the return value is the requested
	 *         value.<br/>
	 *         <br/>
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call {@link #Native.GetLastError()}.
	 */
	public int GetClassLong(final HWND hWnd, final int nIndex);

	/**
	 * Retrieves the identifier of the thread that created the specified window
	 * and, optionally, the identifier of the process that created the window.
	 * 
	 * @see <a href=
	 *      "https://msdn.microsoft.com/en-us/library/ms633522(v=vs.85).aspx">
	 *      MSDN webpage#GetWindowThreadProcessId function</a>
	 * 
	 * @param hWnd
	 *            A handle to the window.
	 * @param lpdwProcessId
	 *            A pointer to a variable that receives the process identifier.
	 *            If this parameter is not <tt>null</tt>,
	 *            GetWindowThreadProcessId copies the identifier of the process
	 *            to the variable; otherwise, it does not.
	 * @return The return value is the identifier of the thread that created the
	 *         window.
	 */
	public int GetWindowThreadProcessId(final Pointer hWnd, final IntByReference lpdwProcessId);
}
