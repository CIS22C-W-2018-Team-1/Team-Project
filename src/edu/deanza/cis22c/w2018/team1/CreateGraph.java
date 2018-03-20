package edu.deanza.cis22c.w2018.team1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class CreateGraph {

	public static void main(String args[]) {
		Graph<String> graph = new Graph<String>();

		Graph<String>.Vertex a = graph.getOrCreateVertex("a");
		Graph<String>.Vertex b = graph.getOrCreateVertex("b");
		Graph<String>.Vertex c = graph.getOrCreateVertex("c");
		Graph<String>.Vertex d = graph.getOrCreateVertex("d");

		a.createOrUpdateEdgeTo(b, 5);
		a.createOrUpdateEdgeTo(c, 15);
		b.createOrUpdateEdgeTo(d, 10);
		c.createOrUpdateEdgeTo(d, 10);

		graph.showAdjTable();

		System.out.println(MaxFlow.findMaximumFlow(graph, "a", "d"));

		c.createOrUpdateEdgeTo(b, 5);

		graph.showAdjTable();

		System.out.println(MaxFlow.findMaximumFlow(graph, "a", "d"));

		String json = serializeGraph(graph);

		System.out.println(json);

		Graph<String> deserializedGraph = deserializeGraph(json);

		System.out.println("Deserialized:");

		deserializedGraph.showAdjTable();

		System.out.println(MaxFlow.findMaximumFlow(graph, "a", "d"));
	}

	private static Gson gson;

	static {
		GsonBuilder gBuilder = new GsonBuilder();
		gBuilder.registerTypeAdapter(Graph.class, new GraphSerializer());
		gBuilder.setPrettyPrinting();
		gson = gBuilder.create();
	}

	private static <E> String serializeGraph(Graph<E> graph) {
		return gson.toJson(graph);
	}

	private static Graph<String> deserializeGraph(String json) {
		Type graphType = new TypeToken<Graph<String>>() {}.getType();
		return gson.fromJson(json, graphType);
	}
}
