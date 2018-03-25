package edu.deanza.cis22c.w2018.team1.swing.overlay;

import edu.deanza.cis22c.w2018.team1.structure.Pair;
import edu.deanza.cis22c.w2018.team1.structure.graph.Graph;
import edu.deanza.cis22c.w2018.team1.swing.GraphPanel;
import edu.deanza.cis22c.w2018.team1.swing.util.Vector2;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

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

		Graph<E> graph = graphPanel.getGraph();
		graph.forEach(
			source -> graph.getDirectSuccessors(source).get().forEach(
				dest -> {
					Pair<E, E> edge = new Pair<>(source, dest);
					graphPanel.getEdgeLine(edge).ifPresent(
						edgeLine -> {
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

							g2d.drawString(String.valueOf(graph.getEdgeCost(source, dest).getAsDouble()),
									(float) textPos.getX(), (float) textPos.getY());
						}
					);
				}
			)
		);
	}
}
