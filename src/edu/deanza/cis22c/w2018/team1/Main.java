package edu.deanza.cis22c.w2018.team1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.deanza.cis22c.w2018.team1.serialization.GraphSerializer;
import edu.deanza.cis22c.w2018.team1.structure.Pair;
import edu.deanza.cis22c.w2018.team1.structure.Triple;
import edu.deanza.cis22c.w2018.team1.structure.graph.Graph;
import edu.deanza.cis22c.w2018.team1.structure.graph.maxflow.MaxFlow;
import edu.deanza.cis22c.w2018.team1.structure.stack.LinkedStack;
import edu.deanza.cis22c.w2018.team1.structure.stack.StackInterface;

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
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Scanner;

//TODO: Use final data type
public class Main {
	public static void main(String[] args) {
		Graph<String> g = null;
		final StackInterface<Triple<String, String, Double>> undoStack = new LinkedStack<>();
		boolean unsaved = false;
		boolean running = true;

		while (true) {
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
					Graph<String> finalG = g;
					unsaved = createVertex(g, "Enter the edge source").flatMap(
						source -> createVertex(finalG, "Enter the edge destination").map(
							dest -> {
								OptionalDouble d = requestNumber("Enter the weight");
								d.ifPresent(weight -> finalG.addEdgeOrUpdate(source, dest, weight));
								return d.isPresent();
							})).orElse(false);

					continue;
				} else if (input == 2) {
					Graph<String> finalG = g;
					requestVertex(g, "Enter the edge source").ifPresent(
						source -> requestVertex(finalG, "Enter the edge destination").ifPresent(
							dest -> {
								OptionalDouble weight = finalG.getVertex(source).getCostTo(finalG.getVertex(dest));
								weight.ifPresent(w -> undoStack.push(new Triple<>(source, dest, w)));

								if (finalG.remove(source, dest)) {
									System.out.println("Successfully removed edge");
								} else {
									System.out.println("No such edge");
								}
							}
						));

					unsaved = true;
					continue;
				} else if (input == 3) {
					if (undoStack.isEmpty()) {
						System.out.println("No removals to undo");
						continue;
					}
					Triple<String, String, Double> edgeToRestore = undoStack.pop();
					g.addEdgeOrUpdate(edgeToRestore.getLeft(), edgeToRestore.getMiddle(), edgeToRestore.getRight());

					continue;
				} else if (input == 4) {
					Graph<String> finalG = g;
					requestVertex(g, "Enter vertex to traverse from")
							.ifPresent(f -> finalG.depthFirstTraversal(f, System.out::print));

					continue;
				} else if (input == 5) {
					Graph<String> finalG = g;
					requestVertex(g, "Enter vertex to traverse from")
							.ifPresent(f -> finalG.breadthFirstTraversal(f, System.out::print));

					continue;
				} else if (input == 6) {
					g.showAdjTable();

					continue;
				} else if (input == 7) {
					Graph<String> finalG = g;
					requestVertex(g, "Enter the source").ifPresent(
						source -> requestVertex(finalG, "Enter the sink").ifPresent(
							dest -> {
								MaxFlow.findMaximumFlow(finalG, source, dest).ifPresent(
									flow -> {
										System.out.println("Maximum flow is " + flow);
										if (requestYesNo("Would you like to log this to a file?")) {
											openOutputFile(true).ifPresent(
												writer -> {
													try {
														if (writer.getRight()) { // is new file
															writer.getLeft().write("source,sink,max flow");
														}
														writer.getLeft().write(source + "," + dest + "," + flow);
														writer.getLeft().close();
													} catch (IOException e) {
														throw new RuntimeException(e);
													}
												});
										}
									}
								);
							}));

					continue;
				} else if (input == 8) {
					Optional<Writer> oWriter = openOutputFile().map(Pair::getLeft);
					if (oWriter.isPresent()) {
						try (Writer writer = oWriter.get()) {
							writeGraph(g, writer);
							unsaved = false;
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}

					continue;
				} else if (input == 9) {
					if (unsaved) {
						if (!requestYesNo("Graph has unsaved changes, are you sure you want to continue?")) {
							continue;
						}
					}

					undoStack.clear();
					g = null;

					continue;
				} else if (input == 0) {
					if (unsaved) {
						if (requestYesNo("Graph has unsaved changes, are you sure you want to continue?")) {
							break;
						}
					} else {
						break;
					}
				}
			} else {
				if (input == 1) {
					g = readFile().orElse(null);

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
	
	private static Graph<String> readGraph(Reader s) {
		Type graphType = new TypeToken<Graph<String>>() {}.getType();
		return gson.fromJson(s, graphType);
	}

	private static Optional<Graph<String>> readFile() {
		return openInputFile().map(reader -> {
			Graph<String> ret = readGraph(reader);
			if (ret == null) {
				System.out.println("Could not read graph from file.");
			}
			try {
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return ret;
		});
	}

	private static void writeGraph(Graph<String> g, Writer s) {
		gson.toJson(g, s);
	}

	public static Scanner userScanner = new Scanner(System.in);

	private static OptionalDouble requestNumber(String query) {
		System.out.print(query + ": ");
		while (true) {
			String line = userScanner.nextLine();
			if ("".equals(line)) {
				System.out.println("Cancelling operation.");
				return OptionalDouble.empty();
			}
			try {
				return OptionalDouble.of(Double.valueOf(line));
			} catch (NumberFormatException e) {
				System.out.println("Please enter a valid number.");
			}
		}
	}

	private static boolean requestYesNo(String query) {
		System.out.println(query + " (y/n)");
		while (true) {
			char response = userScanner.nextLine().charAt(0);

			if (response == 'n' || response == 'N') {
				return false;
			} else if (response == 'y' || response == 'Y') {
				return true;
			}

			System.out.print("Please respond yes or no: ");
		}
	}

	private static Optional<String> requestVertex(Graph<String> graph, String query) {
		System.out.print(query + ": ");
		String name = userScanner.nextLine();
		if ("".equals(name)) {
			System.out.println("Cancelling operation.");
			return Optional.empty();
		} else if (graph.getVertex(name) == null) {
			System.out.println("No such vertex.");
			return Optional.empty();
		} else {
			return Optional.of(name);
		}
	}

	private static Optional<String> createVertex(Graph<String> graph, String query) {
		System.out.print(query + ": ");
		String name = userScanner.nextLine();
		if ("".equals(name)) {
			System.out.println("Cancelling operation.");
			return Optional.empty();
		} else {
			return Optional.of(name);
		}
	}

	/**
	 * Opens a file for input
	 * @return A reader for the file
	 */
	private static Optional<Reader> openInputFile() {
		String filename;

		System.out.print("Enter filepath: ");
		filename = userScanner.nextLine();
		File file = new File(filename);

		if (file.exists()) {
			try {
				return Optional.of(new BufferedReader(new FileReader(file)));
			} catch (FileNotFoundException e) {
				throw new RuntimeException("ERROR: UNREACHABLE STATE REACHED");
			}
		} else {
			System.out.println("File does not exist.");
			return Optional.empty();
		}
	}

	/**
	 * Opens a file for output
	 * @return a writer for the file and true if a new file was created
	 */
	private static Optional<Pair<Writer, Boolean>> openOutputFile(boolean append) {
		String filename;

		System.out.print("Enter file path: ");
		filename = userScanner.nextLine();
		File file = new File(filename);

		try {
			boolean exists = file.exists();
			return Optional.of(new Pair<>(new BufferedWriter(new FileWriter(file, append)), !exists));
		} catch (IOException e) {
			System.out.println("Could not open file because:");
			System.out.println(e);

			return Optional.empty();
		}
	}

	private static Optional<Pair<Writer, Boolean>> openOutputFile() {
		return openOutputFile(false);
	}
}
