package edu.deanza.cis22c.w2018.team1;

import edu.deanza.cis22c.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

public class MaxFlow {
	private static <E> Map<E, Integer> computeLevels(Graph<E>.Vertex source) {
		Map<E, Integer> levels = new HashMap<>();
		levels.put(source.getData(), 0);

		int level = 1;
		List<Graph<E>.Vertex> currentLevel = Collections.singletonList(source);
		List<Graph<E>.Vertex> nextLevel = new ArrayList<>();
		while (!currentLevel.isEmpty()) {
			final int lvl = level;
			final List<Graph<E>.Vertex> next = nextLevel;
			for (Graph<E>.Vertex vertex: currentLevel) {
				vertex.edges().forEachRemaining((e) -> {
					if (!levels.containsKey(e.getLeft().getData())) {
						next.add(e.getLeft());
						levels.put(e.getLeft().getData(), lvl);
					}
				});
			}
			++level;
			currentLevel = nextLevel;
			nextLevel = new ArrayList<>();
		}
		return levels;
	}

	private static <E> OptionalDouble computeGreedyAugmentingFlow(Map<E, Integer> levels, Graph<E>.Vertex source,
	                                                                                      Graph<E>.Vertex dest) {
		Set<Graph<E>.Vertex> visited = new HashSet<>();

		visited.add(source);

		Map<Graph<E>.Vertex, Graph<E>.Vertex> backtrackMap = new HashMap<>();
		Map<Graph<E>.Vertex, Double> flowAtVertex = new HashMap<>();
		flowAtVertex.put(source, Double.POSITIVE_INFINITY);

		Graph<E>.Vertex currentVertex = source;
		double flowCache = Double.POSITIVE_INFINITY;
		while (currentVertex != null && currentVertex != dest) {
			Iterator<Pair<Graph<E>.Vertex, Double>> edges = currentVertex.edges();

			int currentLevel = levels.get(currentVertex.getData());

			Optional<Pair<Graph<E>.Vertex, Double>> nextEdge = StreamUtil.fromIterator(edges)
					.filter((p) -> !visited.contains(p.getLeft()))
					.filter((p) -> levels.get(p.getLeft().getData()) > currentLevel)
					.reduce((l, r) -> l.getRight() > r.getRight() ? l : r);

			if (!nextEdge.isPresent()) {
				currentVertex = backtrackMap.get(currentVertex);
				if (currentVertex != null) {
					flowCache = flowAtVertex.get(currentVertex);
				}
			} else {
				Graph<E>.Vertex nextVertex = nextEdge.get().getLeft();
				double edgeFlow = nextEdge.get().getRight();

				visited.add(nextVertex);
				backtrackMap.put(nextVertex, currentVertex);
				flowCache = Math.min(flowCache, edgeFlow);
				flowAtVertex.put(nextVertex, flowCache);
				currentVertex = nextVertex;
			}
		}

		if (currentVertex == null) { return OptionalDouble.empty(); }

		while (currentVertex != source) {
			Graph<E>.Vertex prevVertex = backtrackMap.get(currentVertex);

			double newCapacity = prevVertex.getEdgeWeightWith(currentVertex.getData()).getAsDouble() - flowCache;
			if (newCapacity > 0.0) {
				prevVertex.setEdgeWeightWith(currentVertex.getData(), newCapacity);
			} else {
				prevVertex.removeEdgeWith(currentVertex);
			}

			OptionalDouble extantFlowThroughEdge = currentVertex.getEdgeWeightWith(prevVertex.getData());
			double totalFlowThroughEdge = extantFlowThroughEdge.orElse(0.0) + flowCache;

			currentVertex.setOrCreateEdgeWith(prevVertex, totalFlowThroughEdge);

			currentVertex = prevVertex;
		}

		return OptionalDouble.of(flowCache);
	}

	private static <E> OptionalDouble computeBlockingFlow(Graph<E>.Vertex source, Graph<E>.Vertex dest) {
		Map<E, Integer> levels = computeLevels(source);

		OptionalDouble firstFlow = computeGreedyAugmentingFlow(levels, source, dest);

		if (!firstFlow.isPresent()) { return OptionalDouble.empty(); }

		double extantFlow = firstFlow.getAsDouble();

		while (true) {
			OptionalDouble nextFlow = computeGreedyAugmentingFlow(levels, source, dest);

			if (!nextFlow.isPresent()) { return OptionalDouble.of(extantFlow); }

			extantFlow += nextFlow.getAsDouble();
		}
	}

	public static <E> OptionalDouble findMaximumFlow(Graph<E> graph, E source, E dest) {
		Graph<E> residualGraph = new Graph<>(graph);

		Optional<Graph<E>.Vertex> oSource, oDest;
		oSource = residualGraph.getVertexIfPresent(source);
		oDest   = residualGraph.getVertexIfPresent(dest);

		Graph<E>.Vertex vSource, vDest;
		vSource = oSource.orElseThrow(()-> new IllegalArgumentException("Node with value " + source + " does not exist"));
		vDest   =   oDest.orElseThrow(()-> new IllegalArgumentException("Node with value " + dest   + " does not exist"));

		OptionalDouble firstIteration = computeBlockingFlow(vSource, vDest);

		if (!firstIteration.isPresent()) { return OptionalDouble.empty(); }

		double totalFlow = firstIteration.getAsDouble();

		while (true) {
			OptionalDouble nextIteration = computeBlockingFlow(vSource, vDest);

			if (!nextIteration.isPresent()) { return OptionalDouble.of(totalFlow); }

			totalFlow += nextIteration.getAsDouble();
		}
	}
}
