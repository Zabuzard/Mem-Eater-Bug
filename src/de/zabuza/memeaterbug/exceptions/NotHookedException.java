package de.zabuza.memeaterbug.exceptions;

/**
 * Thrown when a method could not be executed since the
 * {@link de.zabuza.memeaterbug.MemEaterBug MemEaterBug} was not hooked to a
 * process.
 * 
 * @author Zabuza
 *
 */
public final class NotHookedException extends RuntimeException {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception without a detailed description.
	 */
	public NotHookedException() {
		super();
	}

	/**
	 * Creates a new exception with a given description.
	 * 
	 * @param description
	 *            Description of the exception
	 */
	public NotHookedException(final String description) {
		super(description);
	}

}
