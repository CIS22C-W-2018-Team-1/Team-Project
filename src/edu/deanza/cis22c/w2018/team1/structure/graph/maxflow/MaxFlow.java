package edu.deanza.cis22c.w2018.team1.structure.graph.maxflow;

import edu.deanza.cis22c.w2018.team1.structure.Pair;
import edu.deanza.cis22c.w2018.team1.structure.graph.Graph;
import edu.deanza.cis22c.w2018.team1.structure.graph.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An implementation of Dinic's algorithm for finding maximal flows.
 *
 * @param   <E>   the graph vertex data type
 *
 * @author Dimitriye Danilovic
 */
public class MaxFlow<E> extends Graph<E> {
	private MaxFlowVertex<E> source, dest;

	private boolean done;
	private double totalFlow = 0.0;
	private Graph<E> lastFlowGraph;
	private Graph<E> totalFlowGraph = new Graph<>();

	@Override
	protected Vertex<E> makeVertex(E x) {
		return new MaxFlowVertex<>(x);
	}

	private MaxFlow() {}

	public MaxFlow(Graph<E> graph, E source, E dest) {
		this.addSubgraph(graph, (l, r) -> r);

		this.source = (MaxFlowVertex<E>) addToVertexSet(source);
		this.dest   = (MaxFlowVertex<E>) addToVertexSet(dest);
	}

	public boolean isDone() {
		return done;
	}

	public double getTotalFlow() {
		return totalFlow;
	}

	public Map<E, Integer> getLastLevels() {
		return stream().collect( Collectors.toMap(Function.identity(),
		                                          e -> ((MaxFlowVertex<E>) addToVertexSet(e)).getLevel()) );
	}

	public Graph<E> getAccessibilityGraph() {
		Graph<E> ret = new Graph<>();

		breadthFirstTraversal(source.getData(),
			(s) -> {
				MaxFlowVertex<E> v = (MaxFlowVertex<E>) addToVertexSet(s);
				addToVertexSet(s).getAdjList().forEach(
					(d, e) -> {
						if (((MaxFlowVertex<E>) e.getLeft()).getLevel() > v.getLevel())
							ret.addEdgeOrUpdate(s, d, e.getRight());
					});
			});

		return ret;
	}

	public Graph<E> getLastFlowGraph() {
		return lastFlowGraph;
	}

	public Graph<E> getTotalFlowGraph() {
		return totalFlowGraph;
	}

	/**
	 * Computes the levels via a breadth first search
	 */
	public void computeLevels() {
		unvisitVertices();
		source.visit();

		int level = 1;

		List<Vertex<E>> currentLevel = Collections.singletonList(source);
		List<Vertex<E>> nextLevel = new ArrayList<>();
		while (!currentLevel.isEmpty()) {
			final int lvl = level;
			final List<Vertex<E>> next = nextLevel;
			currentLevel.forEach((vertex) ->
					vertex.getAdjList().values().stream().map(Pair::getLeft).forEachOrdered((dest) -> {
						if (!dest.isVisited()) {
							dest.visit();
							next.add(dest);
							((MaxFlowVertex<E>) dest).setLevel(lvl);
						}
					}));

			++level;
			currentLevel = nextLevel;
			nextLevel = new ArrayList<>();
		}
	}

	/**
	 * Greedily probes one flow
	 *
	 * @return   the flow
	 */
	private Optional<Pair<Double, List<Pair<E, E>>>> computeGreedyAugmentingFlow() {
		unvisitVertices();
		source.visit();

		Map<MaxFlowVertex<E>, MaxFlowVertex<E>> backtrackMap = new HashMap<>();
		Map<MaxFlowVertex<E>, Double> flowAtVertex = new HashMap<>();
		flowAtVertex.put(source, Double.POSITIVE_INFINITY);

		// Straightforward greedy search
		MaxFlowVertex<E> currentVertex = source;
		double flowCache = Double.POSITIVE_INFINITY;
		while (currentVertex != null && currentVertex != dest) {
			int currentLevel = currentVertex.getLevel();

			Optional<Pair<Vertex<E>, Double>> nextEdge = currentVertex.getAdjList().values().stream()
					.filter((p) -> !p.getLeft().isVisited())
					.filter((p) -> ((MaxFlowVertex<E>) p.getLeft()).getLevel() > currentLevel)
					.reduce((l, r) -> l.getRight() > r.getRight() ? l : r);

			if (!nextEdge.isPresent()) {
				currentVertex = backtrackMap.get(currentVertex);
				if (currentVertex != null) {
					flowCache = flowAtVertex.get(currentVertex);
				}
			} else {
				MaxFlowVertex<E> nextVertex = (MaxFlowVertex<E>) nextEdge.get().getLeft();
				double edgeFlow = nextEdge.get().getRight();

				nextVertex.visit();
				backtrackMap.put(nextVertex, currentVertex);
				flowCache = Math.min(flowCache, edgeFlow);
				flowAtVertex.put(nextVertex, flowCache);
				currentVertex = nextVertex;
			}
		}

		if (currentVertex == null) { return Optional.empty(); }

		LinkedList<Pair<E, E>> ret = new LinkedList<>();

		// Invert the edges in the path to allow for backtracking later
		while (currentVertex != source) {
			MaxFlowVertex<E> prevVertex = backtrackMap.get(currentVertex);

			double capacity = prevVertex.getCostTo(currentVertex).getAsDouble();
			double newCapacity = capacity - flowCache;
			if (newCapacity > 0.0) {
				prevVertex.addToAdjListOrUpdate(currentVertex, newCapacity);
			} else {
				prevVertex.removeFromAdjList(currentVertex);
			}

			OptionalDouble extantFlowThroughEdge = currentVertex.getCostTo(prevVertex);
			double totalFlowThroughEdge = extantFlowThroughEdge.orElse(0.0) + flowCache;

			currentVertex.addToAdjListOrUpdate(prevVertex, totalFlowThroughEdge);

			ret.addFirst(new Pair<>(prevVertex.getData(), currentVertex.getData()));

			currentVertex = prevVertex;
		}

		return Optional.of(new Pair<>(flowCache, ret));
	}

	/**
	 * Greedily probes as many flows as possible until blocked
	 *
	 * @return   the flow graph
	 */
	private Optional<Pair<Double, Graph<E>>> computeBlockingFlow() {
		// First flow has to be treated somewhat specially, which makes this method
		// far longer and more complicated looking than it actually is.
		// Most of this is just exactly the same as what's in the loop, with some initialization
		// code added.

		Optional<Pair<Double, List<Pair<E, E>>>> oFirstFlow
				= computeGreedyAugmentingFlow();

		if (!oFirstFlow.isPresent()) { return Optional.empty(); }

		Pair<Double, List<Pair<E, E>>> firstFlow = oFirstFlow.get();

		double extantFlow = firstFlow.getLeft();

		final double dFirstFlow = extantFlow;
		Graph<E> flowGraph = new Graph<>();
		firstFlow.getRight().forEach(
				edge -> flowGraph.addEdgeOrUpdate(edge.getLeft(), edge.getRight(), dFirstFlow));


		Optional<Pair<Double, List<Pair<E, E>>>> oNextFlow
				= computeGreedyAugmentingFlow();
		while (oNextFlow.isPresent()) {
			Pair<Double, List<Pair<E, E>>> nextFlow = oNextFlow.get();

			extantFlow += nextFlow.getLeft();

			// the following is entirely unnecessary except for visualization purposes
			nextFlow.getRight().forEach(
				edge -> {
					E source = edge.getLeft();
					E dest   = edge.getRight();

					double oldFlow = flowGraph.getEdgeCost(source, dest).orElse(0.0);
					flowGraph.addEdgeOrUpdate(source, dest, oldFlow + nextFlow.getLeft());
				});
			// end unnecessary stuff

			oNextFlow = computeGreedyAugmentingFlow();
		}
		return Optional.of(new Pair<>(extantFlow, flowGraph));
	}

	public void doIteration() {
		Optional<Pair<Double, Graph<E>>> oRes = computeBlockingFlow();

		if (!oRes.isPresent()) {
			done = true;
			return;
		}

		Pair<Double, Graph<E>> res = oRes.get();

		totalFlow += res.getLeft();

		lastFlowGraph = res.getRight();

		totalFlowGraph.addSubgraph(lastFlowGraph, Double::sum);
		canonicalizeGraph(totalFlowGraph);
	}

	/**
	 * Converts the graph such that there's only one edge
	 * between any two nodes. If there are two, the lesser
	 * one is subtracted from the opposite greater one and removed.
	 */
	private static <E> void canonicalizeGraph(Graph<E> g) {
		g.forEach(
			source -> g.getDirectSuccessors(source).get().forEach(
				dest -> g.getEdgeCost(source, dest).ifPresent(
					capacity -> g.getEdgeCost(dest, source).ifPresent(
						oppCapacity -> {
							if (capacity < oppCapacity) {
								// Have to set edges to 0 before removing them later
								// to avoid invalidating the iterator
								g.addEdgeOrUpdate(source, dest, 0.0);
								g.addEdgeOrUpdate(dest, source, oppCapacity - capacity);
							} else if (capacity == oppCapacity) {
								g.addEdgeOrUpdate(source, dest, 0.0);
								g.addEdgeOrUpdate(dest, source, 0.0);
							}
						}))));

		trimGraph(g);
	}

	/**
	 * Removes any edges with capacity 0 from the graph
	 */
	private static <E> void trimGraph(Graph<E> g) {
		// Cache all the edges so that we can remove them without
		// invalidating our iterator
		List<Pair<E, E>> edgesToRemove = g.stream().flatMap(
			source -> g.getDirectSuccessors(source).get().stream()
				.filter(dest -> g.getEdgeCost(source, dest).orElse(Double.NaN) == 0)
				.map(dest -> new Pair<>(source, dest)))
			.collect(Collectors.toList());

		edgesToRemove.forEach((e) -> g.removeEdge(e.getLeft(), e.getRight()));
	}

	/**
	 * Finds the maximum flow between two nodes in a graph using Dinic's algorithm
	 *
	 * @param   graph    the graph
	 * @param   source   the source vertex
	 * @param   sink     the sink vertex
	 * @return   the maximum flow if any
	 */
	public static <E> OptionalDouble findMaximumFlow(Graph<E> graph, E source, E sink) {
		MaxFlow<E> maxFlow = new MaxFlow<>(graph, source, sink);

		while (!maxFlow.isDone()) {
			maxFlow.computeLevels();
			maxFlow.doIteration();
		}

		return maxFlow.getTotalFlow() != 0.0 ? OptionalDouble.of(maxFlow.getTotalFlow()) : OptionalDouble.empty();
	}
}
