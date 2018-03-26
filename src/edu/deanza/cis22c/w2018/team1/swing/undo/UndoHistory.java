package edu.deanza.cis22c.w2018.team1.swing.undo;

import edu.deanza.cis22c.w2018.team1.structure.stack.LinkedStack;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * An undo history with redo capabilities
 *
 * @author Dimitriye Danilovic
 */
public class UndoHistory {
	private LinkedStack<UndoItem> undoStack = new LinkedStack<>();
	private LinkedStack<UndoItem> redoStack = new LinkedStack<>();

	public enum UndoEvent {
		ITEM_ADDED, UNDO, REDO, HISTORY_CLEARED
	}

	private List<Consumer<UndoEvent>> listeners = new LinkedList<>();

	public void addListener(Consumer<UndoEvent> listener) {
		listeners.add(listener);
	}

	public boolean removeListener(Consumer<UndoEvent> listener) {
		return listeners.remove(listener);
	}

	private void triggerListeners(UndoEvent ev) {
		listeners.forEach(l -> l.accept(ev));
	}

	/**
	 * Adds the given item to the history.
	 * This drops all redo operations from the
	 * history and triggers listeners.
	 *
	 * @param   item   the item to add
	 */
	public void addToHistory(UndoItem item) {
		undoStack.push(item);
		clearForward();
		triggerListeners(UndoEvent.ITEM_ADDED);
	}

	/**
	 * Merges the given item onto the most recent undo frame.
	 * Note that this operation *does not* trigger listeners.
	 *
	 * @param   item   the item to fuse on
	 */
	public void fuseToHistory(UndoItem item) {
		undoStack.push(undoStack.pop().compose(item));
	}

	/**
	 * Clears the redo history.
	 */
	public void clearForward() {
		redoStack = new LinkedStack<>();
	}

	/**
	 * Clears the history
	 */
	public void clear() {
		undoStack = new LinkedStack<>();
		clearForward();
		triggerListeners(UndoEvent.HISTORY_CLEARED);
	}

	/**
	 * Executes the top undo frame, and pushes it
	 * to the redo stack.
	 */
	public void undo() {
		if (canUndo()) {
			UndoItem item = undoStack.pop();
			item.undo();
			redoStack.push(item);
		}
		triggerListeners(UndoEvent.UNDO);
	}

	/**
	 * Returns whether or not there's anything to undo
	 *
	 * @return   true if there are undo frames in the stack
	 */
	public boolean canUndo() {
		return !undoStack.isEmpty();
	}

	/**
	 * Executes the top redo frame, and pushes it
	 * to the undo stack.
	 */
	public void redo() {
		if (canRedo()) {
			UndoItem item = redoStack.pop();
			item.redo();
			undoStack.push(item);
		}
		triggerListeners(UndoEvent.REDO);
	}

	/**
	 * Returns whether or not there's anything to undo
	 *
	 * @return   true if there are frames in the redo stack
	 */
	public boolean canRedo() {
		return !redoStack.isEmpty();
	}
}
