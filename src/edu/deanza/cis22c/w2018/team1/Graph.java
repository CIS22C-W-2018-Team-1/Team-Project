package edu.deanza.cis22c.w2018.team1;

import edu.deanza.cis22c.Pair;
import edu.deanza.cis22c.QueueInterface;
import edu.deanza.cis22c.StackInterface;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Graph<E> implements Iterable<E> {
	public class Vertex {
		private HashMap<E, Pair<Vertex, Double>> adjList
				= new HashMap<>();
		private E data;

		private Vertex(E x) {
			data = x;
		}

		private Vertex() {
			this(null);
		}

		public E getData() {
			return data;
		}

		public Iterator<Pair<Vertex, Double>> edges() {
			return adjList.values().iterator();
		}

		public void addToAdjList(Vertex neighbor, double cost) {
			if (adjList.get(neighbor.data) == null)
				adjList.put(neighbor.data, new Pair<>(neighbor, cost));
			// Note: if you want to change the cost, you'll need to remove it and then add it back
		}

		public void showAdjList() {
			Iterator<Entry<E, Pair<Vertex, Double>>> iter;
			Entry<E, Pair<Vertex, Double>> entry;
			Pair<Vertex, Double> pair;

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

	// the graph data is all here --------------------------
	private HashMap<E, Vertex> vertexSet;

	// public graph methods --------------------------------
	public Graph() {
		vertexSet = new HashMap<>();
	}

	public void addEdge(E source, E dest, double cost) {
		Vertex src, dst;

		// put both source and dest into vertex list(s) if not already there
		src = addToVertexSet(source);
		dst = addToVertexSet(dest);

		// add dest to source's adjacency list
		src.addToAdjList(dst, cost);
		//dst.addToAdjList(src, cost); // ADD THIS IF UNDIRECTED GRAPH
	}

	// adds vertex with x in it, and always returns ref to it
	public Vertex addToVertexSet(E x) {
		Vertex retVal;
		Vertex foundVertex;

		// find if edu.deanza.cis22c.w2018.team1.Vertex already in the list:
		foundVertex = vertexSet.get(x);

		if (foundVertex != null) // found it, so return it
		{
			return foundVertex;
		}

		// the vertex not there, so create one
		retVal = new Vertex(x);
		vertexSet.put(x, retVal);

		return retVal;   // should never happen
	}

	public Optional<Vertex> getVertexIfPresent(E e) {
		return Optional.ofNullable(vertexSet.get(e));
	}

	public boolean remove(E start, E end) {
		Vertex startVertex = vertexSet.get(start);
		boolean removedOK = false;

		if (startVertex != null) {
			Pair<Vertex, Double> endPair = startVertex.adjList.remove(end);
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
		Iterator<Entry<E, Vertex>> iter;

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

	@Override
	public Iterator<E> iterator() {
		return breadthFirstIterator();
	}

	private class GraphIterator implements Iterator<E> {
		Consumer<Vertex> addToPool;
		Supplier<Vertex> pollPool;
		BooleanSupplier isPoolEmpty;

		Set<Vertex> visited = new HashSet<>();

		GraphIterator(Consumer<Vertex> addToPool, Supplier<Vertex> pollPool, BooleanSupplier isPoolEmpty) {
			this.addToPool = addToPool;
			this.pollPool = pollPool;
			this.isPoolEmpty = isPoolEmpty;
		}

		@Override
		public boolean hasNext() {
			return !isPoolEmpty.getAsBoolean();
		}

		@Override
		public E next() {
			Vertex next = pollPool.get();

			next.edges().forEachRemaining((e) -> {
				Vertex neighbor = e.first;
				if (visited.add(neighbor)) {
					addToPool.accept(neighbor);
				}
			});

			return next.getData();
		}
	}

	private Iterator<E> _breadthFirstIterator(Vertex startVertex) {
		QueueInterface<Vertex> queue = new LinkedQueue<>();

		queue.enqueue(startVertex);

		return new GraphIterator(queue::enqueue, queue::dequeue, queue::isEmpty);
	}

	/**
	 * Returns an edges which iterates over the graph in
	 * breadth-first order, starting at the specified element.
	 *
	 * @param   startElement   the element of the graph to iterate from
	 * @return   an Iterator
	 */
	public Iterator<E> breadthFirstIterator(E startElement) {
		Vertex startVertex = vertexSet.get(startElement);
		if (startVertex != null) {
			return _breadthFirstIterator(startVertex);
		} else {
			return Collections.emptyIterator();
		}
	}

	/**
	 * Returns an edges which iterates over the graph in
	 * breadth-first order, starting at an arbitrary element.
	 *
	 * @return   an Iterator
	 */
	public Iterator<E> breadthFirstIterator() {
		if (vertexSet.isEmpty()) {
			return Collections.emptyIterator();
		} else {
			return _breadthFirstIterator(vertexSet.values().iterator().next());
		}
	}

	private Iterator<E> _depthFirstIterator(Vertex startVertex) {
		StackInterface<Vertex> stack = new LinkedStack<>();

		stack.push(startVertex);

		return new GraphIterator(stack::push, stack::pop, stack::isEmpty);
	}

	/**
	 * Returns an edges which iterates over the graph in
	 * depth-first order, starting at the specified element.
	 *
	 * @param   startElement   the element of the graph to iterate from
	 * @return   an Iterator
	 */
	public Iterator<E> depthFirstIterator(E startElement) {
		Vertex startVertex = vertexSet.get(startElement);
		if (startVertex != null) {
			return _depthFirstIterator(startVertex);
		} else {
			return Collections.emptyIterator();
		}
	}

	/**
	 * Returns an edges which iterates over the graph in
	 * depth-first order, starting at an arbitrary element.
	 *
	 * @return   an Iterator
	 */
	public Iterator<E> depthFirstIterator() {
		if (vertexSet.isEmpty()) {
			return Collections.emptyIterator();
		} else {
			return _depthFirstIterator(vertexSet.values().iterator().next());
		}
	}

	/**
	 * Breadth-first traversal from the parameter startElement
	 *
	 * @deprecated   use {@link #breadthFirstIterator(Object) breadthFirstIterator} instead
	 */
	@Deprecated
	public void breadthFirstTraversal(E startElement, Consumer<E> visitor) {
		breadthFirstIterator(startElement).forEachRemaining(visitor);
	}

	/**
	 * Depth-first traversal from the parameter startElement
	 *
	 * @deprecated   use {@link #depthFirstIterator(Object) depthFirstIterator} instead
	 */
	@Deprecated
	public void depthFirstTraversal(E startElement, Consumer<E> visitor) {
		depthFirstIterator(startElement).forEachRemaining(visitor);
	}


// WRITE THE INSTANCE METHOD HERE TO
	//         WRITE THE GRAPH's vertices and its
	//         adjacency list TO A TEXT FILE (SUGGEST TO PASS AN
	//        ALREADY OPEN PrintWriter TO THIS) !


}
