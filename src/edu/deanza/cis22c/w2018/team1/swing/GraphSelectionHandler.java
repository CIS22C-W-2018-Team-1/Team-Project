package edu.deanza.cis22c.w2018.team1.swing;

import edu.deanza.cis22c.w2018.team1.structure.Pair;
import edu.deanza.cis22c.w2018.team1.swing.undo.UndoHistory;
import edu.deanza.cis22c.w2018.team1.swing.undo.UndoItem;
import edu.deanza.cis22c.w2018.team1.swing.util.Vector2;

import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The selection handling layer.
 *
 * @param   <E>   the graph vertex data type
 *
 * @author Dimitriye Danilovic
 */
public class GraphSelectionHandler<E> implements MouseInputListener {
	// If I had more time, this class would probably be implemented as a tool.
	// Unfortunately, I don't, so it's its own special case.
	private Set<E> selection = new HashSet<>();
	private Rectangle2D selecting;
	private Map<E, Point2D> posCache;
	private Vector2 dragStart;
	private UndoHistory history;
	private boolean headItemValid;

	private GraphPanel<E> pane;

	public interface SelectionBoxListener {
		void selectionStarted();
		void selectionChanged(Rectangle2D rect);
		void selectionEnded();
	}

	private List<SelectionBoxListener> listeners = new LinkedList<>();

	public void addBoxListener(SelectionBoxListener l) {
		listeners.add(l);
	}

	public boolean removeBoxListener(SelectionBoxListener l) {
		return listeners.remove(l);
	}

	public GraphSelectionHandler(GraphPanel<E> pane, UndoHistory history) {
		this.pane = pane;
		this.history = history;

		history.addListener(e -> headItemValid = false);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}

		Optional<E> vertex = pane.getVertexAt(e.getPoint());

		if (vertex.isPresent() && selection.contains(vertex.get()) && !e.isShiftDown()) {
			selection.clear();
			selection.add(vertex.get());
			pane.repaint();
		}
	}

	@Override
	/**
	 * On a mouse press clear the selection / begin a drag /
	 * begin a rectangle select, as appropriate.
	 */
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			dragStart = new Vector2(e.getPoint());
		}

		Optional<E> vertex = pane.getVertexAt(e.getPoint());

		if (!(vertex.isPresent() && selection.contains(vertex.get())) && !e.isShiftDown()) {
			headItemValid = false;
			selection.clear();
			pane.repaint();
		}

		if (vertex.isPresent()) {
			if (selection.add(vertex.get())) {
				headItemValid = false;
				pane.repaint();
			}

			if (e.getButton() == MouseEvent.BUTTON1) {
				posCache = new HashMap<>();

				selection.forEach((vert) -> {
					Optional<Point2D> oPos = pane.getVertexPosition(vert);
					oPos.ifPresent((pos) -> posCache.put(vert, pos));
				});
			}
		} else if (e.getButton() == MouseEvent.BUTTON1) {
			selecting = new Rectangle2D.Double();
			listeners.forEach(SelectionBoxListener::selectionStarted);
		}
	}

	private static Rectangle2D rectFromVecs(Vector2 p1, Vector2 p2) {
		Vector2 pDiff = p2.minus(p1);
		return new Rectangle2D.Double(Math.min(p1.getX(), p2.getX()),
		                              Math.min(p1.getY(), p2.getY()),
		                              Math.abs(pDiff.getX()), Math.abs(pDiff.getY()));
	}

	@Override
	/**
	 * Drag and rectangle select code
	 */
	public void mouseDragged(MouseEvent e) {
		if (dragStart == null) { return; }

		Vector2 mousePos = new Vector2(e.getPoint());
		if (posCache != null) { // Nodes are being dragged
			Vector2 dP = mousePos.minus(dragStart);
			selection.forEach(vertex ->
				Optional.of(posCache.get(vertex))
				        .ifPresent(pos -> pane.setVertexPosition(vertex, dP.plus(pos).asPoint()))
			);
			pane.repaint();
		} else {
			selecting.setRect(rectFromVecs(dragStart, mousePos));

			pane.repaint();
			listeners.forEach((l) -> l.selectionChanged(selecting));
		}
	}

	private double dragEpsilon = 5; // Small drags push garbage undo frames without this

	@Override
	/**
	 * If there's a drag, terminate it and push an UndoEvent
	 * to the history.
	 * If there's a rectangle select, terminate it and add
	 * all the vertices to the selection.
	 */
	public void mouseReleased(MouseEvent e) {
		Vector2 mousePos = new Vector2(e.getPoint());
		if (posCache != null) {
			Vector2 dP = mousePos.minus(dragStart);
			if (dP.magnitude() > dragEpsilon) {
				if (!headItemValid) {
					history.addToHistory(UndoItem.create(pane::repaint, pane::repaint));
				}
				for (E vertex : selection) {
					Point2D oldPos = posCache.get(vertex);
					Point2D newPos = dP.plus(oldPos).asPoint();
					pane.setVertexPosition(vertex, newPos);
					history.fuseToHistory(UndoItem.create(
							() -> pane.setVertexPosition(vertex, oldPos),
							() -> pane.setVertexPosition(vertex, newPos)));
				}
			}
		}
		posCache = null;
		dragStart = null;

		if (selecting != null) {
			pane.getPositionTable().entrySet().stream()
					.filter(entry -> selecting.contains(entry.getValue()))
					.map(Map.Entry::getKey)
					.forEachOrdered(selection::add);

			pane.repaint();
			selecting = null;

			listeners.forEach(SelectionBoxListener::selectionEnded);
		}
	}

	/**
	 * Used with a predicate decorator to highlight the selection
	 *
	 * @return   the predicate
	 */
	public Predicate<Pair<E, Point2D>> stylingPredicate() {
		return (pair) -> selection.contains(pair.getLeft()) || selecting != null && selecting.contains(pair.getRight());
	}

	public void clear() {
		selection.clear();
	}

	public Set<E> getSelection() {
		return Collections.unmodifiableSet(selection);
	}

	@Override public void mouseEntered(MouseEvent e) { }
	@Override public void mouseExited(MouseEvent e) { }
	@Override public void mouseMoved(MouseEvent e) { }
}
