package edu.deanza.cis22c.w2018.team1.structure.graph;

import edu.deanza.cis22c.w2018.team1.structure.Pair;
import edu.deanza.cis22c.w2018.team1.structure.queue.LinkedQueue;
import edu.deanza.cis22c.w2018.team1.structure.stack.LinkedStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.Stream;

// All changes to graph class by Dimitriye Danilovic

public class Graph<E> implements Iterable<E> {
	// the graph data is all here --------------------------
	private HashMap<E, Vertex<E> > vertexSet = new HashMap<>();

	/**
	 * Adds an edge between the given vertices,
	 * creating them if necessary.
	 *
	 * @param   source   the id of the source vertex
	 * @param   dest     the id of the destination vertex
	 * @param   cost     the cost of the edge
	 */
	public void addEdgeOrUpdate(E source, E dest, double cost) {
		Vertex<E> src, dst;

		// put both source and dest into vertex list(s) if not already there
		src = addToVertexSet(source);
		dst = addToVertexSet(dest);

		// add dest to source's adjacency list
		src.addToAdjListOrUpdate(dst, cost);
	}

	/**
	 * Gets the cost of the edge between the given vertices
	 * if one exists.
	 *
	 * @param   source   the id of the source vertex
	 * @param   dest     the id of the destination vertex
	 *
	 * @return   the cost if the edge exists, OptionalDouble.empty() otherwise
	 */
	public OptionalDouble getEdgeCost(E source, E dest) {
		return Optional.ofNullable(vertexSet.get(dest))
				.map(vertexSet.get(source)::getCostTo).orElse(OptionalDouble.empty());
	}

	/**
	 * Returns an unmodifiable set containing all direct
	 * successors of the provided vertex. This set is
	 * backed by the graph, and will remain valid even
	 * if edges are removed from the graph, however
	 * its iterator will not.
	 *
	 * @param   source   the source vertex whose successors to retrieve
	 * @return   the successor set if the vertex exists, Optional.empty() otherwise
	 */
	public Optional<Set<E>> getDirectSuccessors(E source) {
		return Optional.ofNullable(vertexSet.get(source))
				.map(Vertex::getAdjList)
				.map(Map::keySet)
				.map(Collections::unmodifiableSet);
	}

	public Optional<Set<E>> getDirectPredecessors(E dest) {
		return Optional.ofNullable(vertexSet.get(dest))
				.map(v -> v.incomingEdges)
				.map(Collections::unmodifiableSet);
	}

	/**
	 * Removes the edge between the given vertices
	 * if present.
	 *
	 * @param   source   the data of the source vertex
	 * @param   dest     the data of the destination vertex
	 *
	 * @return   true if there was an edge to remove
	 */
	public boolean removeEdge(E source, E dest) {
		Vertex<E> startVertex = vertexSet.get(source);
		if (startVertex == null) { return false; }

		Vertex<E> endVertex = vertexSet.get(dest);

		return endVertex != null && startVertex.removeFromAdjList(endVertex);
	}

	// Meant to be overridden to provide the proper vertex subclass
	protected Vertex<E> makeVertex(E x) {
		return new Vertex<>(x);
	}

	/**
	 * Retrieves the vertex with the given data,
	 * creating it if necessary.
	 *
	 * @param   data   the data for the vertex
	 * @return   the vertex with the given data
	 */
	protected Vertex<E> addToVertexSet(E data) {
		Vertex<E> retVal;
		Vertex<E> foundVertex;

		// find if Vertex already in the list:
		foundVertex = vertexSet.get(data);

		if ( foundVertex != null ) // found it, so return it
		{
			return foundVertex;
		}

		// the vertex not there, so create one
		retVal = makeVertex(data);
		vertexSet.put(data, retVal);

		return retVal;   // should never happen
	}

	/**
	 * Adds to the vertex set a vertex with the given data
	 * if none is present.
	 *
	 * @param   data   the data for the vertex
	 * @return   true if a new vertex was created
	 */
	public boolean addVertex(E data) {
		boolean ret = !vertexSet.containsKey(data);

		addToVertexSet(data);

		return ret;
	}

	/**
	 * Returns true if the graph contains a vertex with the
	 * given data.
	 *
	 * @param   data   the data of the vertex to check for
	 * @return   true if the vertex exists
	 */
	public boolean containsVertex(E data) {
		return vertexSet.containsKey(data);
	}

	/**
	 * Removes the vertex with the given data
	 *
	 * @param   data   the data of the vertex to remove
	 * @return   true if there was a vertex to remove
	 */
	public boolean removeVertex(E data) {
		Vertex<E> vertex = vertexSet.get(data);
		if (vertex == null) { return false; }

		vertex.incomingEdges.forEach((v) -> removeEdge(v, data));
		vertexSet.remove(data);

		return true;
	}

	/**
	 * Prints the graph's adjacency table to System.out
	 */
	public void showAdjTable() {
		// It may be preferable to have this method accept a Writer object
		Iterator<Entry<E, Vertex<E>>> iter;

		System.out.println( "------------------------ ");
		iter = vertexSet.entrySet().iterator();
		while( iter.hasNext() )
		{
			(iter.next().getValue()).showAdjList();
		}
		System.out.println();
	}

	/**
	 * Clears the graph data so as to be logically equivalent
	 * to a newly created graph.
	 */
	public void clear() {
		vertexSet.clear();
	}

	protected void unvisitVertices() {
		// Could be replaced with the following one-liner:
		// vertexSet.valueSet().forEach(Vertex::unvisit)

		Iterator<Entry<E, Vertex<E>>> iter;

		iter = vertexSet.entrySet().iterator();
		while( iter.hasNext() )
		{
			iter.next().getValue().unvisit();
		}
	}

	/**
	 * Applies the provided visitor to a breadth-first traversal
	 *
	 * @param startElement the element to begin traversing from
	 * @param visitor
	 */
	public void breadthFirstTraversal(E startElement, Consumer<E> visitor)
	{
		unvisitVertices();

		Vertex<E> startVertex = vertexSet.get(startElement);
		breadthFirstTraversalHelper( startVertex, visitor );
	}

	/**
	 * Applies the provided visitor to a depth-first traversal
	 *
	 * @param startElement the element to begin traversing from
	 * @param visitor
	 */
	public void depthFirstTraversal(E startElement, Consumer<E> visitor)
	{
		unvisitVertices();

		Vertex<E> startVertex = vertexSet.get(startElement);
		depthFirstTraversalHelper( startVertex, visitor );
	}

	protected void breadthFirstTraversalHelper(Vertex<E> startVertex,
	                                           Consumer<E> visitor)
	{
		LinkedQueue<Vertex<E>> vertexQueue = new LinkedQueue<>();
		E startData = startVertex.getData();

		startVertex.visit();
		visitor.accept(startData);
		vertexQueue.enqueue(startVertex);
		while( !vertexQueue.isEmpty() ) {
			Vertex<E> nextVertex = vertexQueue.dequeue();
			Iterator<Map.Entry<E, Pair<Vertex<E>, Double>>> iter =
					nextVertex.getAdjList().entrySet().iterator(); // iterate adjacency list

			while( iter.hasNext() ) {
				Entry<E, Pair<Vertex<E>, Double>> nextEntry = iter.next();
				Vertex<E> neighborVertex = nextEntry.getValue().getLeft();
				if( !neighborVertex.isVisited() )
				{
					vertexQueue.enqueue(neighborVertex);
					neighborVertex.visit();
					visitor.accept(neighborVertex.getData());
				}
			}
		}
	} // end breadthFirstTraversalHelper

	public void depthFirstTraversalHelper(Vertex<E> startVertex, Consumer<E> visitor)
	{
		LinkedStack<Vertex<E>> vertexStack = new LinkedStack<>();
		E startData = startVertex.getData();

		startVertex.visit();
		visitor.accept(startData);
		vertexStack.push(startVertex);
		while( !vertexStack.isEmpty() ) {
			Vertex<E> nextVertex = vertexStack.pop();
			Iterator<Map.Entry<E, Pair<Vertex<E>, Double>>> iter =
					nextVertex.getAdjList().entrySet().iterator(); // iterate adjacency list

			while( iter.hasNext() ) {
				Entry<E, Pair<Vertex<E>, Double>> nextEntry = iter.next();
				Vertex<E> neighborVertex = nextEntry.getValue().getLeft();
				if( !neighborVertex.isVisited() )
				{
					vertexStack.push(neighborVertex);
					neighborVertex.visit();
					visitor.accept(neighborVertex.getData());
				}
			}
		}
	}

	@Override
	public Iterator<E> iterator() {
		return vertexSet.keySet().iterator();
	}

	public Stream<E> stream() {
		return vertexSet.keySet().stream();
	}

	/**
	 * Adds all vertices and edges from the given graph to this one,
	 * making this graph a supergraph of it.
	 *
	 * Any edge conflicts (where the same edge is present in both graphs)
	 * are resolved via the provided edgeMergeOperator, which should be
	 * a function of the form:
	 *
	 * (graphEdge, subgraphEdge) -> outputEdge
	 *
	 * @param   subgraph            the graph to add to this graph
	 * @param   edgeMergeOperator   the operator used to merge conflicting edges
	 */
	public void addSubgraph(Graph<E> subgraph, DoubleBinaryOperator edgeMergeOperator) {
		subgraph.vertexSet.values().forEach(
			vertex -> vertex.getAdjList().values().forEach(
				edge -> {
					Vertex<E> source = addToVertexSet(vertex.getData());
					Vertex<E> dest   = addToVertexSet(edge.getLeft().getData());

					OptionalDouble oldCost = source.getCostTo(dest);
					if (oldCost.isPresent()) {
						source.addToAdjListOrUpdate(dest,
								edgeMergeOperator.applyAsDouble(oldCost.getAsDouble(), edge.getRight()));
					} else {
						source.addToAdjListOrUpdate(dest, edge.getRight());
					}
				}));
	}

}

