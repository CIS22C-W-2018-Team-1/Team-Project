package edu.deanza.cis22c.w2018.team1.swing;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.Font;
import java.io.Writer;

/**
 * A frame which provides an output console to be written to by a writer.
 *
 * @author Dimitriye Danilovic
 */
public class ConsoleFrame extends JFrame {
	private JTextArea textArea;
	public ConsoleFrame() {
		textArea = new JTextArea(24, 80);
		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		add(textArea);
		pack();
	}

	public Writer consoleWriter() {
		return new Writer() {
			@Override
			public void write(char[] cbuf, int off, int len) {
				textArea.append(String.valueOf(cbuf, off, len));
			}

			@Override public void flush() { textArea.repaint(); }

			@Override
			public void close() { }
		};
	}

	public void init() {
		pack();
		setVisible(true);
	}
}
