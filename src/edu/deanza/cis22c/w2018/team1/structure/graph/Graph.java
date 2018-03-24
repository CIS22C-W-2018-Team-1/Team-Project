package edu.deanza.cis22c.w2018.team1.structure.graph;

import edu.deanza.cis22c.w2018.team1.structure.Pair;
import edu.deanza.cis22c.w2018.team1.structure.queue.LinkedQueue;
import edu.deanza.cis22c.w2018.team1.structure.stack.LinkedStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

// All changes to graph class by Dimitriye Danilovic

public class Graph<E> {
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
	 * Retrieves the vertex with the given data,
	 * creating it if necessary.
	 *
	 * @param   x   the data for the vertex
	 * @return   the vertex with the given data
	 */
	public Vertex<E> addToVertexSet(E x) {
		Vertex<E> retVal;
		Vertex<E> foundVertex;

		// find if Vertex already in the list:
		foundVertex = vertexSet.get(x);

		if ( foundVertex != null ) // found it, so return it
		{
			return foundVertex;
		}

		// the vertex not there, so create one
		retVal = new Vertex<>(x);
		vertexSet.put(x, retVal);

		return retVal;   // should never happen
	}

	/**
	 * Gets the vertex with the given data if present
	 *
	 * @param   x   the data for which to get a vertex
	 *
	 * @return   the vertex if found, null otherwise
	 */
	public Vertex<E> getVertex(E x) {
		return vertexSet.get(x);
	}

	/**
	 * Removes the vertex with the given data
	 *
	 * @param   x   the data of the vertex to remove
	 * @return   true if there was a vertex to remove
	 */
	public boolean removeVertex(E x) {
		Vertex<E> vertex = getVertex(x);
		if (vertex == null) { return false; }

		vertex.incomingEdges.forEach((v) -> v.removeFromAdjList(vertex));
		vertexSet.remove(x);

		return true;
	}

	/**
	 * Removes the edge between the given vertices
	 * if present.
	 *
	 * @param   start   the data of the source vertex
	 * @param   end     the data of the destination vertex
	 *
	 * @return   true if there was an edge to remove
	 */
	public boolean remove(E start, E end) {
		Vertex<E> startVertex = vertexSet.get(start);
		if (startVertex == null) { return false; }

		Vertex<E> endVertex = vertexSet.get(end);

		return endVertex != null && startVertex.removeFromAdjList(endVertex);
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
	 * Gets the graph's vertex set as an unmodifiable map.
	 *
	 * @return   an unmodifiable map representing the graph's vertex set
	 */
	public Map<E, Vertex<E>> getVertexSet() {
		// It may be desirable to make the map modifiable through
		// a wrapper layer which preserves the graph's invariants
		// however I believe this falls outside the scope of permitted modifications
		return Collections.unmodifiableMap(vertexSet);
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


// WRITE THE INSTANCE METHOD HERE TO
	//         WRITE THE GRAPH's vertices and its
	//         adjacency list TO A TEXT FILE (SUGGEST TO PASS AN
	//        ALREADY OPEN PrintWriter TO THIS) !


}

