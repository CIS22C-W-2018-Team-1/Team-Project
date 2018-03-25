package edu.deanza.cis22c.w2018.team1.swing.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class UndoHistory {
	private static class UndoNode {
		UndoItem item;
		UndoNode prev;
		UndoNode next;

		UndoNode(UndoItem item, UndoNode prev) {
			this.item = item;
			this.prev = prev;
		}
	}

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

	private UndoNode currentState = new UndoNode(null, null);

	public void addToHistory(UndoItem item) {
		currentState.next = new UndoNode(item, currentState);
		currentState = currentState.next;
		triggerListeners(UndoEvent.ITEM_ADDED);
	}

	public void fuseToHistory(UndoItem item) {
		currentState.item = currentState.item.compose(item);
	}

	public void clearForward() {
		currentState.next = null;
	}

	public void clear() {
		currentState = new UndoNode(null, null);
	}

	public void undo() {
		if (canUndo()) {
			currentState.item.undo();
			currentState = currentState.prev;
		}
		triggerListeners(UndoEvent.UNDO);
	}

	public boolean canUndo() {
		return currentState != null && currentState.prev != null;
	}

	public void redo() {
		if (canRedo()) {
			currentState = currentState.next;
			currentState.item.redo();
		}
		triggerListeners(UndoEvent.REDO);
	}

	public boolean canRedo() {
		return currentState != null && currentState.next != null;
	}
}
