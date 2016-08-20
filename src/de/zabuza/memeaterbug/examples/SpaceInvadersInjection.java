package de.zabuza.memeaterbug.examples;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

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
		// TODO Rename everything, we inject SwingApp, not SpaceInvaders
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

		// Frame found, manipulate the content
		if (gameFrame != null) {
			GlassPane glassPane = new GlassPane();
			gameFrame.setGlassPane(glassPane);
			glassPane.setOpaque(false);
			glassPane.setVisible(true);

			// Find the JProgressBar of the App
			Component[] components = gameFrame.getContentPane().getComponents();
			JProgressBar bar = null;
			for (Component component : components) {
				if (component instanceof JProgressBar) {
					bar = (JProgressBar) component;
					break;
				}
			}
			if (bar != null) {
				// Logic loop
				while (true) {
					// Pass the value to the glass pane
					glassPane.updateProgress(bar.getValue());
					try {
						sleep(TIMEOUT);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Glass pane that gets placed on top of the JFrame to manipulate.
	 * 
	 * @author Zabuza
	 *
	 */
	private class GlassPane extends JPanel {
		/**
		 * Serial UID.
		 */
		private static final long serialVersionUID = 1L;
		private int mProgress = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		public void paintComponent(Graphics g) {
			g.setColor(new Color(0, 0, 0, 50));
			g.fillRect(3, 70, 100, 100);
			// TODO Do something with the progress value
		}
		
		/**
		 * Updates the progress value to display.
		 * @param progress Progress value
		 */
		public void updateProgress(final int progress) {
			mProgress = progress;
		}
	}
}
