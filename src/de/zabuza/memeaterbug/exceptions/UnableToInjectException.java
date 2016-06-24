package de.zabuza.memeaterbug.exceptions;

/**
 * Thrown when an {@link de.zabuza.memeaterbug.injection.Injector Injector}
 * method could not inject code into the given process.
 * 
 * @author Zabuza
 *
 */
public final class UnableToInjectException extends RuntimeException {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception without a detailed description.
	 */
	public UnableToInjectException() {
		super();
	}

	/**
	 * Creates a new exception with a given description.
	 * 
	 * @param description
	 *            Description of the exception
	 */
	public UnableToInjectException(final String description) {
		super(description);
	}

}
