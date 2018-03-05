package edu.deanza.cis22c.w2018.team1;

import edu.deanza.cis22c.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.ToDoubleBiFunction;

public class AStar<E> {
	private ToDoubleBiFunction<Vertex<E>, Vertex<E>> heuristic;

	private static ToDoubleBiFunction<?, ?> zeroHeuristic = (v, w)->0;
	/**
	 * Constructs an A* processor with the given heuristic.
	 * @param heuristic the desired heuristic
	 */
	@SuppressWarnings("unchecked")
	public AStar(ToDoubleBiFunction<Vertex<E>, Vertex<E>> heuristic) {
		if (heuristic == null) {
			heuristic = (ToDoubleBiFunction<Vertex<E>, Vertex<E>>) zeroHeuristic;
		}
		this.heuristic = heuristic;
	}

	/**
	 * Constructs an A* processor with no heuristic. This is in effect Dijkstra's algorithm.
	 */
	@SuppressWarnings("unchecked")
	public AStar() {
		heuristic = (ToDoubleBiFunction<Vertex<E>, Vertex<E>>) zeroHeuristic;
	}

	/**
	 * A utility class for the return value of
	 * @param <E>
	 */
	public static class Path<E> {
		private List<Vertex<E>> nodeList;
		private double length;

		private Path(List<Vertex<E>> nodeList, double length) {
			this.nodeList = Collections.unmodifiableList(nodeList);
			this.length = length;
		}

		public List<Vertex<E>> getNodeList() {
			return nodeList;
		}

		public double getLength() {
			return length;
		}
	}

	public Path<E> findPath(Vertex<E> source, Vertex<E> dest) {
		// SortedSet acts as priority queue for frontier
		PriorityQueue<Pair<Vertex<E>, Double>> frontier = new PriorityQueue<>(Comparator.comparing((p) -> p.second));

		Map<Vertex<E>, Vertex<E>> backtrace = new HashMap<>();
		Map<Vertex<E>, Double> routeCost = new HashMap<>();

		frontier.add(new Pair<>(source, 0.0));
		routeCost.put(source, 0.0);

		while (!frontier.isEmpty()) {
			Pair<Vertex<E>, Double> currentPair = frontier.poll();
			Vertex<E> current = currentPair.first;

			// Check for identity
			if (current == dest) { break; }

			current.edges().forEachRemaining((edge) -> {
				double cumCost = routeCost.get(current) + edge.second;
				Vertex<E> next = edge.first;
				Double oldCost = routeCost.get(next);

				// Second condition will never occur so long as heuristic is admissible
				if (oldCost == null || cumCost < oldCost) {
					routeCost.put(next, cumCost);
					frontier.add(new Pair<>(next, cumCost + heuristic.applyAsDouble(next, dest)));
					backtrace.put(next, current);
				}
			});
		}

		LinkedList<Vertex<E>> pathList = new LinkedList<>();
		pathList.addFirst(dest);

		Vertex<E> current = backtrace.get(dest);

		if (current == null) {
			return null; // source and dest are not elements of contiguous graph
		}

		// Walk backward through the discovered path, adding it to the list
		while (current != null) {
			pathList.addFirst(current);
			current = backtrace.get(current);
		}

		return new Path<>(pathList, routeCost.get(dest));
	}
}
