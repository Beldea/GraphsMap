import java.util.ArrayList;
import java.util.HashMap;

/**
 * Grafo che rappresenta una mappa su griglia rettangolare (max 100x100).
 * Ogni nodo ha id "ccrr" (colonna e riga su due cifre).
 * Gli archi sono bidirezionali (andata e ritorno) e, se non indicato, hanno peso 1.
 */
public class Map extends GraphOriented<String> {

    private final HashMap<String, NodeMap> gridNodes = new HashMap<>();

    /** Aggiunge un nodo indicando l'id "ccrr". */
    @Override
    public void addNode(String id) {
        if (!gridNodes.containsKey(id)) {
            gridNodes.put(id, new NodeMap(id)); // valida il formato
        }
        super.addNode(id);
    }

    /** Aggiunge un nodo indicando riga e colonna. */
    public void addNode(int riga, int colonna) {
        addNode(NodeMap.buildId(riga, colonna));
    }

    public NodeMap getGridNode(String id) {
        return gridNodes.get(id);
    }


    /** Arco bidirezionale con peso (usato anche da GraphOriented.addEdge). */
    @Override
    public void addEdge(String source, String dest, float weight) {
        super.addEdge(source, dest, weight);
        super.addEdge(dest, source, weight);
    }

    /** Arco bidirezionale di peso 1 tra due id. */
    public void addEdge(String source, String dest) {
        addEdge(source, dest, 1f);
    }

    /** Shortest path con Bellman-Ford: array di id da source a dest (vuoto se non raggiungibile). */
    public String[] bellmanFord(String source, String dest) {
        return buildPath(bellman_ford(source), source, dest);
    }

    /** Shortest path con Dijkstra: array di id da source a dest (vuoto se non raggiungibile). */
    public String[] dijkstra(String source, String dest) {
        return buildPath(dijkstra(source), source, dest);
    }

    private String[] buildPath(HashMap<String, Distance<String>> dist, String source, String dest) {
        if (!nodes.containsKey(dest)) {
            throw new IllegalArgumentException("Nodo destinazione inesistente: " + dest);
        }
        if (dist.get(dest).getDistance() == Float.POSITIVE_INFINITY) {
            return new String[0];
        }
        ArrayList<String> path = new ArrayList<>();
        String cur = dest;
        while (cur != null) {
            path.add(0, cur);
            if (cur.equals(source)) break;
            cur = dist.get(cur).getPrev();
        }
        return path.toArray(new String[0]);
    }
}