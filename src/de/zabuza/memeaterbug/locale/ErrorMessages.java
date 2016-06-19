package de.zabuza.memeaterbug.locale;

/**
 * Utility class that provides error messages for the Mem-Eater-Bug.
 * 
 * @author Zabuza
 * 
 */
public final class ErrorMessages {
	/**
	 * Thrown when trying to create a {@link de.zabuza.memeaterbug.MemEaterBug
	 * MemEaterBug} with an operating system that is not Windows.
	 */
	public static final String OS_IS_NOT_WINDOWS = "Mem-Eater-Bug can only be used on a Windows operating system: ";
	/**
	 * Thrown when a {@link de.zabuza.memeaterbug.MemEaterBug MemEaterBug} tries
	 * to find its corresponding process, but the given process id was invalid.
	 */
	public static final String PROCESS_ID_INVALID = "Process id must be greater zero: ";
	/**
	 * Thrown when a {@link de.zabuza.memeaterbug.MemEaterBug MemEaterBug} tries
	 * to find its corresponding process, but was unable to do so.
	 */
	public static final String PROCESS_NOT_FOUND = "Process was not found.";
	/**
	 * Thrown when a {@link de.zabuza.memeaterbug.MemEaterBug MemEaterBug} tries
	 * to hook to its corresponding process, but was unable to do so, since it
	 * was not already hooked to a process.
	 */
	public static final String PROCESS_UNABLE_TO_HOOK_SINCE_ALREADY_HOOKED = "Unable to hook since already hooked to a process. Unhook first, then try again.";
	/**
	 * Thrown when a {@link de.zabuza.memeaterbug.MemEaterBug MemEaterBug} tries
	 * to unhook from its corresponding process, but was unable to do so, since
	 * it was not hooked to a process.
	 */
	public static final String PROCESS_UNABLE_TO_UNHOOK_SINCE_NOT_HOOKED = "Unable to unhook since not hooked to a process.";
	/**
	 * Thrown when a method could not be executed since the
	 * {@link de.zabuza.memeaterbug.MemEaterBug MemEaterBug} was not hooked to a
	 * process.
	 */
	public static final String UNABLE_SINCE_NOT_HOOKED = "Unable to execute since not hooked to a process. First hook, then try again.";

	/**
	 * Utility class. No implementation.
	 */
	private ErrorMessages() {

	}
}
