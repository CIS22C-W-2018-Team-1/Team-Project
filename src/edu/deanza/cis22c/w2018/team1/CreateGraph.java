import com.google.gson.Gson;

public class CreateGraph {

    public static void main(String args[]) {
        serializeGraph();
        deserializeGraph();
    }

    private static void serializeGraph() {
        Graph<Vertex<String>> graph = new Graph<Vertex<String>>();
        Vertex<String> a = new Vertex<String>("a");
        Vertex<String> b = new Vertex<String>("b");

        graph.addEdge(a, b, 3);
        graph.addEdge(b, a, 8);

        Gson g = new Gson();
        String json = g.toJson(graph);
    }

    private static void deserializeGraph() {
        String userJson = "";

        Gson g = new Gson();
        Graph graph = g.fromJson(userJson, Graph.class);
    }
}
