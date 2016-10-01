package de.zabuza.memeaterbug.examples;

import de.zabuza.memeaterbug.MemEaterBug;
import de.zabuza.memeaterbug.injection.Injector;

/**
 * Hack for the popular game Space Invaders that displays various information on
 * the game screen by jar-injection.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class SpaceInvadersInjector {
	/**
	 * Demonstrates the usage of the Mem-Eater-Bug for jar-injection by
	 * displaying various information on the game screen of the game Space
	 * Invaders.<br/>
	 * <br/>
	 * The program was tested on a Windows 10 64-bit system using the provided
	 * SpaceInvaders.jar file.
	 * 
	 * @param args
	 *            Not supported
	 */
	public static void main(final String[] args) {
		// Constants
		String windowTitle = "Space Invaders 101";
		String pathToInjectionAgent = "C:\\Users\\Zabuza\\Desktop\\SpaceInvadersInjection.jar";
		String injectionClassName = "de.zabuza.memeaterbug.examples.SpaceInvadersInjection";

		// Hook to the game
		MemEaterBug memEaterBug = new MemEaterBug(null, windowTitle);
		memEaterBug.hookProcess();
		Injector injector = memEaterBug.getInjector();

		// Inject the agent jar into the target jar
		injector.injectJarIntoJar(pathToInjectionAgent, injectionClassName);

		// Unhook from the game
		memEaterBug.unhookProcess();
	}
	
	/**
	 * Utility class. No implementation.
	 */
	private SpaceInvadersInjector() {

	}
}
