package edu.deanza.cis22c.w2018.team1.swing.tool;

import edu.deanza.cis22c.w2018.team1.structure.graph.maxflow.MaxFlow;
import edu.deanza.cis22c.w2018.team1.swing.GraphPanel;
import edu.deanza.cis22c.w2018.team1.swing.TabularLogFrame;

import javax.swing.WindowConstants;
import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Implementation of the MaxFlow UI tool
 *
 * @param   <E>   the graph vertex data type
 *
 * @author Dimitriye Danilovic
 */
public class MaxFlowTool<E> implements MouseListener {
	private GraphPanel<E> panel;
	private E source;
	private List<ToolListener> listeners = new LinkedList<>();
	private List<String[]> log = new ArrayList<>();
	private TabularLogFrame logFrame;

	private void makeLogFrame() {
		logFrame = new TabularLogFrame("Max Flows", new String[]{"source", "sink", "max flow"}, log);
		logFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				log = new ArrayList<>();
				makeLogFrame();
			}
		});
		logFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void addToolListener(ToolListener l) {
		listeners.add(l);
	}

	public boolean removeToolListener(ToolListener l) {
		return listeners.remove(l);
	}

	public MaxFlowTool(GraphPanel<E> panel) {
		this.panel = panel;
		makeLogFrame();
	}

	public void setSource(E source) {
		this.source = source;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) { return; }

		Optional<E> vertex = panel.getVertexAt(e.getPoint());

		vertex.ifPresent((vert) -> {
			OptionalDouble maxFlow = MaxFlow.findMaximumFlow(panel.getGraph(), source, vert);

			if (maxFlow.isPresent()) {
				double flow = maxFlow.getAsDouble();

				log.add(new String[]{source.toString(), vert.toString(), String.valueOf(flow)});
			} else {
				log.add(new String[]{source.toString(), vert.toString(), "N/A"});
			}
			logFrame.setVisible(true);
			logFrame.notifyDataChanged();
			listeners.forEach(ToolListener::actionPerformed);
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
