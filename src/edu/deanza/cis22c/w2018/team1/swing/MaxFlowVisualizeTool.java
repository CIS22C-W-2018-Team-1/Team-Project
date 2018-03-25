package edu.deanza.cis22c.w2018.team1.swing;

import edu.deanza.cis22c.w2018.team1.structure.graph.Graph;
import edu.deanza.cis22c.w2018.team1.structure.graph.maxflow.MaxFlow;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

public class MaxFlowVisualizeTool<E> implements MouseListener {
	private GraphPanel<E> panel;
	private E source;
	private List<ToolListener> listeners = new LinkedList<>();
	private JFrame oldFrame;

	public void addToolListener(ToolListener l) {
		listeners.add(l);
	}

	public boolean removeToolListener(ToolListener l) {
		return listeners.remove(l);
	}

	public MaxFlowVisualizeTool(JFrame frame, GraphPanel<E> panel, E source) {
		this.oldFrame = frame;
		this.panel = panel;
		this.source = source;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) { return; }

		Optional<E> vertex = panel.getVertexAt(e.getPoint());

		vertex.ifPresent((vert) -> {
			JFrame frame = buildMaxFlowVisualizerFrame(panel.getGraph(), panel.getPositionTable(), source, vert);
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


	private static class FlowState<E> {
		double extantFlow;
		boolean capacitiesVisible;
		Map<E, Integer> levels;
		Graph<E> graph;
		Graph<E> overlayGraph;
		Color overlayColor;

		public FlowState(double extantFlow, boolean capacitiesVisible, Map<E, Integer> levels,
		                 Graph<E> graph, Graph<E> overlayGraph, Color overlayColor) {
			this.extantFlow = extantFlow;
			this.capacitiesVisible = capacitiesVisible;
			this.levels = levels;
			this.graph = graph;
			this.overlayGraph = overlayGraph;
			this.overlayColor = overlayColor;
		}
	}

	private static class FlowHolder<E> {
		private ListIterator<FlowState<E>> iterator;
		boolean steppedForwardLast = true;

		public FlowHolder(ListIterator<FlowState<E>> iterator) {
			this.iterator = iterator;
		}

		public FlowState previous() {
			FlowState<E> ret = iterator.previous();
			if (steppedForwardLast) {
				ret = iterator.previous();
				steppedForwardLast = false;
			}

			return ret;
		}

		public boolean hasPrevious() {
			return iterator.hasPrevious();
		}

		public FlowState next() {
			FlowState<E> ret = iterator.next();
			if (!steppedForwardLast) {
				ret = iterator.next();
				steppedForwardLast = true;
			}

			return ret;
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}
	}

	public static <E> JFrame buildMaxFlowVisualizerFrame(Graph<E> graph, Map<E, Point2D> positionTable,
	                                                     E source, E sink) {
		MaxFlow<E> maxFlow = new MaxFlow<>(graph, source, sink);
		LinkedList<FlowState<E>> stateList = new LinkedList<>();

		Graph<E> emptyGraph = new Graph<>();
		maxFlow.computeLevels();
		stateList.add(new FlowState<>(0.0, true, maxFlow.getLastLevels(),
				graph, emptyGraph, Color.RED));
		Graph<E> accessibility = maxFlow.getAccessibilityGraph();
		maxFlow.doIteration();
		while (!maxFlow.isDone()) {
			stateList.add(new FlowState<>(maxFlow.getTotalFlow(), true, maxFlow.getLastLevels(),
					accessibility, emptyGraph, Color.RED));
			stateList.add(new FlowState<>(maxFlow.getTotalFlow(), false, maxFlow.getLastLevels(),
					accessibility, maxFlow.getLastFlowGraph(), Color.YELLOW));
			Graph<E> totalFlow = new Graph<>();
			totalFlow.addSubgraph(maxFlow.getTotalFlowGraph(), (l, r) -> r);
			stateList.add(new FlowState<>(maxFlow.getTotalFlow(), false, maxFlow.getLastLevels(),
					accessibility, totalFlow, Color.BLUE));

			maxFlow.computeLevels();
			accessibility = maxFlow.getAccessibilityGraph();
			maxFlow.doIteration();
		}

		JPanel pane = new JPanel();
		BorderLayout layout = new BorderLayout();
		pane.setLayout(layout);

		JPanel layers = new JPanel();
		OverlayLayout overlayer = new OverlayLayout(layers);
		layers.setLayout(overlayer);

		GraphPanel<E> graphPanel = new GraphPanel<>(graph);
		graphPanel.addPositionData(positionTable);
		layers.add(graphPanel, 0);

		GraphPanel<E> overlayPanel = new GraphPanel<>(stateList.getFirst().overlayGraph);
		overlayPanel.addPositionData(positionTable);
		overlayPanel.setOpaque(false);
		layers.add(overlayPanel, 0);

		VertexLevelOverlay<E> levelOverlay = new VertexLevelOverlay<>(graphPanel, stateList.getFirst().levels);
		layers.add(levelOverlay, 0);
		graphPanel.addRepaintListener(layers::repaint);

		EdgeWeightOverlay<E> capacityOverlay = new EdgeWeightOverlay<>(graphPanel);
		layers.add(capacityOverlay, 0);
		overlayPanel.addRepaintListener(capacityOverlay::repaint);

		EdgeWeightOverlay<E> flowOverlay = new EdgeWeightOverlay<>(overlayPanel);
		layers.add(flowOverlay, 0);
		overlayPanel.addRepaintListener(flowOverlay::repaint);

		pane.add(layers, BorderLayout.CENTER);

		ListIterator<FlowState<E>> stateIterator = stateList.listIterator();
		stateIterator.next(); // Advance it by 1

		FlowHolder<E> flow = new FlowHolder<>(stateIterator);

		JPanel buttonBar = new JPanel();

		JButton prev = new JButton("<-");
		JButton next = new JButton("->");

		prev.addActionListener((e) -> {
			FlowState<E> currentState = flow.previous();

			graphPanel.setGraph(currentState.graph);
			overlayPanel.setGraph(currentState.overlayGraph);
			capacityOverlay.setVisible(currentState.capacitiesVisible);
			overlayPanel.setEdgeColor(currentState.overlayColor);
			levelOverlay.setLevels(currentState.levels);

			graphPanel.repaint();
			overlayPanel.repaint();

			prev.setEnabled(flow.hasPrevious());
			next.setEnabled(flow.hasNext());
		});
		prev.setEnabled(false);
		buttonBar.add(prev);

		next.addActionListener((e) -> {
			FlowState<E> currentState = flow.next();

			graphPanel.setGraph(currentState.graph);
			overlayPanel.setGraph(currentState.overlayGraph);
			capacityOverlay.setVisible(currentState.capacitiesVisible);
			overlayPanel.setEdgeColor(currentState.overlayColor);
			levelOverlay.setLevels(currentState.levels);

			graphPanel.repaint();
			overlayPanel.repaint();

			prev.setEnabled(flow.hasPrevious());
			next.setEnabled(flow.hasNext());
		});
		buttonBar.add(next);

		pane.add(buttonBar, BorderLayout.SOUTH);

		JFrame ret = new JFrame();
		ret.add(pane);
		ret.pack();
		ret.setSize(1280, 800);
		return ret;
	}

}
