package edu.deanza.cis22c.w2018.team1;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

public class GraphPanel extends JPanel {
	private Graph<UUID> graph;
	private Map<UUID, Point> posData;
	private LinkedHashSet<UUID> selected;

	private static final int NODE_RADIUS = 25;

	private class GraphRMouseMenu extends JPopupMenu {

		private Point locCache = null;

		private JMenuItem newNode;
		private JMenuItem linkNodes;

		public GraphRMouseMenu() {
			newNode = new JMenuItem("New Node");
			newNode.addActionListener((e) -> {
				UUID node = UUID.randomUUID();
				graph.addToVertexSet(node);
				Point panelLoc = GraphPanel.this.getLocationOnScreen();
				posData.put(node, new Point(locCache.x - panelLoc.x, locCache.y - panelLoc.y));
				GraphPanel.this.repaint();
			});

			linkNodes = new JMenuItem("Link nodes");
			linkNodes.addActionListener((e) -> {
				Double weight = Double.valueOf(JOptionPane.showInputDialog("Edge weight"));
				Iterator<UUID> iter = selected.iterator();
				UUID first = iter.next();
				UUID second = iter.next();

				graph.addEdge(first, second, weight);

				Point firstPos = posData.get(first);
				Point secondPos = posData.get(second);

				if (firstPos != null && secondPos != null) {
					int redrawStartX = Math.min(firstPos.x, secondPos.x);
					int redrawStartY = Math.min(firstPos.y, secondPos.y);
					int redrawEndX = Math.max(firstPos.x, secondPos.x);
					int redrawEndY = Math.max(firstPos.y, secondPos.y);

					GraphPanel.this.repaint(redrawStartX - NODE_RADIUS, redrawStartY - NODE_RADIUS,
					                        redrawEndX - redrawStartX + 4 * NODE_RADIUS,
					                        redrawEndY - redrawStartY + 4 * NODE_RADIUS);
				}

				selected.clear();
			});

			addPopupMenuListener(new PopupMenuListener() {
				@Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
				@Override public void popupMenuCanceled(PopupMenuEvent e) { }

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					locCache = getLocationOnScreen();
				}
			});
		}

		public void updateItems() {
			removeAll();

			if (selected.isEmpty()) {
				add(newNode);
			} else if (selected.size() == 2) {
				add(linkNodes);
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

		addMouseListener(new MouseAdapter() {
			private void displayRightClickMenu(MouseEvent e) {
				if (selected.isEmpty()) {
					UUID node = getNodeUnderMouse(e);
					if (node != null) {
						selectNode(node, posData.get(node));
					}
				}
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
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					displayRightClickMenu(e);
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);

				if (!e.isShiftDown() && !selected.isEmpty()) {
					for (UUID node: selected) {
						Point p = posData.get(node);

						repaint(p.x - NODE_RADIUS, p.y - NODE_RADIUS, 4*NODE_RADIUS, 4*NODE_RADIUS);
					}
					selected.clear();
				}

				UUID node = getNodeUnderMouse(e);

				if (node != null) {
					Point pos = posData.get(node);
					selectNode(node, pos);
				}
			}
		});
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
			graph.getVertexIfPresent(vertex).get().edges().forEachRemaining((edge) -> {
				Graphics2D localContext = (Graphics2D) g2d.create();

				Vector2 destPos = new Vector2(NODE_RADIUS, NODE_RADIUS).plus(posData.get(edge.getLeft().getData()));

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
				localContext.drawString(String.valueOf(edge.getRight()), (float) textPos.getX(), (float) textPos.getY());

				tx.setToIdentity();
				tx.translate(edgeEnd.getX(), edgeEnd.getY());
				tx.rotate(offset.getX(), offset.getY());

				localContext.transform(tx);
				localContext.fill(arrowHead);
			});
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
	}
}