package edu.deanza.cis22c.w2018.team1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.deanza.cis22c.w2018.team1.serialization.GraphSerializer;
import edu.deanza.cis22c.w2018.team1.structure.Pair;
import edu.deanza.cis22c.w2018.team1.structure.graph.Graph;
import edu.deanza.cis22c.w2018.team1.swing.ContextMenu;
import edu.deanza.cis22c.w2018.team1.swing.GraphPanel;
import edu.deanza.cis22c.w2018.team1.swing.GraphSelectionHandler;
import edu.deanza.cis22c.w2018.team1.swing.PredicateDecorator;
import edu.deanza.cis22c.w2018.team1.swing.overlay.EdgeWeightOverlay;
import edu.deanza.cis22c.w2018.team1.swing.overlay.VertexNameOverlay;
import edu.deanza.cis22c.w2018.team1.swing.tool.EdgeTool;
import edu.deanza.cis22c.w2018.team1.swing.tool.MaxFlowTool;
import edu.deanza.cis22c.w2018.team1.swing.tool.MaxFlowVisualizeTool;
import edu.deanza.cis22c.w2018.team1.swing.util.OrderedMouseListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class Main implements Runnable {
	public static void main(String... args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.invokeLater(new Main());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <E> JFrame buildGraphFrame(Supplier<E> vertIdSupplier, Type elemType) {
		JFrame frame = new JFrame();

		GraphPanel<E> pane = new GraphPanel<>();
		Graph<E> graph = new Graph<>();
		pane.setGraph(graph);

		JPanel layers = new JPanel() {
			@Override
			public boolean isOptimizedDrawingEnabled() {
				return false;
			}
		};
		OverlayLayout layout = new OverlayLayout(layers);
		layers.setLayout(layout);

		ContextMenu<Set<E>> rightClickMenu = new ContextMenu<>();

		GraphSelectionHandler<E> selector = new GraphSelectionHandler<>(pane);

		rightClickMenu.setContextSupplier(p -> selector.getSelection());

		JMenuItem newNode = rightClickMenu.addMenuItem(Set::isEmpty, e -> {
			E vertex = vertIdSupplier.get();
			pane.getGraph().addVertex(vertex);
			pane.setVertexPosition(vertex, e.getLocation());
			pane.repaint();
		});
		newNode.setText("New Node");

		OrderedMouseListener listeners = new OrderedMouseListener();

		JMenuItem addEdge = rightClickMenu.addMenuItem(s -> s.size() == 1, e -> {
			EdgeTool<E> tool = new EdgeTool<>(pane);
			tool.setSource(e.getContext().iterator().next());

			layers.add(tool.getOverlay(), 0);
			layers.revalidate();
			pane.repaint();

			listeners.addListener(tool);

			tool.addToolListener(() -> {
				layers.remove(tool.getOverlay());
				listeners.removeListener(tool);
				layers.revalidate();
				layers.repaint();
			});
		});
		addEdge.setText("Add edge");

		JMenuItem maxFlow = rightClickMenu.addMenuItem(s -> s.size() == 1, e -> {
			MaxFlowTool<E> tool = new MaxFlowTool<>(pane, e.getContext().iterator().next());

			listeners.addListener(tool);

			tool.addToolListener( () -> listeners.removeListener(tool) );
		});
		maxFlow.setText("Find max flow to");

		JMenuItem vMaxFlow = rightClickMenu.addMenuItem(s -> s.size() == 1, e -> {
			MaxFlowVisualizeTool<E> tool = new MaxFlowVisualizeTool<>(frame, pane, e.getContext().iterator().next());

			listeners.addListener(tool);

			tool.addToolListener( () -> listeners.removeListener(tool) );
		});
		vMaxFlow.setText("Visualize max flow");

		JMenuItem delete = rightClickMenu.addMenuItem( s -> !s.isEmpty(), e -> {
			e.getContext().forEach(pane.getGraph()::removeVertex);
			pane.repaint();
		} );
		delete.setText("Delete");

		listeners.addListener(rightClickMenu.getTriggerListener());
		listeners.addListener(selector);
		pane.addMouseListener(listeners);
		pane.addMouseMotionListener(listeners);

		pane.addVertexDecorator(new PredicateDecorator<>(selector.stylingPredicate(), new Pair<>(new BasicStroke(5), Color.BLUE)));

		layers.add(pane);

		selector.addBoxListener(new GraphSelectionHandler.SelectionBoxListener() {
			private JPanel pane;
			private Rectangle2D rect;

			@Override
			public void selectionStarted() {
				pane = new JPanel() {
					@Override
					public void paintComponent(Graphics g) {
						super.paintComponent(g);

						if (rect == null) { return; }

						Graphics2D g2d = (Graphics2D) g;
						g2d.setStroke(new BasicStroke(3));
						g2d.setColor(Color.BLUE);

						g2d.draw(rect);
					}
				};
				pane.setOpaque(false);
				layers.add(pane, 0);
				layers.revalidate();
				layers.repaint();
			}

			@Override
			public void selectionChanged(Rectangle2D rect) {
				if (this.rect != null) {
					pane.repaint(this.rect.getBounds());
				} else {
					pane.repaint(rect.getBounds());
				}
				this.rect = rect;
			}

			@Override
			public void selectionEnded() {
				layers.remove(pane);
				layers.revalidate();
				layers.repaint();
				pane = null;
				rect = null;
			}
		});

		frame.setContentPane(layers);

		frame.setJMenuBar(buildMenuBar(frame, pane, layers, elemType));

		return frame;
	}

	private static <E> JMenuBar buildMenuBar(JFrame frame, GraphPanel<E> pane, JPanel layers, Type elemType) {
		JMenuBar menuBar = new JMenuBar();

		JMenu file = new JMenu("File");
		JFileChooser fileChooser = new JFileChooser();

		JMenuItem open = new JMenuItem("Open");
		open.addActionListener(e -> {
			int ret = fileChooser.showOpenDialog(frame);

			if (ret == JFileChooser.APPROVE_OPTION) {
				File in = fileChooser.getSelectedFile();
				try (BufferedReader reader = new BufferedReader(new FileReader(in))) {
					Pair<Graph<E>, Map<E, Point2D>> data = readFile(reader, elemType);
					pane.setGraph(data.getLeft());
					pane.addPositionData(data.getRight());

					pane.getGraph().forEach(v -> {
						if (!pane.getVertexPosition(v).isPresent()) {
							pane.setVertexPosition(v, new Point2D.Double(0, 0));
						}
					});

					pane.repaint();
				} catch (IOException ex) {} // Do nothing if the file's not found
			}
		});
		file.add(open);

		JMenuItem save = new JMenuItem("Save");
		save.addActionListener(e -> {
			int ret = fileChooser.showSaveDialog(frame);

			if (ret == JFileChooser.APPROVE_OPTION) {
				File in = fileChooser.getSelectedFile();
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(in))) {
					writeToFile(pane.getGraph(), writer, pane.getPositionTable(), elemType);
				} catch (IOException ex) {} // Do nothing if the file's not found
			}
		});
		file.add(save);

		menuBar.add(file);

		JMenu view = new JMenu("View");

		JCheckBoxMenuItem edgeWeights = new JCheckBoxMenuItem("Edge weights");
		ActionListener edgeWeightsAL =
				makeOverlayCheckboxActionListener(edgeWeights, layers, pane, new EdgeWeightOverlay<>(pane));
		edgeWeights.addActionListener(edgeWeightsAL);
		edgeWeights.setState(true);
		edgeWeightsAL.actionPerformed(null);

		view.add(edgeWeights);

		JCheckBoxMenuItem vertexNames = new JCheckBoxMenuItem("Vertex names");
		ActionListener vertexNamesAL =
				makeOverlayCheckboxActionListener(vertexNames, layers, pane, new VertexNameOverlay<>(pane));
		vertexNames.addActionListener(vertexNamesAL);
		vertexNames.setState(true);
		vertexNamesAL.actionPerformed(null);

		view.add(vertexNames);

		menuBar.add(view);

		return menuBar;
	}

	private static ActionListener makeOverlayCheckboxActionListener(JCheckBoxMenuItem checkbox, JPanel layers,
	                                                                GraphPanel<?> graphPanel, JComponent overlay) {
		Runnable repainter = overlay::repaint;
		return e -> {
			if (checkbox.getState()) {
				layers.add(overlay, 0);
				graphPanel.addRepaintListener(repainter);
				layers.revalidate();
				layers.repaint();
			} else {
				layers.remove(overlay);
				graphPanel.removeRepaintListener(repainter);
				layers.revalidate();
				layers.repaint();
			}
		};
	}

	private static Gson gson;

	static {
		GsonBuilder gBuilder = new GsonBuilder();
		gBuilder.registerTypeAdapter(Graph.class, new GraphSerializer());
		gBuilder.setPrettyPrinting();
		gson = gBuilder.create();
	}

	private static <E> Pair<Graph<E>, Map<E, Point2D>> readFile(Reader s, Type elemType) {
		Type graphType = new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return new Type[]{elemType};
			}

			@Override
			public Type getRawType() {
				return Graph.class;
			}

			@Override
			public Type getOwnerType() {
				return null;
			}
		};

		JsonParser parser = new JsonParser();
		JsonObject obj = parser.parse(s).getAsJsonObject();

		Graph<E> graph = gson.fromJson(obj, graphType);
		Map<E, Point2D> knownPositions = new HashMap<>();
		JsonArray vertices = obj.getAsJsonArray("vertices");

		vertices.forEach((eVert) -> {
			JsonObject oVert = eVert.getAsJsonObject();
			Point2D.Double p = gson.fromJson(oVert.get("coordinates"), Point2D.Double.class);
			if (p != null) {
				E id = gson.fromJson(oVert.get("id"), elemType);
				knownPositions.put(id, p);
			}
		});

		return new Pair<>(graph, knownPositions);
	}

	private static <E> void writeToFile(Graph<E> g, Writer s, Map<E, Point2D> posMap, Type elemType) {
		Type graphType = new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return new Type[]{elemType};
			}

			@Override
			public Type getRawType() {
				return Graph.class;
			}

			@Override
			public Type getOwnerType() {
				return null;
			}
		};

		JsonObject obj = gson.toJsonTree(g, graphType).getAsJsonObject();
		JsonArray vertices = obj.getAsJsonArray("vertices");

		vertices.forEach((eVert) -> {
			JsonObject oVert = eVert.getAsJsonObject();
			Point2D pos = posMap.get(gson.fromJson(oVert.get("id"), elemType));
			oVert.add("coordinates", gson.toJsonTree(pos));
		});

		gson.toJson(obj, s);
	}

	@Override
	public void run() {
		JFrame frame = buildGraphFrame(() -> JOptionPane.showInputDialog("Input new node name:"), String.class);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}