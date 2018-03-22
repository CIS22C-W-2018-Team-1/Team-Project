package edu.deanza.cis22c.w2018.team1.flowimpl;

import edu.deanza.cis22c.Pair;
import edu.deanza.cis22c.w2018.team1.Graph;

import java.util.ArrayList;
import java.util.Collection;
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

	private Graph<E>.Vertex rSource, rDest;

	private double totalFlow = 0.0;

	private Map<E, Integer> lastLevels;
	private Graph<E> totalFlowGraph = new Graph<>();
	private Graph<E> lastFlowGraph;

	private boolean done = false;

	private MaxFlow(Graph<E> graph, Graph<E>.Vertex source, Graph<E>.Vertex dest) {
		this.graph = graph;

		residualGraph = new Graph<>(graph);
		rSource = residualGraph.getVertex(source.getId()).get();
		rDest   = residualGraph.getVertex(dest.getId()).get();
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
		addend.vertices().values().stream()
				.map(Graph.Vertex::outgoingEdges)
				.flatMap(Collection::stream)
				.forEach((edge) -> {
					Graph<E>.Vertex source = augend.getOrCreateVertex(edge.getSource().getId());
					Graph<E>.Vertex dest   = augend.getOrCreateVertex(edge.getDestination().getId());

					if (source.hasEdgeTo(dest)) {
						Graph<E>.Edge edgeToUpdate = source.getEdgeTo(dest).get();

						edgeToUpdate.setWeight(edgeToUpdate.getWeight() + edge.getWeight());
					} else {
						source.createOrUpdateEdgeTo(dest, edge.getWeight());
					}
				});
	}

	private static <E> void canonicalizeGraph(Graph<E> g) {
		g.vertices().values().stream()
				.map(Graph.Vertex::outgoingEdges)
				.flatMap(Collection::stream)
				.forEach((edge) -> edge.getDestination().getEdgeTo(edge.getSource()).ifPresent(
						(oppEdge) -> {
							double edgeWeight = edge.getWeight();
							double oppEdgeWeight = oppEdge.getWeight();
							if (edgeWeight < oppEdgeWeight) {
								oppEdge.setWeight(oppEdgeWeight - edgeWeight);
								edge.setWeight(0);
							} else if (edgeWeight == oppEdgeWeight) {
								edge.setWeight(0);
								oppEdge.setWeight(0);
							}
						}));

		trimGraph(g);
	}

	private static <E> void trimGraph(Graph<E> g) {
		List<Graph<E>.Edge> edgesToRemove = g.vertices().values().stream()
				.map(Graph.Vertex::outgoingEdges).flatMap(Collection::stream).collect(Collectors.toList());

		edgesToRemove.forEach(Graph.Edge::remove);
	}

	private static <E> Map<E, Integer> computeLevels(Graph<E>.Vertex source) {
		Map<E, Integer> levels = new HashMap<>();
		levels.put(source.getId(), 0);

		int level = 1;
		List<Graph<E>.Vertex> currentLevel = Collections.singletonList(source);
		List<Graph<E>.Vertex> nextLevel = new ArrayList<>();
		while (!currentLevel.isEmpty()) {
			final int lvl = level;
			final List<Graph<E>.Vertex> next = nextLevel;
			currentLevel.forEach((vertex) ->
					vertex.outgoingEdges().stream().map(Graph.Edge::getDestination).forEachOrdered((dest) -> {
						if (!levels.containsKey(dest.getId())) {
							next.add(dest);
							levels.put(dest.getId(), lvl);
						}
					}));

			++level;
			currentLevel = nextLevel;
			nextLevel = new ArrayList<>();
		}
		return levels;
	}

	private static <E> Optional<Pair<Double, List<Graph<E>.Edge>>>
			computeGreedyAugmentingFlow(Map<E, Integer> levels,
			                            Graph<E>.Vertex source, Graph<E>.Vertex dest) {
		Set<Graph<E>.Vertex> visited = new HashSet<>();

		visited.add(source);

		Map<Graph<E>.Vertex, Graph<E>.Vertex> backtrackMap = new HashMap<>();
		Map<Graph<E>.Vertex, Double> flowAtVertex = new HashMap<>();
		flowAtVertex.put(source, Double.POSITIVE_INFINITY);

		Graph<E>.Vertex currentVertex = source;
		double flowCache = Double.POSITIVE_INFINITY;
		while (currentVertex != null && currentVertex != dest) {
			int currentLevel = levels.get(currentVertex.getId());

			Optional<Graph<E>.Edge> nextEdge = currentVertex.outgoingEdges().stream()
					.filter((p) -> !visited.contains(p.getDestination()))
					.filter((p) -> levels.get(p.getDestination().getId()) > currentLevel)
					.reduce((l, r) -> l.getWeight() > r.getWeight() ? l : r);

			if (!nextEdge.isPresent()) {
				currentVertex = backtrackMap.get(currentVertex);
				if (currentVertex != null) {
					flowCache = flowAtVertex.get(currentVertex);
				}
			} else {
				Graph<E>.Vertex nextVertex = nextEdge.get().getDestination();
				double edgeFlow = nextEdge.get().getWeight();

				visited.add(nextVertex);
				backtrackMap.put(nextVertex, currentVertex);
				flowCache = Math.min(flowCache, edgeFlow);
				flowAtVertex.put(nextVertex, flowCache);
				currentVertex = nextVertex;
			}
		}

		if (currentVertex == null) { return Optional.empty(); }

		LinkedList<Graph<E>.Edge> ret = new LinkedList<>();

		while (currentVertex != source) {
			Graph<E>.Vertex prevVertex = backtrackMap.get(currentVertex);

			Graph<E>.Edge edge = prevVertex.getEdgeTo(currentVertex).get();
			double newCapacity = edge.getWeight() - flowCache;
			if (newCapacity > 0.0) {
				edge.setWeight(newCapacity);
			} else {
				edge.remove();
			}

			Optional<Double> extantFlowThroughEdge = currentVertex.getEdgeTo(prevVertex).map(Graph.Edge::getWeight);
			double totalFlowThroughEdge = extantFlowThroughEdge.orElse(0.0) + flowCache;

			currentVertex.createOrUpdateEdgeTo(prevVertex, totalFlowThroughEdge);

			ret.addFirst(edge);

			currentVertex = prevVertex;
		}

		return Optional.of(new Pair<>(flowCache, ret));
	}

	private static <E> Optional<Pair<Double, Graph<E>>>
			computeBlockingFlow(Graph<E>.Vertex source, Graph<E>.Vertex dest, Map<E, Integer> levels) {
		Optional<Pair<Double, List<Graph<E>.Edge>>> oFirstFlow = computeGreedyAugmentingFlow(levels, source, dest);

		if (!oFirstFlow.isPresent()) { return Optional.empty(); }

		Pair<Double, List<Graph<E>.Edge>> firstFlow = oFirstFlow.get();

		double extantFlow = firstFlow.getLeft();

		final double dFirstFlow = extantFlow;
		Graph<E> flowGraph = new Graph<>();
		firstFlow.getRight().forEach((edge) ->
			flowGraph.getOrCreateVertex(edge.getSource().getId())
					.createOrUpdateEdgeTo(flowGraph.getOrCreateVertex(edge.getDestination().getId()), dFirstFlow));

		while (true) {
			Optional<Pair<Double, List<Graph<E>.Edge>>> oNextFlow = computeGreedyAugmentingFlow(levels, source, dest);

			if (!oNextFlow.isPresent()) { return Optional.of(new Pair<>(extantFlow, flowGraph)); }

			Pair<Double, List<Graph<E>.Edge>> nextFlow = oNextFlow.get();

			extantFlow += nextFlow.getLeft();
			nextFlow.getRight().forEach((edge) -> {
				Graph<E>.Vertex flowSource, flowDestination;
				flowSource      = flowGraph.getOrCreateVertex(edge.getSource().getId());
				flowDestination = flowGraph.getOrCreateVertex(edge.getDestination().getId());

				if (flowSource.hasEdgeTo(flowDestination)) {
					Graph<E>.Edge flowEdge = flowSource.getEdgeTo(flowDestination).get();

					flowEdge.setWeight(flowEdge.getWeight() + nextFlow.getLeft());
				} else {
					flowSource.createOrUpdateEdgeTo(flowDestination, nextFlow.getLeft());
				}
			});
		}
	}

	public static <E> OptionalDouble findMaximumFlow(Graph<E> graph, Graph<E>.Vertex source, Graph<E>.Vertex dest) {
		Graph<E> residualGraph = new Graph<>(graph);

		MaxFlow<E> maxFlow = new MaxFlow<>(graph, source, dest);

		maxFlow.execute();

		return maxFlow.totalFlow != 0.0 ? OptionalDouble.of(maxFlow.totalFlow) : OptionalDouble.empty();
	}
}
