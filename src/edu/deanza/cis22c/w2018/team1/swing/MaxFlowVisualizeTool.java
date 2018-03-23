package edu.deanza.cis22c.w2018.team1.swing;

import edu.deanza.cis22c.w2018.team1.Graph;
import edu.deanza.cis22c.w2018.team1.Main;
import edu.deanza.cis22c.w2018.team1.flowimpl.MaxFlow;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public class MaxFlowVisualizeTool<E> implements MouseListener {
	private GraphPanel<E> panel;
	private Graph<E>.Vertex source;
	private List<ToolListener> listeners = new LinkedList<>();
	private JFrame oldFrame;

	public void addToolListener(ToolListener l) {
		listeners.add(l);
	}

	public boolean removeToolListener(ToolListener l) {
		return listeners.remove(l);
	}

	public MaxFlowVisualizeTool(JFrame frame, GraphPanel<E> panel, Graph<E>.Vertex source) {
		this.oldFrame = frame;
		this.panel = panel;
		this.source = source;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) { return; }

		Optional<Graph<E>.Vertex> vertex = panel.getVertexAt(e.getPoint());

		vertex.ifPresent((vert) -> {
			JFrame frame = Main.buildMaxFlowVisualizerFrame(panel.getGraph(), panel.getPositionTable(), source, vert);
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			oldFrame.setVisible(false);
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

	@Override public void mouseEntered(MouseEvent e) { }
	@Override public void mouseExited(MouseEvent e) { }
}
