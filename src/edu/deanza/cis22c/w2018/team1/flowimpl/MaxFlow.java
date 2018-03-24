package edu.deanza.cis22c.w2018.team1.flowimpl;

import edu.deanza.cis22c.Pair;
import edu.deanza.cis22c.w2018.team1.Graph;
import edu.deanza.cis22c.w2018.team1.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

public class MaxFlow<E> {
	private Graph<E> graph;
	private Graph<E> residualGraph;

	private Vertex<E> rSource, rDest;

	private double totalFlow = 0.0;

	private Map<E, Integer> lastLevels;
	private Graph<E> totalFlowGraph = new Graph<>();
	private Graph<E> lastFlowGraph;

	private boolean done = false;

	private MaxFlow(Graph<E> graph, Vertex<E> source, Vertex<E> dest) {
		this.graph = graph;

		residualGraph = new Graph<>();
		graph.getVertexSet().values().forEach(
				v -> v.getAdjList().values().forEach(
						p -> residualGraph.addEdgeOrUpdate(v.getData(), p.getLeft().getData(), p.getRight())));

		rSource = residualGraph.getVertex(source.getData());
		rDest   = residualGraph.getVertex(dest.getData());
	}

	private void updateLevels() {
		lastLevels = computeLevels(rSource);
	}

	private void doIteration() {
		updateLevels();

		Optional<Pair<Double, Graph<E>>> oRes = computeBlockingFlow(rSource, rDest, lastLevels);

		if (!oRes.isPresent()) {
			done = true;
			return;
		}

		Pair<Double, Graph<E>> res = oRes.get();

		totalFlow += res.getLeft();

		lastFlowGraph = res.getRight();

		addGraphTo(lastFlowGraph, totalFlowGraph);
		canonicalizeGraph(totalFlowGraph);
	}

	private void execute() {
		while (!done) {
			doIteration();
		}
	}

	private static <E> void addGraphTo(Graph<E> addend, Graph<E> augend) {
		addend.getVertexSet().values().forEach(
				vertex -> vertex.getAdjList().values().forEach(
						edge -> {
							Vertex<E> source = augend.addToVertexSet(vertex.getData());
							Vertex<E> dest   = augend.addToVertexSet(edge.getLeft().getData());

							OptionalDouble oldCost = source.getCostTo(dest);
							if (oldCost.isPresent()) {
								source.addToAdjListOrUpdate(dest, oldCost.getAsDouble() + edge.getRight());
							} else {
								source.addToAdjListOrUpdate(dest, edge.getRight());
							}
						}));
	}

	private static <E> void canonicalizeGraph(Graph<E> g) {
		g.getVertexSet().values().forEach(
				source -> source.getAdjList().values().forEach(
				edge -> edge.getLeft().getCostTo(source).ifPresent(
						(oppEdgeWeight) -> {
							double edgeWeight = edge.getRight();
							if (edgeWeight < oppEdgeWeight) {
								edge.getLeft().addToAdjListOrUpdate(source, oppEdgeWeight - edgeWeight);
							} else if (edgeWeight == oppEdgeWeight) {
								edge.setRight(0.0);
								edge.getLeft().addToAdjListOrUpdate(source, oppEdgeWeight - edgeWeight);
							}
						})));

		trimGraph(g);
	}

	private static <E> void trimGraph(Graph<E> g) {
		List<Pair<E, E>> edgesToRemove = g.getVertexSet().values().stream()
				.flatMap(source -> source.getAdjList().values().stream()
						.filter(e -> e.getRight() == 0)
						.map(p -> new Pair<>(source.getData(), p.getLeft().getData())))
				.collect(Collectors.toList());

		edgesToRemove.forEach((e) -> g.remove(e.getLeft(), e.getRight()));
	}

	private static <E> Map<E, Integer> computeLevels(Vertex<E> source) {
		Map<E, Integer> levels = new HashMap<>();
		levels.put(source.getData(), 0);

		int level = 1;
		List<Vertex<E>> currentLevel = Collections.singletonList(source);
		List<Vertex<E>> nextLevel = new ArrayList<>();
		while (!currentLevel.isEmpty()) {
			final int lvl = level;
			final List<Vertex<E>> next = nextLevel;
			currentLevel.forEach((vertex) ->
					vertex.getAdjList().values().stream().map(Pair::getLeft).forEachOrdered((dest) -> {
						if (!levels.containsKey(dest.getData())) {
							next.add(dest);
							levels.put(dest.getData(), lvl);
						}
					}));

			++level;
			currentLevel = nextLevel;
			nextLevel = new ArrayList<>();
		}
		return levels;
	}

	private static <E> Optional<Pair<Double, List<Pair<Vertex<E>, Vertex<E>>>>>
			computeGreedyAugmentingFlow(Map<E, Integer> levels,
			                            Vertex<E> source, Vertex<E> dest) {
		Set<Vertex<E>> visited = new HashSet<>();

		visited.add(source);

		Map<Vertex<E>, Vertex<E>> backtrackMap = new HashMap<>();
		Map<Vertex<E>, Double> flowAtVertex = new HashMap<>();
		flowAtVertex.put(source, Double.POSITIVE_INFINITY);

		Vertex<E> currentVertex = source;
		double flowCache = Double.POSITIVE_INFINITY;
		while (currentVertex != null && currentVertex != dest) {
			int currentLevel = levels.get(currentVertex.getData());

			Optional<Pair<Vertex<E>, Double>> nextEdge = currentVertex.getAdjList().values().stream()
					.filter((p) -> !visited.contains(p.getLeft()))
					.filter((p) -> levels.get(p.getLeft().getData()) > currentLevel)
					.reduce((l, r) -> l.getRight() > r.getRight() ? l : r);

			if (!nextEdge.isPresent()) {
				currentVertex = backtrackMap.get(currentVertex);
				if (currentVertex != null) {
					flowCache = flowAtVertex.get(currentVertex);
				}
			} else {
				Vertex<E> nextVertex = nextEdge.get().getLeft();
				double edgeFlow = nextEdge.get().getRight();

				visited.add(nextVertex);
				backtrackMap.put(nextVertex, currentVertex);
				flowCache = Math.min(flowCache, edgeFlow);
				flowAtVertex.put(nextVertex, flowCache);
				currentVertex = nextVertex;
			}
		}

		if (currentVertex == null) { return Optional.empty(); }

		LinkedList<Pair<Vertex<E>, Vertex<E>>> ret = new LinkedList<>();

		while (currentVertex != source) {
			Vertex<E> prevVertex = backtrackMap.get(currentVertex);

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

			ret.addFirst(new Pair<>(prevVertex, currentVertex));

			currentVertex = prevVertex;
		}

		return Optional.of(new Pair<>(flowCache, ret));
	}

	private static <E> Optional<Pair<Double, Graph<E>>>
			computeBlockingFlow(Vertex<E> source, Vertex<E> dest, Map<E, Integer> levels) {
		Optional<Pair<Double, List<Pair<Vertex<E>, Vertex<E>>>>> oFirstFlow
				= computeGreedyAugmentingFlow(levels, source, dest);

		if (!oFirstFlow.isPresent()) { return Optional.empty(); }

		Pair<Double, List<Pair<Vertex<E>, Vertex<E>>>> firstFlow = oFirstFlow.get();

		double extantFlow = firstFlow.getLeft();

		final double dFirstFlow = extantFlow;
		Graph<E> flowGraph = new Graph<>();
		firstFlow.getRight().forEach(
				edge -> flowGraph.addEdgeOrUpdate(edge.getLeft().getData(), edge.getRight().getData(), dFirstFlow));

		while (true) {
			Optional<Pair<Double, List<Pair<Vertex<E>, Vertex<E>>>>> oNextFlow
					= computeGreedyAugmentingFlow(levels, source, dest);

			if (!oNextFlow.isPresent()) { return Optional.of(new Pair<>(extantFlow, flowGraph)); }

			Pair<Double, List<Pair<Vertex<E>, Vertex<E>>>> nextFlow = oNextFlow.get();

			extantFlow += nextFlow.getLeft();
			nextFlow.getRight().forEach((edge) -> {
				Vertex<E> flowSource, flowDestination;
				flowSource      = flowGraph.addToVertexSet(edge.getLeft().getData());
				flowDestination = flowGraph.addToVertexSet(edge.getRight().getData());

				OptionalDouble oldFlow = flowSource.getCostTo(flowDestination);
				if (oldFlow.isPresent()) {
					flowSource.addToAdjListOrUpdate(flowDestination, oldFlow.getAsDouble() + nextFlow.getLeft());
				} else {
					flowSource.addToAdjListOrUpdate(flowDestination, nextFlow.getLeft());
				}
			});
		}
	}

	public static <E> OptionalDouble findMaximumFlow(Graph<E> graph, Vertex<E> source, Vertex<E> dest) {
		MaxFlow<E> maxFlow = new MaxFlow<>(graph, source, dest);

		maxFlow.execute();

		return maxFlow.totalFlow != 0.0 ? OptionalDouble.of(maxFlow.totalFlow) : OptionalDouble.empty();
	}
}
