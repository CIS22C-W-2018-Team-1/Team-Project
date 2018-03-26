package edu.deanza.cis22c.w2018.team1.swing;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Handles the file session, allowing for save / save as, as well
 * as notification about overwriting unsaved changes
 *
 * @param <E> the data type to be read / written
 *
 * @author Dimitriye Danilovic
 */
public class FileHandler<E> {
	private File currentSession;
	private boolean unsavedChanges = false;

	private JFrame parent;
	private JFileChooser fileChooser = new JFileChooser();
	private Supplier<E> fileFactory;
	private Function<File, Optional<E>> loader;
	private BiConsumer<File, E> saver;

	public FileHandler(JFrame parent, FileFilter filter,
	                   Supplier<E> fileFactory, Function<File, Optional<E>> loader, BiConsumer<File, E> saver) {
		this.parent = parent;
		this.fileFactory = fileFactory;
		this.loader = loader;
		this.saver = saver;
		fileChooser.setFileFilter(filter);
	}

	/**
	 * Creates a new file.
	 *
	 * @param   oldData   the data currently in the buffer, loaded lazily for performance reasons
	 * @return   the new buffer data
	 */
	public Optional<E> newFile(Supplier<E> oldData) {
		if (unsavedChanges) {
			int result = JOptionPane.showConfirmDialog(parent,
					"Would you like to save before creating a new file?",
					"Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION);

			if (result == JOptionPane.CANCEL_OPTION) { return Optional.empty(); }
			if (result == JOptionPane.YES_OPTION) {
				if (!save(oldData.get())) {
					return Optional.empty();
				}
			}
		}
		unsavedChanges = false;
		currentSession = null;
		return Optional.of(fileFactory.get());
	}

	/**
	 * Tries to open a file for the user, prompting for unsaved data,
	 * pulling up the file chooser, etc.
	 *
	 * @param   oldData   the data currently in the buffer, loaded lazily for performance reasons
	 * @return   the new buffer data
	 */
	public Optional<E> open(Supplier<E> oldData) {
		if (unsavedChanges) {
			int result = JOptionPane.showConfirmDialog(parent,
					"Would you like to save before opening another file?",
					"Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION);

			if (result == JOptionPane.CANCEL_OPTION) { return Optional.empty(); }
			if (result == JOptionPane.YES_OPTION) {
				if (!save(oldData.get())) {
					return Optional.empty();
				}
			}
		}
		int result = fileChooser.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File newSession = fileChooser.getSelectedFile();
			Optional<E> retVal = loader.apply(newSession);
			if (retVal.isPresent()) {
				unsavedChanges = false;
				currentSession = newSession;
			}
			return retVal;
		}
		return Optional.empty();
	}

	/**
	 * Implementation method for save and saveAs
	 *
	 * @param   data     the buffer data to save
	 * @param   saveAs   whether to always ask the user where to save,
	 *                      or only if we don't know where
	 *
	 * @return   true if the save was successful
	 */
	private boolean saveImpl(E data, boolean saveAs) {
		if (saveAs || currentSession == null) {
			int res = fileChooser.showSaveDialog(parent);
			if (res == JFileChooser.CANCEL_OPTION) { return false; }

			currentSession = fileChooser.getSelectedFile();
		}

		saver.accept(currentSession, data);
		unsavedChanges = false;

		return true;
	}

	public boolean save(E data) {
		return saveImpl(data, false);
	}

	public boolean saveAs(E data) {
		return saveImpl(data, true);
	}

	/**
	 * Notifies the FileHandler of unsaved changes so it knows to
	 * prompt the user to save.
	 */
	public void notifyOfUnsavedChanges() {
		unsavedChanges = true;
	}
}
