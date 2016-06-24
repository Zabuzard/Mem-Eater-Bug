package de.zabuza.memeaterbug.examples;

import java.lang.instrument.Instrumentation;

/**
 * Hack for the popular game Space Invaders that displays various information on
 * the game screen by jar-injection.<br/>
 * <br/>
 * This is the agent as jar that gets injected inside the Space Invaders
 * jar-file.
 * 
 * @author Zabuza
 *
 */
public final class SpaceInvadersInjection {

	/**
	 * JVM hook to dynamically load this agent at runtime.
	 * 
	 * @param args
	 *            Passed arguments, not supported
	 * @param inst
	 *            Object used for ByteCode manipulation
	 */
	public static void agentmain(final String args, final Instrumentation inst) {
		try {
			System.out.println("Injected the agent.");
		} catch (Exception e) {
			// Catch and print every exception as they would otherwise be
			// ignored in an agentmain method
			e.printStackTrace();
		}
	}

	/**
	 * Utility class. No implementation.
	 */
	private SpaceInvadersInjection() {

	}
}
