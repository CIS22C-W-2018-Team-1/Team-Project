package edu.deanza.cis22c.w2018.team1;

import com.google.gson.Gson;

public class CreateGraph {

	public static void main(String args[]) {
		serializeGraph();
		deserializeGraph();
	}

	private static void serializeGraph() {
		Graph<String> graph = new Graph<String>();

		Graph<String>.Vertex a = graph.getOrCreateVertex("a");
		Graph<String>.Vertex b = graph.getOrCreateVertex("b");

		a.createOrUpdateEdgeTo(b, 3);
		b.createOrUpdateEdgeTo(a, 8);

		Gson g = new Gson();
		String json = g.toJson(graph);
	}

	private static void deserializeGraph() {
		String userJson = "";

		Gson g = new Gson();
		Graph graph = g.fromJson(userJson, Graph.class);
	}
}
