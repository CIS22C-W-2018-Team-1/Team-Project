package edu.deanza.cis22c.w2018.team1;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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

			Graph.Vertex vertex
					= graph.getOrCreateVertex(jsonDeserializationContext.deserialize(jVertex.get("id"), idType));

			JsonArray edges = jVertex.get("edges").getAsJsonArray();

			for (JsonElement jeEdge: edges) {
				JsonObject jEdge = jeEdge.getAsJsonObject();

				Graph.Vertex destination
						= graph.getOrCreateVertex(jsonDeserializationContext.deserialize(jEdge.get("destination"), idType));

				double weight = jEdge.get("weight").getAsDouble();

				vertex.createOrUpdateEdgeTo(destination, weight);
			}
		}

		return graph;
	}

	@Override
	public JsonElement serialize(Graph<?> graph, Type type, JsonSerializationContext jsonSerializationContext) {
		JsonObject jGraph = new JsonObject();
		JsonArray vertices = new JsonArray();

		for (Graph<?>.Vertex vertex: graph.vertices().values()) {
			vertices.add(serializeVertex(vertex, jsonSerializationContext));
		}

		jGraph.add("vertices", vertices);

		return jGraph;
	}

	private JsonObject serializeVertex(Graph<?>.Vertex vertex, JsonSerializationContext jsonSerializationContext) {
		JsonObject jVertex = new JsonObject();

		jVertex.add("id", jsonSerializationContext.serialize(vertex.getId()));

		JsonArray edges = new JsonArray();

		for (Graph<?>.Edge edge: vertex.outgoingEdges()) {
			JsonObject jEdge = new JsonObject();
			jEdge.addProperty("weight", edge.getWeight());
			jEdge.add("destination", jsonSerializationContext.serialize(edge.getDestination().getId()));

			edges.add(jEdge);
		}

		jVertex.add("edges", edges);

		return jVertex;
	}
}
