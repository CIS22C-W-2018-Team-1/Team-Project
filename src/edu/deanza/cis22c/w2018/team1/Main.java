package edu.deanza.cis22c.w2018.team1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.deanza.cis22c.w2018.team1.Graph.Vertex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Scanner;

//TODO: Use final data type
public class Main {
	public static void main(String[] args) {
		Graph<String> g = null;
		ArrayStack<Graph<String>.Edge> undoStack = new ArrayStack<Graph<String>.Edge>();
		boolean unsaved = false;
		boolean running = true;

		MAIN_LOOP: while (true) {
			System.out.println("MAIN MENU - Choose an option: ");
			if (g == null) {
				System.out.println("1. Open a graph from a file");
				System.out.println("2. Create new graph");
			} else {
				System.out.println("1. Add an edge or change edge weight");
				System.out.println("2. Remove an edge");
				System.out.println("3. Undo previous Removal");
				System.out.println("4. Display the graph using depth-first traversal");
				System.out.println("5. Display the graph using breadth-first traversal");
				System.out.println("6. Display the graph's adjacency list");
				System.out.println("7. Find the maximum flow");
				System.out.println("8. Save graph to file");
				System.out.println("9. Close graph");
			}
			System.out.println("0. Exit program");
			int input;
			while (true) {
				try {
					input = userScanner.nextInt();
					userScanner.nextLine();
					break;
				} catch (InputMismatchException e) {
					System.out.println("Please enter a number.");
					userScanner.nextLine();
				}
			}

			if (g != null) {
				if (input == 1) {
					System.out.println("\nEnter the source (first vertex): ");

					String source = userScanner.nextLine();
					if ("".equals(source)) {
						System.out.println("Cancelling operation");
						continue;
					}
					Graph<String>.Vertex vSource = g.getOrCreateVertex(source);

					System.out.println("Enter the destination (second vertex): ");
					String dest = userScanner.nextLine();
					if ("".equals(dest)) {
						System.out.println("Cancelling operation");
						continue;
					}
					Graph<String>.Vertex vDest = g.getOrCreateVertex(dest);

					System.out.println("Enter the weight: ");
					double weight;
					while (true) {
						String line = userScanner.nextLine();
						if ("".equals(line)) {
							System.out.println("Cancelling operation");
							continue MAIN_LOOP;
						}
						try {
							weight = Double.valueOf(line);
							break;
						} catch (NumberFormatException e) {
							System.out.println("Please enter a valid number.");
						}
					}
					vSource.createOrUpdateEdgeTo(vDest, weight);
					unsaved = true;
					continue;
				} else if (input == 2) {
					System.out.println("\nEnter the source (first vertex): ");

					String source = userScanner.nextLine();
					if ("".equals(source)) {
						System.out.println("Cancelling operation");
						continue;
					}

					Optional<Graph<String>.Vertex> oSource = g.getVertex(source);
					if (!oSource.isPresent()) {
						System.out.println("No such vertex");
						continue;
					}
					Graph<String>.Vertex vSource = oSource.get();

					System.out.println("Enter the destination (second vertex): ");
					String dest = userScanner.nextLine();
					if ("".equals(dest)) {
						System.out.println("Cancelling operation");
						continue;
					}

					Optional<Graph<String>.Vertex> oDest = g.getVertex(dest);
					if (!oDest.isPresent()) {
						System.out.println("No such vertex");
						continue;
					}
					Graph<String>.Vertex vDest = oDest.get();

					Optional<Graph<String>.Edge> oEdge = vSource.getEdgeTo(vDest);

					oEdge.ifPresent(Graph.Edge::remove);

					if (oEdge.isPresent()) {
						System.out.println("Successfully removed edge");
					} else {
						System.out.println("No such edge");
					}

					unsaved = true;
					continue;
				} else if (input == 3) {
					Graph<String>.Edge edgeToRestore = undoStack.pop();
					edgeToRestore.getSource().createOrUpdateEdgeTo(edgeToRestore.getDestination(), edgeToRestore.getWeight());

					continue;
				} else if (input == 4) {
					//TODO
					Iterator<String> dFirst = g.depthFirstIterator();
					while (dFirst.hasNext()) {
						System.out.println(dFirst.next());
					}

					continue;
				} else if (input == 5) {
					//TODO
					Iterator<String> bFirst = g.breadthFirstIterator();
					while (bFirst.hasNext()) {
						System.out.println(bFirst.next());
					}

					continue;
				} else if (input == 6) {
					g.showAdjTable();

					continue;
				} else if (input == 7) {
					System.out.println("\nEnter the source (first vertex): ");
					String source = userScanner.nextLine();
					Vertex vSource = g.getOrCreateVertex(source);
					source = (String) vSource.getId();

					System.out.println("Enter the destination (second vertex): ");
					String dest = userScanner.nextLine();
					Vertex vDest = g.getOrCreateVertex(dest);
					dest = (String) vDest.getId();

					OptionalDouble flow = MaxFlow.findMaximumFlow(g, source, dest);
					if (flow.isPresent()) {
						System.out.println("The max flow is: " + flow.getAsDouble());
					} else {
						System.out.println("Canceled finding Max Flow.");
					}

					continue;
				} else if (input == 8) {
					try (Writer writer = openOutputFile()) {
						writeToFile(g, writer);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}

					continue;
				} else if (input == 9) {
					if (unsaved) {
						System.out.println("Graph has unsaved changes, are you sure you want to continue? (y/n)");
						while (true) {
							char response = userScanner.nextLine().charAt(0);

							if (response == 'n' || response == 'N') {
								continue MAIN_LOOP;
							} else if (response == 'y' || response == 'Y') {
								break;
							}

							System.out.print("Please respond y or n:");
						}
					}

					undoStack = new ArrayStack<>();
					g = null;

					continue;
				} else if (input == 0) {
					if (unsaved) {
						System.out.println("Graph has unsaved changes, are you sure you want to continue? (y/n)");
						while (true) {
							char response = userScanner.nextLine().charAt(0);

							if (response == 'n' || response == 'N') {
								continue MAIN_LOOP;
							} else if (response == 'y' || response == 'Y') {
								break MAIN_LOOP;
							}

							System.out.print("Please respond y or n:");
						}
					} else {
						break;
					}
				}
			} else {
				if (input == 1) {
					Reader reader = openInputFile();
					if (reader != null) {
						g = readFile(reader);
						try {
							reader.close();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}

					continue;
				} else if (input == 2) {
					g = new Graph<>();

					continue;
				} else if (input == 0) {
					break;
				}
			}
			System.out.println("");
		}
		System.out.println("Program Ended.");
	}

	private static Gson gson;

	static {
		GsonBuilder gBuilder = new GsonBuilder();
		gBuilder.registerTypeAdapter(Graph.class, new GraphSerializer());
		gBuilder.setPrettyPrinting();
		gson = gBuilder.create();
	}
	
	private static Graph<String> readFile(Reader s) {
		Type graphType = new TypeToken<Graph<String>>() {}.getType();
		return gson.fromJson(s, graphType);
	}

	private static void writeToFile(Graph<String> g, Writer s) {
		gson.toJson(g, s);
	}
	
	public static Scanner userScanner = new Scanner(System.in);

	/**
	 * Opens a file for input
	 * @return A reader for the file
	 */
	private static Reader openInputFile() {
		String filename;

		System.out.print("Enter filepath: ");
		filename = userScanner.nextLine();
		File file = new File(filename);

		if (file.exists()) {
			try {
				return new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				throw new RuntimeException("ERROR: UNREACHABLE STATE REACHED");
			}
		} else {
			System.out.println("File does not exist.");
			return null;
		}
	}

	/**
	 * Opens a file for output
	 * @return A writer for the file
	 */
	private static Writer openOutputFile() {
		String filename;

		System.out.print("Enter file path: ");
		filename = userScanner.nextLine();
		File file = new File(filename);

		try {
			return new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			System.out.println(e);
		}

		return null;
	}
}
