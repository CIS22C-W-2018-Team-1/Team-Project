package edu.deanza.cis22c.w2018.team1.swing;

import edu.deanza.cis22c.Pair;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Map;
import java.util.Objects;

public class VertexLevelOverlay<E> extends JPanel {
	private GraphPanel<E> graphPanel;
	private Map<E, Integer> levels;

	public VertexLevelOverlay(GraphPanel<E> graphPanel, Map<E, Integer> levels) {
		this.graphPanel = graphPanel;
		this.levels = levels;

		setOpaque(false);
	}

	public void setLevels(Map<E, Integer> levels) {
		this.levels = levels;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		graphPanel.getGraph().vertices().values().forEach( (vertex) ->
			graphPanel.getVertexPosition(vertex.getId()).ifPresent( (p) -> {
				Pair<Stroke, Color> style = graphPanel.getStyleFor(vertex);

				g2d.setStroke(style.getLeft());
				g2d.setColor(style.getRight());

				g2d.drawString(Objects.toString(levels.get(vertex.getId())), (int) p.getX(), (int) p.getY());
			}));
	}
}
