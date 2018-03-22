package edu.deanza.cis22c.w2018.team1.swing;

import edu.deanza.cis22c.Pair;
import edu.deanza.cis22c.w2018.team1.Graph;
import edu.deanza.cis22c.w2018.team1.Vector2;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.Collection;

public class EdgeWeightOverlay<E> extends JPanel {
	private GraphPanel<E> graphPanel;

	public EdgeWeightOverlay(GraphPanel<E> graphPanel) {
		this.graphPanel = graphPanel;

		setOpaque(false);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		graphPanel.getGraph().vertices().values().stream()
				.map(Graph.Vertex::outgoingEdges)
				.flatMap(Collection::stream)
				.forEach((edge) -> graphPanel.getEdgeLine(edge).ifPresent((edgeLine) -> {
					Vector2 edgeEnd = new Vector2(edgeLine.getP2());
					Vector2 offset = edgeEnd.minus(edgeLine.getP1());

					Vector2 edgeTip = new Vector2(edgeLine.getP2()).lerp(edgeLine.getP1(), 0.3);
					Vector2 textOffset = new Vector2(-offset.getY(), offset.getX()).normalized().times(15);
					if (textOffset.getX() < 0) {
						textOffset = textOffset.times(-1);
					}
					Vector2 textPos = edgeTip.plus(textOffset);

					Pair<Stroke, Color> edgeStyle = graphPanel.getStyleFor(edge);

					g2d.setStroke(edgeStyle.getLeft());
					g2d.setColor(edgeStyle.getRight());

					g2d.drawString(String.valueOf(edge.getWeight()), (float) textPos.getX(), (float) textPos.getY());
				}));
	}
}
