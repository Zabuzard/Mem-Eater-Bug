package de.zabuza.memeaterbug.examples;

import de.zabuza.memeaterbug.MemEaterBug;
import de.zabuza.memeaterbug.memory.MemManipulator;

/**
 * Hack for the popular game Solitaire that allows arbitrary changes of the user
 * score.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class SoliScorer {
	/**
	 * Demonstrates the usage of the Mem-Eater-Bug by reading and changing the
	 * user score in the popular game Solitaire.<br/>
	 * <br/>
	 * The program was tested on a Windows 10 64-bit system using the default
	 * 64-bit Solitaire.exe from Windows 7.
	 * 
	 * @param args
	 *            Not supported
	 */
	public static void main(final String[] args) {
		// Constants
		final String exeFileName = "Solitaire.exe";
		final int nextScore = 1500;
		// Offsets are found with a MemReader like CheatEngine
		final int scoreBaseAddressOffset = 0xBAFA8;
		final int[] scoreOffsets = new int[] { 0x50, 0x14 };

		// Hook to the game
		final MemEaterBug memEaterBug = new MemEaterBug(exeFileName);
		memEaterBug.hookProcess();
		final MemManipulator memManipulator = memEaterBug.getMemManipulator();

		// Find the dynamic address of the score
		final long scoreBaseAddress = memManipulator.getBaseAddress() + scoreBaseAddressOffset;
		final long scoreDynAddress = memManipulator.findDynAddress(scoreOffsets, scoreBaseAddress);

		// Read the current score
		final int score = memManipulator.readInt(scoreDynAddress);
		System.out.println("The current score is: " + score);

		// Write to the current score
		memManipulator.writeInt(scoreDynAddress, nextScore);
		final int readScore = memManipulator.readInt(scoreDynAddress);
		System.out.println("Now the score is: " + readScore);

		// Unhook from the game
		memEaterBug.unhookProcess();
	}

	/**
	 * Utility class. No implementation.
	 */
	private SoliScorer() {

	}
}
