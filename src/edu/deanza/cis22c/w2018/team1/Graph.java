package edu.deanza.cis22c.w2018.team1;

import edu.deanza.cis22c.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

// --- edu.deanza.cis22c.w2018.team1.Vertex class ------------------------------------------------------
class Vertex<E> {
	public HashMap<E, Pair<Vertex<E>, Double>> adjList
			= new HashMap<>();
	public E data;

	public Vertex(E x) {
		data = x;
	}

	public Vertex() {
		this(null);
	}

	public E getData() {
		return data;
	}

	public Iterator<Entry<E, Pair<Vertex<E>, Double>>> iterator() {
		return adjList.entrySet().iterator();
	}

	public void addToAdjList(Vertex<E> neighbor, double cost) {
		if (adjList.get(neighbor.data) == null)
			adjList.put(neighbor.data, new Pair<>(neighbor, cost));
		// Note: if you want to change the cost, you'll need to remove it and then add it back
	}

	public void addToAdjList(Vertex<E> neighbor, int cost) {
		addToAdjList(neighbor, (double) cost);
	}

	public boolean equals(Object rhs) {
		if (!(rhs instanceof Vertex<?>))
			return false;
		Vertex<?> other = (Vertex<?>) rhs;

		return Objects.equals(data, other.data);

	}

	public int hashCode() {
		return (data.hashCode());
	}

	public void showAdjList() {
		Iterator<Entry<E, Pair<Vertex<E>, Double>>> iter;
		Entry<E, Pair<Vertex<E>, Double>> entry;
		Pair<Vertex<E>, Double> pair;

		System.out.print("Adj List for " + data + ": ");
		iter = adjList.entrySet().iterator();
		while (iter.hasNext()) {
			entry = iter.next();
			pair = entry.getValue();
			System.out.print(pair.first.data + "("
					+ String.format("%3.1f", pair.second)
					+ ") ");
		}
		System.out.println();
	}

}

//--- edu.deanza.cis22c.w2018.team1.Graph class ------------------------------------------------------
public class Graph<E> {
	// the graph data is all here --------------------------
	protected HashMap<E, Vertex<E>> vertexSet;

	// public graph methods --------------------------------
	public Graph() {
		vertexSet = new HashMap<>();
	}

	public void addEdge(E source, E dest, double cost) {
		Vertex<E> src, dst;

		// put both source and dest into vertex list(s) if not already there
		src = addToVertexSet(source);
		dst = addToVertexSet(dest);

		// add dest to source's adjacency list
		src.addToAdjList(dst, cost);
		//dst.addToAdjList(src, cost); // ADD THIS IF UNDIRECTED GRAPH
	}

	public void addEdge(E source, E dest, int cost) {
		addEdge(source, dest, (double) cost);
	}

	// adds vertex with x in it, and always returns ref to it
	public Vertex<E> addToVertexSet(E x) {
		Vertex<E> retVal;
		Vertex<E> foundVertex;

		// find if edu.deanza.cis22c.w2018.team1.Vertex already in the list:
		foundVertex = vertexSet.get(x);

		if (foundVertex != null) // found it, so return it
		{
			return foundVertex;
		}

		// the vertex not there, so create one
		retVal = new Vertex<>(x);
		vertexSet.put(x, retVal);

		return retVal;   // should never happen
	}

	public boolean remove(E start, E end) {
		Vertex<E> startVertex = vertexSet.get(start);
		boolean removedOK = false;

		if (startVertex != null) {
			Pair<Vertex<E>, Double> endPair = startVertex.adjList.remove(end);
			removedOK = endPair != null;
		}
	   /*// Add if UNDIRECTED GRAPH:
		edu.deanza.cis22c.w2018.team1.Vertex<E> endVertex = vertexSet.get(end);
		if( endVertex != null )
		{
			edu.deanza.cis22c.Pair<edu.deanza.cis22c.w2018.team1.Vertex<E>, Double> startPair = endVertex.adjList.remove(start);
			removedOK = startPair!=null ;
		}
		*/

		return removedOK;
	}

	public void showAdjTable() {
		Iterator<Entry<E, Vertex<E>>> iter;

		System.out.println("------------------------ ");
		iter = vertexSet.entrySet().iterator();
		while (iter.hasNext()) {
			(iter.next().getValue()).showAdjList();
		}
		System.out.println();
	}


	public void clear() {
		vertexSet.clear();
	}

	/**
	 * Breadth-first traversal from the parameter startElement
	 */
	public void breadthFirstTraversal(E startElement, Consumer<E> visitor) {
		Vertex<E> startVertex = vertexSet.get(startElement);

		LinkedQueue<Vertex<E>> vertexQueue = new LinkedQueue<>();
		vertexQueue.enqueue(startVertex);

		Set<Vertex<E>> visited = new HashSet<>();

		while (!vertexQueue.isEmpty()) {
			Vertex<E> nextVertex = vertexQueue.dequeue();

			visited.add(nextVertex);
			visitor.accept(nextVertex.getData());

			nextVertex.iterator().forEachRemaining((e) -> {
				Vertex<E> neighbor = e.getValue().first;
				if (!visited.contains(nextVertex)) {
					vertexQueue.enqueue(neighbor);
				}
			});
		}
	}

	/**
	 * Depth-first traversal from the parameter startElement
	 */
	public void depthFirstTraversal(E startElement, Consumer<E> visitor) {
		Vertex<E> startVertex = vertexSet.get(startElement);

		LinkedStack<Vertex<E>> vertexStack = new LinkedStack<>();
		vertexStack.push(startVertex);

		Set<Vertex<E>> visited = new HashSet<>();

		while (!vertexStack.isEmpty()) {
			Vertex<E> nextVertex = vertexStack.pop();

			visited.add(nextVertex);
			visitor.accept(nextVertex.getData());

			nextVertex.iterator().forEachRemaining((e) -> {
				Vertex<E> neighbor = e.getValue().first;
				if (!visited.contains(nextVertex)) {
					vertexStack.push(neighbor);
				}
			});
		}
	}


// WRITE THE INSTANCE METHOD HERE TO
	//         WRITE THE GRAPH's vertices and its
	//         adjacency list TO A TEXT FILE (SUGGEST TO PASS AN
	//        ALREADY OPEN PrintWriter TO THIS) !


}
