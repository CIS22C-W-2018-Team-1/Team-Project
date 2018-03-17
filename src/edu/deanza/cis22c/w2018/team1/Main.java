package edu.deanza.cis22c.w2018.team1;

import javax.swing.*;

public class Main implements Runnable {
	public static void main(String... args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.invokeLater(new Main());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		JFrame frame = new JFrame();
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		GraphPanel pane = new GraphPanel();
		frame.add(pane);

		frame.setVisible(true);
	}
}
