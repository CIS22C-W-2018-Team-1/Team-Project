package edu.deanza.cis22c.w2018.team1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

public class MaxFlow {
	private static <E> Map<Graph<E>.Vertex, Integer> computeLevels(Graph<E>.Vertex source) {
		Map<Graph<E>.Vertex, Integer> levels = new HashMap<>();
		levels.put(source, 0);

		int level = 1;
		List<Graph<E>.Vertex> currentLevel = Collections.singletonList(source);
		List<Graph<E>.Vertex> nextLevel = new ArrayList<>();
		while (!currentLevel.isEmpty()) {
			final int lvl = level;
			final List<Graph<E>.Vertex> next = nextLevel;
			for (Graph<E>.Vertex vertex: currentLevel) {
				for (Graph<E>.Edge edge: vertex.outgoingEdges()) {
					if (!levels.containsKey(edge.getDestination())) {
						next.add(edge.getDestination());
						levels.put(edge.getDestination(), lvl);
					}
				}
			}
			++level;
			currentLevel = nextLevel;
			nextLevel = new ArrayList<>();
		}
		return levels;
	}

	private static <E> OptionalDouble computeGreedyAugmentingFlow(Map<Graph<E>.Vertex, Integer> levels,
	                                                              Graph<E>.Vertex source, Graph<E>.Vertex dest) {
		Set<Graph<E>.Vertex> visited = new HashSet<>();

		visited.add(source);

		Map<Graph<E>.Vertex, Graph<E>.Vertex> backtrackMap = new HashMap<>();
		Map<Graph<E>.Vertex, Double> flowAtVertex = new HashMap<>();
		flowAtVertex.put(source, Double.POSITIVE_INFINITY);

		Graph<E>.Vertex currentVertex = source;
		double flowCache = Double.POSITIVE_INFINITY;
		while (currentVertex != null && currentVertex != dest) {
			int currentLevel = levels.get(currentVertex);

			Optional<Graph<E>.Edge> nextEdge = currentVertex.outgoingEdges().stream()
					.filter((p) -> !visited.contains(p.getDestination()))
					.filter((p) -> levels.get(p.getDestination()) > currentLevel)
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

		if (currentVertex == null) { return OptionalDouble.empty(); }

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

			currentVertex = prevVertex;
		}

		return OptionalDouble.of(flowCache);
	}

	private static <E> OptionalDouble computeBlockingFlow(Graph<E>.Vertex source, Graph<E>.Vertex dest) {
		Map<Graph<E>.Vertex, Integer> levels = computeLevels(source);

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
		oSource = residualGraph.getVertex(source);
		oDest   = residualGraph.getVertex(dest);

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
