package de.zabuza.memeaterbug.injection;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;

/**
 * Injection agent that gets dynamically loaded by an Injector into the JVM of a
 * target application at runtime.<br/>
 * <br/>
 * Remark: Some constants are duplicated here to easily create an independent
 * jar that just contains this class.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class Injection {

	/**
	 * Masks that separates the passed arguments.
	 */
	public static final String ARG_SEPARATOR = ",";
	/**
	 * Thrown when the given arguments object has too less arguments.
	 */
	private static final String ERROR_ARG_LENGTH = "The given arguments must at least be two where the first one is the Thread-object to start and the second one is the pid of the injected JVM.";
	/**
	 * Thrown when the given class object is no subtype of Thread.
	 */
	private static final String ERROR_NO_THREAD = "The class object, given by name, must be a subtype of Thread.";

	/**
	 * JVM hook to dynamically load this agent at runtime. It starts a custom
	 * {@link Thread} object that must be given as first argument. Additional
	 * arguments are passed to the thread if it specifies a constructor that
	 * accepts a String-array, else the default constructor gets called.
	 * 
	 * @param args
	 *            Passed arguments, separated by {@link #ARG_SEPARATOR}. The
	 *            first argument is assumed to be the full class name of a
	 *            {@link Thread} object to start. The second argument is assumed
	 *            to be the process id of the injected virtual machine. The
	 *            remaining arguments are custom arguments provided by the
	 *            caller of the injection-method. If the passed Thread object
	 *            has a constructor that accepts a String array, String[], then
	 *            the arguments are passed to it, else the default constructor
	 *            gets called.
	 * @param inst
	 *            Object used for ByteCode manipulation
	 */
	public static void agentmain(final String args, final Instrumentation inst) {
		try {
			if (args == null || args.length() <= 0) {
				throw new IllegalArgumentException(ERROR_ARG_LENGTH);
			}
			String[] argsArray = args.split(ARG_SEPARATOR);
			if (argsArray.length < 2) {
				throw new IllegalArgumentException(ERROR_ARG_LENGTH);
			}

			String threadClassName = argsArray[0];
			Class<?> threadClass = Class.forName(threadClassName);
			if (threadClass.isAssignableFrom(Thread.class)) {
				throw new IllegalArgumentException(ERROR_NO_THREAD);
			}

			boolean useDefaultConstructor = false;
			Constructor<?> threadConstructor = null;
			try {
				threadConstructor = threadClass.getConstructor(String[].class);
			} catch (NoSuchMethodException e) {
				threadConstructor = threadClass.getConstructor();
				useDefaultConstructor = true;
			}

			Object threadObject = null;
			if (useDefaultConstructor) {
				threadObject = threadConstructor.newInstance();
			} else {
				threadObject = threadConstructor.newInstance(new Object[] { argsArray });
			}

			Thread thread = (Thread) threadObject;
			thread.start();
		} catch (Exception e) {
			// Catch and print every exception as they would otherwise be
			// ignored in an agentmain method
			e.printStackTrace();
		}
	}

	/**
	 * Utility class. No implementation.
	 */
	private Injection() {

	}
}
