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
		return Optional.of(fileFactory.get());
	}

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
			Optional<E> retVal = loader.apply(fileChooser.getSelectedFile());
			if (retVal.isPresent()) {
				unsavedChanges = false;
			}
			return retVal;
		}
		return Optional.empty();
	}

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

	public void notifyOfUnsavedChanges() {
		unsavedChanges = true;
	}
}
