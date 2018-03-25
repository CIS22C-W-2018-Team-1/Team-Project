package edu.deanza.cis22c.w2018.team1.swing.util;

import edu.deanza.cis22c.w2018.team1.structure.stack.LinkedStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class UndoHistory {
	private LinkedStack<UndoItem> undoStack = new LinkedStack<>();
	private LinkedStack<UndoItem> redoStack = new LinkedStack<>();

	public enum UndoEvent {
		ITEM_ADDED, UNDO, REDO
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

	public void addToHistory(UndoItem item) {
		undoStack.push(item);
		clearForward();
		triggerListeners(UndoEvent.ITEM_ADDED);
	}

	public void fuseToHistory(UndoItem item) {
		undoStack.push(undoStack.pop().compose(item));
	}

	public void clearForward() {
		redoStack = new LinkedStack<>();
	}

	public void clear() {
		undoStack = new LinkedStack<>();
		clearForward();
	}

	public void undo() {
		if (canUndo()) {
			UndoItem item = undoStack.pop();
			item.undo();
			redoStack.push(item);
		}
		triggerListeners(UndoEvent.UNDO);
	}

	public boolean canUndo() {
		return !undoStack.isEmpty();
	}

	public void redo() {
		if (canRedo()) {
			UndoItem item = redoStack.pop();
			item.redo();
			undoStack.push(item);
		}
		triggerListeners(UndoEvent.REDO);
	}

	public boolean canRedo() {
		return !redoStack.isEmpty();
	}
}
