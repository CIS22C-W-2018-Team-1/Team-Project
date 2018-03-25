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
		Graph<Object> graph = new Graph<>();
		JsonArray vertices = json.getAsJsonObject().get("vertices").getAsJsonArray();

		Type[] typeParameters = ((ParameterizedType)type).getActualTypeArguments();
		Type idType = typeParameters[0];

		for (JsonElement jeVertex: vertices) {
			JsonObject jVertex = jeVertex.getAsJsonObject();

			Object vertex = jsonDeserializationContext.deserialize(jVertex.get("id"), idType);

			graph.addVertex(vertex);

			JsonArray edges = jVertex.get("edges").getAsJsonArray();

			for (JsonElement jeEdge: edges) {
				JsonObject jEdge = jeEdge.getAsJsonObject();
				Object destination = jsonDeserializationContext.deserialize(jEdge.get("destination"), idType);

				double weight = jEdge.get("weight").getAsDouble();

				graph.addEdgeOrUpdate(vertex, destination, weight);
			}
		}

		return graph;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JsonElement serialize(Graph<?> graph, Type idType, JsonSerializationContext jsonSerializationContext) {
		JsonObject jGraph = new JsonObject();
		JsonArray vertices = new JsonArray();

		graph.stream()
				// While we're casting it to a Graph<Object>, the code will still
				// work fine because of the type tag
				.map(v -> this.serializeVertex((Graph<Object>) graph, v, idType, jsonSerializationContext))
				.forEachOrdered(vertices::add);

		jGraph.add("vertices", vertices);

		return jGraph;
	}

	private <T> JsonObject serializeVertex(Graph<T> graph, T vertex, Type idType,
	                                       JsonSerializationContext jsonSerializationContext) {
		JsonObject jVertex = new JsonObject();

		jVertex.add("id", jsonSerializationContext.serialize(vertex, idType));

		JsonArray edges = new JsonArray();

		graph.getDirectSuccessors(vertex).get().stream()
				.map(dest -> serializeEdge(dest, graph.getEdgeCost(vertex, dest).getAsDouble(),
				                           idType, jsonSerializationContext))
				.forEachOrdered(edges::add);

		jVertex.add("edges", edges);

		return jVertex;
	}

	private <T> JsonObject serializeEdge(T dest, double weight, Type idType,
	                                     JsonSerializationContext jsonSerializationContext) {
		JsonObject jEdge = new JsonObject();
		jEdge.addProperty("weight", weight);
		jEdge.add("destination", jsonSerializationContext.serialize(dest));

		return jEdge;
	}
}
