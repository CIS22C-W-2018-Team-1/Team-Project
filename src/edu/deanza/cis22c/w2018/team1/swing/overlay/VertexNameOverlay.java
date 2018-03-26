package edu.deanza.cis22c.w2018.team1.swing.overlay;

import edu.deanza.cis22c.w2018.team1.structure.Pair;
import edu.deanza.cis22c.w2018.team1.swing.GraphPanel;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * A UI overlay which displays the names of vertices beside them.
 *
 * @param   <E>   the graph vertex data type
 *
 * @author Dimitriye Danilovic
 */
public class VertexNameOverlay<E> extends JPanel {
	private GraphPanel<E> graphPanel;

	public VertexNameOverlay(GraphPanel<E> graphPanel) {
		this.graphPanel = graphPanel;

		setOpaque(false);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		graphPanel.getGraph().forEach(
			vertex -> graphPanel.getVertexPosition(vertex).ifPresent(
				p -> {
					Pair<Stroke, Color> style = graphPanel.getStyleFor(vertex);

					g2d.setStroke(style.getLeft());
					g2d.setColor(style.getRight());

					g2d.drawString(vertex.toString(),
							(int) (p.getX() + graphPanel.getVertexRadius() + 10), (int) p.getY());
				}
			)
		);
	}
}
