package edu.deanza.cis22c.w2018.team1.structure.graph;

import edu.deanza.cis22c.w2018.team1.structure.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;

// --- Vertex class ------------------------------------------------------
public class Vertex<E> {
	// Intentionally package private to make it usable by the graph.
	// One of the reasons I'd prefer Vertex to be an inner class.
	Set<Vertex<E>> incomingEdges = new HashSet<>();

	private HashMap<E, Pair<Vertex<E>, Double>> adjList = new HashMap<>();
	private E data;
	private boolean visited;

	protected Vertex( E data) {
		this.data = data;
	}

	public E getData() { return data; }

	public boolean isVisited() { return visited; }

	public void visit() { visited = true; }

	public void unvisit() { visited = false; }

	/**
	 * Gets the vertex's adjacency list in the form of a map
	 *
	 * @return   an unmodifiable map representing the vertex's adjacency list
	 */
	public Map<E, Pair<Vertex<E>, Double>> getAdjList() {
		return Collections.unmodifiableMap(adjList);
	}


	public void addToAdjListOrUpdate(Vertex<E> neighbor, double cost) {
		Pair<Vertex<E>, Double> existingEdge = adjList.get(neighbor.getData());
		if (existingEdge == null) {
			adjList.put(neighbor.getData(), new Pair<>(neighbor, cost));
			neighbor.incomingEdges.add(this);
		} else {
			existingEdge.setRight(cost);
		}
	}

	public OptionalDouble getCostTo(Vertex<E> neighbor) {
		Pair<Vertex<E>, Double> edge = adjList.get(neighbor.getData());
		if (edge != null) {
			return OptionalDouble.of(edge.getRight());
		} else {
			return OptionalDouble.empty();
		}
	}

	public boolean removeFromAdjList(Vertex<E> neighbor) {
		neighbor.incomingEdges.remove(this);
		return adjList.remove(neighbor.getData()) != null;
	}

	/**
	 * Prints the vertex's adjacency list to System.out
	 */
	public void showAdjList() {
		// May be preferable to have it take a Writer instead of using System.out directly
		Iterator<Map.Entry<E, Pair<Vertex<E>, Double>>> iter;
		Map.Entry<E, Pair<Vertex<E>, Double>> entry;
		Pair<Vertex<E>, Double> pair;

		System.out.print( "Adj List for " + getData() + ": ");
		iter = adjList.entrySet().iterator();
		while( iter.hasNext() )
		{
			entry = iter.next();
			pair = entry.getValue();
			System.out.print( pair.getLeft().getData() + "("
					+ String.format("%3.1f", pair.getRight())
					+ ") " );
		}
		System.out.println();
	}

}

