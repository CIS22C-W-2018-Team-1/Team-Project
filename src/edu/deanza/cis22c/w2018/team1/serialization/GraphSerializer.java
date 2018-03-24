package edu.deanza.cis22c.w2018.team1.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import edu.deanza.cis22c.w2018.team1.structure.Pair;
import edu.deanza.cis22c.w2018.team1.structure.graph.Graph;
import edu.deanza.cis22c.w2018.team1.structure.graph.Vertex;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GraphSerializer implements JsonSerializer<Graph<?>>, JsonDeserializer<Graph<?>> {
	@Override
	@SuppressWarnings("unchecked")
	public Graph<?> deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext)
			throws JsonParseException {
		Graph<?> graph = new Graph<>();
		JsonArray vertices = json.getAsJsonObject().get("vertices").getAsJsonArray();

		Type[] typeParameters = ((ParameterizedType)type).getActualTypeArguments();
		Type idType = typeParameters[0];

		for (JsonElement jeVertex: vertices) {
			JsonObject jVertex = jeVertex.getAsJsonObject();

			Vertex vertex
					= graph.addToVertexSet(jsonDeserializationContext.deserialize(jVertex.get("id"), idType));

			JsonArray edges = jVertex.get("edges").getAsJsonArray();

			for (JsonElement jeEdge: edges) {
				JsonObject jEdge = jeEdge.getAsJsonObject();

				Vertex destination
						= graph.addToVertexSet(jsonDeserializationContext.deserialize(jEdge.get("destination"), idType));

				double weight = jEdge.get("weight").getAsDouble();

				vertex.addToAdjListOrUpdate(destination, weight);
			}
		}

		return graph;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JsonElement serialize(Graph<?> graph, Type type, JsonSerializationContext jsonSerializationContext) {
		JsonObject jGraph = new JsonObject();
		JsonArray vertices = new JsonArray();

		graph.getVertexSet().values().stream()
				.map(v -> serializeVertex(v, jsonSerializationContext))
				.forEachOrdered(vertices::add);

		jGraph.add("vertices", vertices);

		return jGraph;
	}

	private <T> JsonObject serializeVertex(Vertex<T> vertex, JsonSerializationContext jsonSerializationContext) {
		JsonObject jVertex = new JsonObject();

		jVertex.add("id", jsonSerializationContext.serialize(vertex.getData()));

		JsonArray edges = new JsonArray();

		vertex.getAdjList().values().stream()
				.map(e -> serializeEdge(e, jsonSerializationContext))
				.forEachOrdered(edges::add);

		jVertex.add("edges", edges);

		return jVertex;
	}

	private <T> JsonObject serializeEdge(Pair<Vertex<T>, Double> edge,
	                                     JsonSerializationContext jsonSerializationContext) {
		JsonObject jEdge = new JsonObject();
		jEdge.addProperty("weight", edge.getRight());
		jEdge.add("destination", jsonSerializationContext.serialize(edge.getLeft().getData()));

		return jEdge;
	}
}
