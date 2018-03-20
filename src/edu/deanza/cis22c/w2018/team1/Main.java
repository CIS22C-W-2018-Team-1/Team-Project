package edu.deanza.cis22c.w2018.team1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Scanner;
import java.util.stream.Collectors;

import edu.deanza.cis22c.w2018.team1.Graph.Edge;
import edu.deanza.cis22c.w2018.team1.Graph.Vertex;

//TODO: Use final data type
public class Main {
	public static void main(String[] args) {
		boolean running = true;
		while (running) {
			
			Scanner s = null;
			while (s == null) {
				s = openInputFile();
			}
			
			Graph<String> g = new Graph<String>();
			ArrayStack<Edge> undoStack = new ArrayStack<Edge>();
			
			
			while (running) {
				System.out.println("MAIN MENU - Choose an option: ");
				System.out.println("1. Read the Graph File");
				System.out.println("2. Add an edge");
				System.out.println("3. Remove an edge");
				System.out.println("4. Undo previous Removal");
				System.out.println("5. Display the graph using depth-first traversal");
				System.out.println("6. Display the graph using breadth-first traversal");
				System.out.println("7. Display the graph's adjaceny list");
				System.out.println("8. Find the maximum flow");
				System.out.println("9. Save the graph to a txt file");
				System.out.println("0. End run");
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
				
				if (input == 1) {
					//TODO: Change for final data type
					readFile(s, g);
					
				} else if (input == 2) {
					System.out.println("\nEnter the source (first vertex): ");
					
					String source = userScanner.nextLine();
					Vertex vSource = g.getOrCreateVertex(source);
					
					System.out.println("Enter the destination (second vertex): ");
					String dest = userScanner.nextLine();
					Vertex vDest = g.getOrCreateVertex(dest);
					
					System.out.println("Enter the weight: ");
					double weight;
					while (true) {
						try {
							weight = userScanner.nextDouble();
							userScanner.nextLine();
							break;
						} catch (InputMismatchException e) {
							System.out.println("Please enter a number.");
							userScanner.nextLine();
						}
					}
					vSource.createOrUpdateEdgeTo(vDest, weight);
					
				} else if (input == 3) {
					System.out.println("\nEnter the first vertex: ");
					String source = userScanner.nextLine();
					System.out.println("Enter the second vertex: ");
					String dest = userScanner.nextLine();
					boolean edgeExists = g.getOrCreateVertex(source).getEdgeTo(g.getOrCreateVertex(dest)).isPresent();
					if (edgeExists == true) {
						undoStack.push(g.getOrCreateVertex(source).getEdgeTo(g.getOrCreateVertex(dest)).get());
						System.out.println("Successfully removed edge. ");
						g.getOrCreateVertex(source).getEdgeTo(g.getOrCreateVertex(dest)).get().remove();
					} else {
						System.out.println("Input Error");
					}
					
				} else if (input == 4) {
					Edge edgeToRestore = undoStack.pop();
					edgeToRestore.getSource().createOrUpdateEdgeTo(edgeToRestore.getDestination(), edgeToRestore.getWeight());
					
				} else if (input == 5) {
					//TODO
					Iterator<String> dFirst = g.depthFirstIterator();
					while (dFirst.hasNext()) {
						System.out.println(dFirst.next());
					}
					
				} else if (input == 6) {
					//TODO
					Iterator<String> bFirst = g.breadthFirstIterator();
					while (bFirst.hasNext()) {
						System.out.println(bFirst.next());
					}
					
				} else if (input == 7) {
					g.showAdjTable();
					
				} else if (input == 8) {
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
						System.out.println("Cannot find valid flow. ");
					}
					
				} else if (input == 9) {
					try {
						openOutputFile(g);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				} else if (input == 0) {
					running = false;
				} else {
					System.out.println("Error, enter a proper input.");
				}
			}
			while (true) {
				System.out.println("Would you like to test another input file? (y/n)");
				String response = userScanner.nextLine();
				if (response.equals("y")) {
					running = true;
					break;
				} else if (response.equals("n")) {
					break;
				}
			}
		}
		System.out.println("Program Ended.");
	}
	
	@SuppressWarnings("unchecked")
	public static void readFile(Scanner s, Graph g) {
		while (s.hasNextLine()) {
			String add = s.nextLine();
			g.getOrCreateVertex(add);
		}
	}
	
	public static Scanner userScanner = new Scanner(System.in);
	
	public static void openOutputFile(Graph g) throws IOException {
		System.out.println("Enter a file name to save the graph: ");
		String name = userScanner.nextLine();
		File file = new File(name);
		try {
			if (file.createNewFile()){
				System.out.println("File was created.");
			}else{
				System.out.println("Adding to existing file.");
			}
			
    	} catch (IOException e) {
	      e.printStackTrace();
    	}
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		
		Iterator iter = g.iterator();

		while (iter.hasNext()) {
			Vertex v = (Vertex) g.getVertex(iter.next()).get();
			writer.write(
					"Adj List for " + v.getId() + ": "
					+ v.outgoingEdges().parallelStream().map(
							(e) -> ((Edge)e).getDestination().getId()
									+ String.format("(%3.1f)", ((Edge)e).getWeight()))
					.collect(Collectors.joining(", "))
			);
			writer.write("\n");
		}
		
		writer.close();
	}
	
	
	// opens a text file for input, returns a Scanner:
	public static Scanner openInputFile()
	{
		String filename;
        Scanner scanner = null;
        
		System.out.print("Enter the input filename: ");
		filename = userScanner.nextLine();
        	File file= new File(filename);

        	try{
        		scanner = new Scanner(file);
        	}// end try
        	catch(FileNotFoundException fe){
        	   System.out.println("Can't open input file\n");
       	    return null; // array of 0 elements
        	} // end catch
        	return scanner;
	}
}
