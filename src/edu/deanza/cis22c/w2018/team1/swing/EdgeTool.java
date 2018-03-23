package edu.deanza.cis22c.w2018.team1.swing;

import edu.deanza.cis22c.w2018.team1.Graph;
import edu.deanza.cis22c.w2018.team1.Vector2;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;
import javax.tools.Tool;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class EdgeTool<E> implements MouseInputListener {
	private Graph<E>.Vertex source;
	private GraphPanel<E> panel;
	private Point mousePos;
	private List<ToolListener> listeners = new LinkedList<>();

	public void addToolListener(ToolListener l) {
		listeners.add(l);
	}

	public boolean removeToolListener(ToolListener l) {
		return listeners.remove(l);
	}

	public EdgeTool(GraphPanel<E> panel) {
		this.panel = panel;
	}

	private static Polygon arrowHead = new Polygon();
	static {
		arrowHead.addPoint(0, 0);
		arrowHead.addPoint(-10, 10);
		arrowHead.addPoint(-10, -10);
	}

	private JPanel edgeToolOverlay = new JPanel() {
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			if (source == null || mousePos == null) { return; }

			Graphics2D g2d = (Graphics2D) g;

			Vector2 sourcePos = new Vector2(panel.getVertexPosition(source.getId()).get());
			Vector2 destPos;

			Optional<Vector2> destVertPos = panel
					.getVertexAt(mousePos)
					.map(Graph.Vertex::getId)
					.flatMap(panel::getVertexPosition)
					.map(Vector2::new);

			destPos = destVertPos.orElse(new Vector2(mousePos));

			Vector2 offset = destPos.minus(sourcePos).normalized().times(panel.getVertexRadius());
			Vector2 edgeStart = sourcePos.plus(offset);

			Vector2 edgeEnd = destVertPos.isPresent() ? destPos.minus(offset) : destPos;

			g2d.setStroke(panel.getEdgeStroke());
			g2d.setColor(panel.getEdgeColor());

			g2d.drawLine((int) edgeStart.getX(), (int) edgeStart.getY(),
			             (int)   edgeEnd.getX(), (int)   edgeEnd.getY());

			AffineTransform tx = new AffineTransform();
			tx.translate(edgeEnd.getX(), edgeEnd.getY());
			tx.rotate(offset.getX(), offset.getY());

			AffineTransform clearTx = g2d.getTransform();

			g2d.transform(tx);
			g2d.fill(arrowHead);
			g2d.setTransform(clearTx);
		}
	};

	{
		edgeToolOverlay.setOpaque(false);
	}

	public JPanel getOverlay() {
		return edgeToolOverlay;
	}

	public void setSource(Graph<E>.Vertex source) {
		this.source = source;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mousePos = e.getPoint();
		edgeToolOverlay.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) { return; }

		Optional<Graph<E>.Vertex> vertex = panel.getVertexAt(e.getPoint());

		vertex.ifPresent((vert) -> {
			if (source == null) {
				source = vert;
				edgeToolOverlay.repaint();
			} else {
				String sWeight = JOptionPane.showInputDialog("Edge weight");
				if (sWeight != null) {
					Double weight = Double.valueOf(sWeight);
					source.createOrUpdateEdgeTo(vert, weight);

					source = null;
					edgeToolOverlay.repaint();
				}

				listeners.forEach(ToolListener::actionPerformed);
			}
		});

		e.consume();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			e.consume();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			e.consume();
		}
	}

	@Override public void mouseDragged(MouseEvent e) { }
	@Override public void mouseEntered(MouseEvent e) { }
	@Override public void mouseExited(MouseEvent e) { }
}
