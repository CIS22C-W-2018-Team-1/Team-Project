package edu.deanza.cis22c.w2018.team1;

import edu.deanza.cis22c.QueueInterface;
import edu.deanza.cis22c.StackInterface;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Graph<E> implements Iterable<E> {
	private Edge makeEdgeBetween(Vertex source, Vertex destination, double weight) {
		Edge edge = new Edge(source, destination, weight);

		source.outgoingEdges.put(destination, edge);
		destination.incomingEdges.put(source, edge);

		return edge;
	}

	public class Vertex {
		private HashMap<Vertex, Edge> outgoingEdges = new HashMap<>();
		private HashMap<Vertex, Edge> incomingEdges = new HashMap<>();

		private E id;

		private Vertex(E x) {
			id = x;
		}

		public E getId() {
			return id;
		}

		/**
		 * Retrieves a collection containing all outgoing edges
		 * from this vertex.
		 *
		 * Note: This collection is unmodifiable. Attempts to modify
		 *       it will throw a runtime exception.
		 *
		 * @return   the outgoing edges from this vertex
		 */
		public Collection<Edge> outgoingEdges() {
			return Collections.unmodifiableCollection(outgoingEdges.values());
		}

		/**
		 * Retrieves a collection containing all incoming edges
		 * to this vertex.
		 *
		 * Note: This collection is unmodifiable. Attempts to modify
		 *       it will throw a runtime exception.
		 *
		 * @return   the incoming edges to this vertex
		 */
		public Collection<Edge> incomingEdges() {
			return Collections.unmodifiableCollection(incomingEdges.values());
		}

		/**
		 * Checks if an edge exists from this vertex to the given one.
		 *
		 * @param   destination   the destination to check for
		 * @return   true if the edge exists, false otherwise
		 */
		public boolean hasEdgeTo(Vertex destination) {
			return outgoingEdges.containsKey(destination);
		}

		/**
		 * Retrieves the edge from this vertex to the given one
		 * if present in an {@link java.util.Optional Optional}. Otherwise returns an empty {@code Optional}
		 *
		 * @param   destination   the destination whose edge to retrieve
		 * @return   an {@code Optional} containing the edge if present
		 */
		public Optional<Edge> getEdgeTo(Vertex destination) {
			return Optional.ofNullable(outgoingEdges.get(destination));
		}

		/**
		 * Remove this vertex and all connected edges from the graph.
		 */
		public void remove() {
			incomingEdges().forEach(Edge::remove);
			outgoingEdges().forEach(Edge::remove);

			Graph.this.vertexSet.remove(id, this);
		}

		private boolean isOwnedBy(Graph<E> graph) {
			return Graph.this == graph;
		}

		/**
		 * Creates an edge to the given vertex, or updates it
		 * to the given weight if already present.
		 *
		 * @param   destination   the vertex to which to create / update the edge
		 * @param   weight   the desired weight of the edge
		 *
		 * @return   the created / modified edge
		 */
		public Edge createOrUpdateEdgeTo(Vertex destination, double weight) {
			if (!destination.isOwnedBy(Graph.this)) {
				throw new IllegalArgumentException(this + " and " + destination + " are not in the same graph");
			}

			Edge edge = outgoingEdges.get(destination);

			if (edge == null) {
				edge = makeEdgeBetween(this, destination, weight);
			} else {
				edge.setWeight(weight);
			}

			return edge;
		}

		public void showAdjList() {
			System.out.println("Adj List for " + id + ": " + outgoingEdges().parallelStream()
					.map((e) -> e.getDestination().getId() + String.format("(%3.1f)", e.getWeight()))
					.collect(Collectors.joining(", "))
			);
		}
	}

	public class Edge {
		private Vertex source, destination;
		private double weight;

		private Edge(Vertex source, Vertex destination, double weight) {
			this.source = source;
			this.destination = destination;
			this.weight = weight;
		}

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}

		public Vertex getSource() {
			return source;
		}

		public Vertex getDestination() {
			return destination;
		}

		/**
		 * Removes this edge from the graph.
		 */
		public void remove() {
			source.outgoingEdges.remove(destination);
			destination.outgoingEdges.remove(source);
		}
	}

	// the graph id is all here --------------------------
	private HashMap<E, Vertex> vertexSet = new HashMap<>();

	// public graph methods --------------------------------
	public Graph() { }

	/**
	 * Creates a graph which is a copy of the original
	 *
	 * @param   original   the original to copy
	 */
	public Graph(Graph<E> original) {
		for (Vertex vertex: original.vertexSet.values()) {
			Vertex newVertex = getOrCreateVertex(vertex.getId());
			for (Edge edge: vertex.outgoingEdges.values()) {
				Vertex destVertex = getOrCreateVertex(edge.getDestination().getId());
				newVertex.createOrUpdateEdgeTo(destVertex, edge.weight);
			}
		}
	}

	/**
	 * Returns vertex with given identifier if present,
	 * creates and returns it otherwise.
	 *
	 * @param   id   the id of the vertex to get / create
	 * @return   the vertex
	 */
	public Vertex getOrCreateVertex(E id) {
		Vertex vertex;

		vertex = vertexSet.get(id);

		if (vertex == null) {
			vertex = new Vertex(id);
			vertexSet.put(id, vertex);
		}

		return vertex;
	}

	/**
	 * Returns an {@link java.util.Optional Optional} containing the vertex with the
	 * given identifier if present, or an empty one otherwise.
	 *
	 * @param   id   the id of the vertex to attempt to retrieve
	 * @return an {@code Optional} containing the vertex if present
	 */
	public Optional<Vertex> getVertex(E id) {
		return Optional.ofNullable(vertexSet.get(id));
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
	/**
	 * Returns an iterator which is guaranteed to iterate
	 * over all vertices of the graph, even if discontiguous,
	 * but does so in an arbitrary order.
	 *
	 * @return   an Iterator
	 */
	public Iterator<E> iterator() {
		return vertexSet.keySet().iterator();
	}

	public Map<E, Vertex> vertices() {
		return Collections.unmodifiableMap(vertexSet);
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

			for (Edge edge: next.outgoingEdges()) {
				Vertex destination = edge.getDestination();
				if (visited.add(destination)) {
					addToPool.accept(destination);
				}
			}

			return next.getId();
		}
	}

	private Iterator<E> _breadthFirstIterator(Vertex startVertex) {
		QueueInterface<Vertex> queue = new LinkedQueue<>();

		queue.enqueue(startVertex);

		return new GraphIterator(queue::enqueue, queue::dequeue, queue::isEmpty);
	}

	/**
	 * Returns an iterator which iterates over the graph in
	 * breadth-first order, starting at the specified element.
	 *
	 * Note: This iterator will not iterate over all elements of a
	 *       discontiguous graph.
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
	 * Returns an iterator which iterates over the graph in
	 * breadth-first order, starting at an arbitrary element.
	 *
	 * Note: This iterator will not iterate over all elements of a
	 *       discontiguous graph.
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
	 * Returns an iterator which iterates over the graph in
	 * depth-first order, starting at the specified element.
	 *
	 * Note: This iterator will not iterate over all elements of a
	 *       discontiguous graph.
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
	 * Returns an iterator which iterates over the graph in
	 * depth-first order, starting at an arbitrary element.
	 *
	 * Note: This iterator will not iterate over all elements of a
	 *       discontiguous graph.
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
