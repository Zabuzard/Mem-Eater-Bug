package de.zabuza.memeaterbug.examples;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.lang.reflect.Field;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;

/**
 * Hack for the popular game Space Invaders that displays various information on
 * the game screen by jar-injection.<br/>
 * <br/>
 * This is the thread that gets started by
 * {@link de.zabuza.memeaterbug.injection.Injection Injection} after injecting
 * the Space Invaders jar-file.
 * 
 * @author Zabuza
 *
 */
public final class SpaceInvadersInjection extends Thread {
	/**
	 * Overlay panel that gets placed on top of the JFrame. It displays
	 * information of the game which is not there by default.
	 * 
	 * @author Zabuza
	 *
	 */
	private final class GameOverlayPanel extends JPanel {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * Amount of remaining aliens. Gets updated by
		 * {@link #updateRemainingAliens(int)}.
		 */
		private int mRemainingAliens;

		/**
		 * Creates a new overlay panel whose content can be updated with
		 * {@link #updateRemainingAliens(int)}.
		 */
		public GameOverlayPanel() {
			setBackground(Color.BLACK);
			mRemainingAliens = -1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		public void paintComponent(final Graphics g) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());

			g.setColor(Color.WHITE);
			g.drawString("Remaining Aliens: " + mRemainingAliens, 5, getHeight() / 2);
		}

		/**
		 * Updates the amount of remaining aliens which gets displayed on this
		 * panel.
		 * 
		 * @param remainingAliens
		 */
		public void updateRemainingAliens(final int remainingAliens) {
			if (mRemainingAliens != remainingAliens) {
				mRemainingAliens = remainingAliens;
				repaint();
			}
		}
	}

	/**
	 * The full name of the game class to manipulate.
	 */
	private static final String GAME_CLASS_NAME = "org.newdawn.spaceinvaders.Game";
	/**
	 * The name of the private field in the game which holds the amount of
	 * remaining aliens.
	 */
	private static final String GAME_REMAINING_ALIEN_FIELD = "alienCount";

	/**
	 * Timeout to wait in the logic loop, in milliseconds.
	 */
	private static final int TIMEOUT = 50;

	/**
	 * Title of the JFrame to manipulate.
	 */
	private static final String WINDOW_TITLE = "Space Invaders 101";

	/**
	 * Gets called by {@link de.zabuza.memeaterbug.injection.Injection
	 * Injection} after injecting the Space Invaders jar-file.
	 */
	public SpaceInvadersInjection() {
		this(null);
	}

	/**
	 * Gets called by {@link de.zabuza.memeaterbug.injection.Injection
	 * Injection} after injecting the Space Invaders jar-file.
	 * 
	 * @param args
	 *            Additional arguments, not used
	 */
	public SpaceInvadersInjection(final String[] args) {
		// Nothing to do here, we want to do our stuff in a new thread
		// environment to not slow down the injector.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// We start in a static context and need to access the game instances
		Frame[] frames = Frame.getFrames();
		JFrame gameFrame = null;
		for (Frame frame : frames) {
			if (frame.getTitle().equalsIgnoreCase(WINDOW_TITLE) && frame instanceof JFrame) {
				gameFrame = (JFrame) frame;
				break;
			}
		}

		GameOverlayPanel overlay = null;
		Component gameComponent = null;

		Class<?> gameClass = null;
		try {
			ClassLoader classLoader = SpaceInvadersInjection.class.getClassLoader();
			if (classLoader == null) {
				throw new IllegalStateException("Can not find ClassLoader.");
			}
			gameClass = classLoader.loadClass(GAME_CLASS_NAME);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		boolean foundGameElement = false;

		// Frame found, find the LayeredPane which holds the content
		if (gameFrame != null) {
			for (Component compOne : gameFrame.getComponents()) {
				if (compOne instanceof JRootPane) {
					JRootPane rootPane = (JRootPane) compOne;
					for (Component compTwo : rootPane.getComponents()) {
						if (compTwo instanceof JLayeredPane) {
							JLayeredPane rootLayeredPane = (JLayeredPane) compTwo;
							for (Component compThree : rootLayeredPane.getComponents()) {
								if (compThree instanceof JPanel) {
									JPanel rootPanel = (JPanel) compThree;
									for (Component compFour : rootPanel.getComponents()) {
										if (compFour instanceof JLayeredPane) {
											// Here is the content of the frame
											JLayeredPane layeredPane = (JLayeredPane) compFour;
											// Search for the games content
											// canvas
											for (Component compFive : layeredPane.getComponents()) {
												if (gameClass != null && gameClass.isInstance(compFive)) {
													gameComponent = compFive;

													// Add the overlay
													overlay = new GameOverlayPanel();
													overlay.setBounds(0, 0, gameComponent.getWidth(), 40);
													overlay.setOpaque(true);
													layeredPane.add(overlay, new Integer(1), 0);

													foundGameElement = true;
												}
												if (foundGameElement) {
													break;
												}
											}
										}
										if (foundGameElement) {
											break;
										}
									}
								}
								if (foundGameElement) {
									break;
								}
							}
						}
						if (foundGameElement) {
							break;
						}
					}
				}
				if (foundGameElement) {
					break;
				}
			}
			gameFrame.pack();
		}

		// Logic loop
		while (true) {
			// Update the value and pass it to the overlay
			try {
				// Access the private field of the game object
				if (gameClass != null) {
					Field field = gameClass.getDeclaredField(GAME_REMAINING_ALIEN_FIELD);
					field.setAccessible(true);
					int remainingAliens = field.getInt(gameComponent);
					overlay.updateRemainingAliens(remainingAliens);
				}

				sleep(TIMEOUT);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
					| InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
