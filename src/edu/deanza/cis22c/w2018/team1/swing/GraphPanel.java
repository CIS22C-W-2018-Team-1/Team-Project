package edu.deanza.cis22c.w2018.team1.swing;

import edu.deanza.cis22c.w2018.team1.Vector2;
import edu.deanza.cis22c.w2018.team1.structure.Pair;
import edu.deanza.cis22c.w2018.team1.structure.graph.Graph;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class GraphPanel<E> extends JPanel {
	private Graph<E> graph;
	private HashMap<E, Point2D> positionTable;
	private List<Function<Pair<E, Point2D>, Optional<Pair<Stroke, Color>>>> vertexDecorators = new LinkedList<>();
	private List<Function<Pair<E, E>, Optional<Pair<Stroke, Color>>>> edgeDecorators = new LinkedList<>();
	private double vertexRadius = 25;

	private List<Runnable> repaintListeners = new LinkedList<>();

	private Pair<Stroke, Color> vertexStyle = new Pair<>(new BasicStroke(2), Color.BLACK);
	private Pair<Stroke, Color> edgeStyle = new Pair<>(new BasicStroke(2), Color.BLACK);

	public GraphPanel() {
		positionTable = new HashMap<>();

		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public GraphPanel(Graph<E> graph) {
		this.graph = graph;

		positionTable = new HashMap<>();

		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public GraphPanel(Graph<E> graph, double vertexRadius) {
		this.graph = graph;
		this.vertexRadius = vertexRadius;

		positionTable = new HashMap<>();

		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public GraphPanel(Graph<E> graph, Map<E, Point2D> positionTable) {
		this.graph = graph;
		this.positionTable = new HashMap<>(positionTable);

		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public GraphPanel(Graph<E> graph, Map<E, Point2D> positionTable, double vertexRadius) {
		this.graph = graph;
		this.positionTable = new HashMap<>(positionTable);
		this.vertexRadius = vertexRadius;

		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public void setGraph(Graph<E> graph) {
		this.graph = graph;
	}

	public Graph<E> getGraph() {
		return graph;
	}

	public void addRepaintListener(Runnable l) {
		this.repaintListeners.add(l);
	}

	public void removeRepaintListener(Runnable l) {
		this.repaintListeners.remove(l);
	}

	@Override
	public void repaint() {
		super.repaint();

		// No way to initialize it before the superclass constructor calls this
		if (repaintListeners == null) { return; }

		repaintListeners.forEach(Runnable::run);
	}

	public void setVertexRadius(double vertexRadius) {
		this.vertexRadius = vertexRadius;
	}

	public double getVertexRadius() {
		return vertexRadius;
	}

	public void setVertexPosition(E vertex, Point2D pos) {
		positionTable.put(vertex, new Point2D.Double(pos.getX(), pos.getY()));
	}

	public void addPositionData(Map<E, Point2D> data) {
		positionTable.putAll(data);
	}

	public Optional<Point2D> getVertexPosition(E vertex) {
		return Optional.ofNullable(positionTable.get(vertex));
	}

	public Optional<E> getVertexAt(Point2D point) {
		return getVertexNear(point, vertexRadius);
	}

	public Optional<E> getVertexNear(Point2D point, double epsilon) {
		// TODO: Replace with some form of spacial partitioning for efficiency reasons
		for (Map.Entry<E, Point2D> entry: positionTable.entrySet()) {
			if (new Vector2(point).minus(entry.getValue()).magnitude() < epsilon) {
				return Optional.of(entry.getKey());
			}
		}

		return Optional.empty();
	}

	public Map<E, Point2D> getPositionTable() {
		return Collections.unmodifiableMap(positionTable);
	}

	public void setVertexStyle(Stroke stroke, Color color) {
		this.vertexStyle = new Pair<>(stroke, color);
	}

	public void setVertexStroke(Stroke stroke) {
		vertexStyle.setLeft(stroke);
	}

	public Stroke getVertexStroke() {
		return vertexStyle.getLeft();
	}

	public void setVertexColor(Color color) {
		vertexStyle.setRight(color);
	}

	public Color getVertexColor() {
		return vertexStyle.getRight();
	}

	public void setEdgeStyle(Stroke stroke, Color color) {
		this.edgeStyle = new Pair<>(stroke, color);
	}

	public void setEdgeStroke(Stroke stroke) {
		edgeStyle.setLeft(stroke);
	}

	public Stroke getEdgeStroke() {
		return edgeStyle.getLeft();
	}

	public void setEdgeColor(Color color) {
		edgeStyle.setRight(color);
	}

	public Color getEdgeColor() {
		return edgeStyle.getRight();
	}

	public void addVertexDecorator(Function<Pair<E, Point2D>, Optional<Pair<Stroke, Color>>> decorator) {
		this.vertexDecorators.add(0, decorator);
	}

	public boolean removeVertexDecorator(Function<Pair<E, Point2D>, Optional<Pair<Stroke, Color>>> decorator) {
		return this.vertexDecorators.remove(decorator);
	}

	public Function<Pair<E, Point2D>, Optional<Pair<Stroke, Color>>> removeVertexDecorator(int i) {
		return this.vertexDecorators.remove(i);
	}

	public void addEdgeDecorator(Function<Pair<E, E>, Optional<Pair<Stroke, Color>>> decorator) {
		this.edgeDecorators.add(0, decorator);
	}

	public boolean removeEdgeDecorator(Function<Pair<E, E>, Optional<Pair<Stroke, Color>>> decorator) {
		return this.edgeDecorators.remove(decorator);
	}

	public Function<Pair<E, E>, Optional<Pair<Stroke, Color>>> removeEdgeDecorator(int i) {
		return this.edgeDecorators.remove(i);
	}

	private static Polygon arrowHead = new Polygon();
	static {
		arrowHead.addPoint(0, 0);
		arrowHead.addPoint(-10, 10);
		arrowHead.addPoint(-10, -10);
	}

	private static <E> Pair<Stroke, Color> decoratorLadder(List<Function<E, Optional<Pair<Stroke, Color>>>> decorators,
	                                                       Pair<Stroke, Color> defaultStyle,
	                                                       E toDecorate) {
		for (Function<E, Optional<Pair<Stroke, Color>>> decorator: decorators) {
			Optional<Pair<Stroke, Color>> style = decorator.apply(toDecorate);
			if (style.isPresent()) { return style.get(); }
		}

		return defaultStyle;
	}

	public Pair<Stroke, Color> getStyleFor(E vertex) {
		return decoratorLadder(vertexDecorators, vertexStyle, new Pair<>(vertex, positionTable.get(vertex)));
	}

	public Pair<Stroke, Color> getStyleFor(Pair<E, E> edge) {
		return decoratorLadder(edgeDecorators, edgeStyle, edge);
	}

	public Optional<Line2D> getEdgeLine(Pair<E, E> edge) {
		return Optional.ofNullable(positionTable.get(edge.getLeft())).map(Vector2::new).flatMap((sourcePos) ->
			Optional.ofNullable(positionTable.get(edge.getRight())).map(Vector2::new).map((destPos) -> {
				Vector2 offset = destPos.minus(sourcePos).normalized().times(vertexRadius);

				return new Line2D.Double(sourcePos.plus(offset).asPoint(), destPos.minus(offset).asPoint());
			}));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (graph == null) { return; }

		Graphics2D g2d = (Graphics2D) g;

		Rectangle clipBound = g2d.getClipBounds();

		clipBound.x -= vertexRadius;
		clipBound.y -= vertexRadius;
		clipBound.width  += 2 * vertexRadius;
		clipBound.height += 2 * vertexRadius;

		Graphics2D localContext = (Graphics2D) g2d.create();

		AffineTransform clearTx = g2d.getTransform();

		for (E vertex: graph) {
			Point2D vertexPos = positionTable.get(vertex);

			if (vertexPos == null) { continue; }

			AffineTransform tx = new AffineTransform();
			for (E dest: graph.getDirectSuccessors(vertex).get()) {
				Pair<E, E> edge = new Pair<>(vertex, dest);
				getEdgeLine(edge).ifPresent((edgeLine) -> {
					if (!clipBound.intersectsLine(edgeLine)) {
						return;
					}

					Pair<Stroke, Color> style = getStyleFor(edge);

					localContext.setStroke(style.getLeft());
					localContext.setColor(style.getRight());

					localContext.draw(edgeLine);

					Vector2 edgeEnd = new Vector2(edgeLine.getP2());
					Vector2 offset  = edgeEnd.minus(edgeLine.getP1());

					tx.setToIdentity();
					tx.translate(edgeEnd.getX(), edgeEnd.getY());
					tx.rotate(offset.getX(), offset.getY());

					localContext.transform(tx);
					localContext.fill(arrowHead);
					localContext.setTransform(clearTx);
				});
			}
		}

		for (E vertex: graph) {
			Point2D p = positionTable.get(vertex);

			if (p == null || !clipBound.contains(p)) {
				continue;
			}

			Pair<Stroke, Color> style = getStyleFor(vertex);

			localContext.setStroke(style.getLeft());
			localContext.setColor(style.getRight());

			localContext.draw(new Ellipse2D.Double(p.getX() - vertexRadius, p.getY() - vertexRadius,
			                                       2 * vertexRadius, 2 * vertexRadius));
		}
	}
}
