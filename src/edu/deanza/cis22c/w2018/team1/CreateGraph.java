package edu.deanza.cis22c.w2018.team1;

public class CreateGraph {
	public staic void main(String[] args) {
	}
	
	private static void serializeGraph() {
		Graph graph = new Graph();
		
		Gson gson = new Gson();
		String json = gson.toJson(graph);
	}
	
	private static void deserializeGraph() {
		
	}
}
