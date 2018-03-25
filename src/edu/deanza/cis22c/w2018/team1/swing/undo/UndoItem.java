package edu.deanza.cis22c.w2018.team1.swing.undo;

public interface UndoItem {
	void undo();
	void redo();

	static UndoItem create(Runnable undo, Runnable redo) {
		return new UndoItem() {
			@Override public void undo() { undo.run(); }
			@Override public void redo() { redo.run(); }
		};
	}

	static UndoItem nop() {
		return new UndoItem() {
			@Override public void undo() { }
			@Override public void redo() { }
		};
	}

	default UndoItem compose(UndoItem o) {
		UndoItem self = this;
		return new UndoItem() {
			@Override
			public void undo() {
				o.undo();
				self.undo();
			}

			@Override
			public void redo() {
				self.redo();
				o.redo();
			}
		};
	}
}
