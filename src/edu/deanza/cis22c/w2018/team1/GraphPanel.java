package edu.deanza.cis22c.w2018.team1;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.UUID;

public class GraphPanel extends JPanel {
	private Graph<UUID> graph;
	private Map<UUID, Point> posData;
	private LinkedHashSet<UUID> selected;

	private static final int NODE_RADIUS = 25;

	private Point locCache;
	private Map<UUID, Point> posCache;
	private Point dragStartLoc;
	private UUID clickedNode;
	private HashSet<UUID> alreadySelected;
	private Rectangle2D.Double rectSelection;

	private class GraphRMouseMenu extends JPopupMenu {

		private JMenuItem newNode;
		private JMenuItem linkNodes;
		private JMenuItem doMaxFlow;

		public GraphRMouseMenu() {
			newNode = new JMenuItem("New Node");
			newNode.addActionListener((e) -> {
				UUID node = UUID.randomUUID();
				graph.getOrCreateVertex(node);
				posData.put(node, new Point(locCache.x - NODE_RADIUS, locCache.y - NODE_RADIUS));
				GraphPanel.this.repaint();
			});

			linkNodes = new JMenuItem("Link nodes");
			linkNodes.addActionListener((e) -> {
				Double weight = Double.valueOf(JOptionPane.showInputDialog("Edge weight"));
				Iterator<UUID> iter = selected.iterator();
				UUID source = iter.next();
				UUID dest = iter.next();

				Graph<UUID>.Vertex vSource = graph.getOrCreateVertex(source);
				Graph<UUID>.Vertex vDest   = graph.getOrCreateVertex(dest);
				vSource.createOrUpdateEdgeTo(vDest, weight);

				Point sourcePos = posData.get(source);
				Point destPos = posData.get(dest);

				if (sourcePos != null && destPos != null) {
					int redrawStartX = Math.min(sourcePos.x, destPos.x);
					int redrawStartY = Math.min(sourcePos.y, destPos.y);
					int redrawEndX = Math.max(sourcePos.x, destPos.x);
					int redrawEndY = Math.max(sourcePos.y, destPos.y);

					GraphPanel.this.repaint(redrawStartX - NODE_RADIUS, redrawStartY - NODE_RADIUS,
					                        redrawEndX - redrawStartX + 4 * NODE_RADIUS,
					                        redrawEndY - redrawStartY + 4 * NODE_RADIUS);
				}

				selected.clear();
			});

			doMaxFlow = new JMenuItem("Compute Max Flow");
			doMaxFlow.addActionListener((e) -> {
				Iterator<UUID> iter = selected.iterator();
				UUID source = iter.next();
				UUID sink = iter.next();

				OptionalDouble flow = MaxFlow.findMaximumFlow(graph, source, sink);

				if (flow.isPresent()) {
					JOptionPane.showMessageDialog(GraphPanel.this, String.valueOf(flow.getAsDouble()));
				} else {
					JOptionPane.showMessageDialog(GraphPanel.this, "Cannot find valid flow",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			});
		}

		public void updateItems() {
			removeAll();

			if (selected.isEmpty()) {
				add(newNode);
			} else if (selected.size() == 2) {
				add(linkNodes);
				add(doMaxFlow);
			}
		}
	}

	public GraphPanel() {
		graph = new Graph<>();
		posData = new HashMap<>();
		selected = new LinkedHashSet<>();

		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		GraphRMouseMenu menu = new GraphRMouseMenu();

		MouseAdapter mouseListener = new MouseAdapter() {
			private void displayRightClickMenu(MouseEvent e) {
				if (selected.isEmpty()) {
					UUID node = getNodeUnderMouse(e);
					if (node != null) {
						selectNode(node, posData.get(node));
					}
				}
				locCache = e.getPoint();
				menu.updateItems();
				menu.show(GraphPanel.this, e.getX(), e.getY());
			}

			private void selectNode(UUID node, Point p) {
				selected.add(node);
				repaint(p.x - NODE_RADIUS, p.y - NODE_RADIUS, 4*NODE_RADIUS, 4*NODE_RADIUS);
			}

			private UUID getNodeUnderMouse(MouseEvent e) {
				Vector2 mousePos = new Vector2(-NODE_RADIUS, -NODE_RADIUS).plus(e.getPoint());

				for (Map.Entry<UUID, Point> node: posData.entrySet()) {
					Point pos = node.getValue();

					double dist = mousePos.minus(pos).magnitude();

					if (dist < NODE_RADIUS) {
						return node.getKey();
					}
				}

				return null;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					displayRightClickMenu(e);
				} else {
					clickedNode = getNodeUnderMouse(e);
					dragStartLoc = e.getPoint();

					if (e.getButton() == 1) {
						if (!e.isShiftDown() && !selected.isEmpty()) {
							selected.clear();
							repaint();
						}

						if (clickedNode != null) {
							Point pos = posData.get(clickedNode);
							selectNode(clickedNode, pos);

							posCache = new HashMap<>(posData);
						} else {
							alreadySelected = new HashSet<>(selected);
						}
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					displayRightClickMenu(e);
				}
				if (alreadySelected != null) {
					rectSelection = null;
					alreadySelected = null;
					repaint();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				Vector2 drag = new Vector2(e.getPoint()).minus(dragStartLoc);

				if (alreadySelected != null) {
					rectSelection = new Rectangle2D.Double(Math.min(dragStartLoc.getX(), e.getX()),
					                                       Math.min(dragStartLoc.getY(), e.getY()),
					                                       Math.abs(dragStartLoc.getX() - e.getX()),
					                                       Math.abs(dragStartLoc.getY() - e.getY()));

					Vector2 offset = new Vector2(NODE_RADIUS, NODE_RADIUS);
					for (Map.Entry<UUID, Point> node: posData.entrySet()) {
						Vector2 pos = offset.plus(node.getValue());

						if (rectSelection.contains(pos.getX(), pos.getY())) {
							selected.add(node.getKey());
						} else if (!alreadySelected.contains(node.getKey())) {
							selected.remove(node.getKey());
						}
					}

					repaint();
				}

				if (selected.contains(clickedNode)) {
					for (UUID node: selected) {
						posData.put(node, drag.plus(posCache.get(node)).asPoint());
					}
					repaint();
				} else if (clickedNode != null) {
					selected.clear();
					selected.add(clickedNode);
					posData.put(clickedNode, drag.plus(posCache.get(clickedNode)).asPoint());
					repaint();
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
			}
		};

		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}

	private static Stroke SELECTED_STROKE = new BasicStroke(5);
	private static Color  SELECTED_COLOR  = Color.BLUE;

	private static Stroke DEFAULT_STROKE = new BasicStroke(2);
	private static Color  DEFAULT_COLOR  = Color.BLACK;

	private static Stroke EDGE_STROKE = new BasicStroke(2);
	private static Color  EDGE_COLOR  = Color.BLACK;

	private static Polygon arrowHead = new Polygon();

	static {
		arrowHead.addPoint(0, 0);
		arrowHead.addPoint(-10, 10);
		arrowHead.addPoint(-10, -10);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (graph == null) { return; }

		Graphics2D g2d = (Graphics2D) g.create();

		for (UUID vertex: graph) {
			Vector2 sourcePos = new Vector2(NODE_RADIUS, NODE_RADIUS).plus(posData.get(vertex));
			AffineTransform tx = new AffineTransform();
			for (Graph<UUID>.Edge edge: graph.getVertex(vertex).get().outgoingEdges()) {
				Graphics2D localContext = (Graphics2D) g2d.create();

				Vector2 destPos = new Vector2(NODE_RADIUS, NODE_RADIUS).plus(posData.get(edge.getDestination().getId()));

				Vector2 offset = destPos.minus(sourcePos).normalized().times(NODE_RADIUS);
				Vector2 edgeStart = sourcePos.plus(offset);
				Vector2 edgeEnd   = destPos.minus(offset);

				localContext.setStroke(EDGE_STROKE);
				localContext.setColor(EDGE_COLOR);

				localContext.drawLine((int) edgeStart.getX(), (int) edgeStart.getY(),
						(int) edgeEnd.getX(),   (int) edgeEnd.getY());

				Vector2 edgeCenter = sourcePos.lerp(destPos, 0.5);
				Vector2 textOffset = new Vector2(-offset.getY(), offset.getX()).normalized().times(15);
				if (textOffset.getX() < 0) {
					textOffset = textOffset.times(-1);
				}
				Vector2 textPos = edgeCenter.plus(textOffset);
				localContext.drawString(String.valueOf(edge.getWeight()), (float) textPos.getX(), (float) textPos.getY());

				tx.setToIdentity();
				tx.translate(edgeEnd.getX(), edgeEnd.getY());
				tx.rotate(offset.getX(), offset.getY());

				localContext.transform(tx);
				localContext.fill(arrowHead);
			}
		}

		for (UUID vertex: graph) {
			if (selected.contains(vertex)) {
				g2d.setStroke(SELECTED_STROKE);
				g2d.setColor(SELECTED_COLOR);
			} else {
				g2d.setStroke(DEFAULT_STROKE);
				g2d.setColor(DEFAULT_COLOR);
			}

			Point p = posData.get(vertex);
			g2d.drawOval(p.x, p.y, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
		}

		if (rectSelection != null) {
			g2d.setStroke(SELECTED_STROKE);
			g2d.setColor(SELECTED_COLOR);

			g2d.draw(rectSelection);
		}
	}
}
