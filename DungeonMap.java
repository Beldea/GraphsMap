import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class DungeonMap {
    private final GraphOriented<String> graph = new GraphOriented<>();

    private DungeonMap() {
    }

    /**
     * Formato:
     * 0000[0001:1;0400:4]0400[0402:2;0600:2]
     */
    public static DungeonMap parse(String testo) {
        if (testo == null || testo.trim().isEmpty()) {
            throw new IllegalArgumentException("La mappa è vuota");
        }

        DungeonMap mappa = new DungeonMap();
        String[] nodi = testo.trim().split("\\]");

        for (String nodoTesto : nodi) {
            nodoTesto = nodoTesto.trim();

            if (nodoTesto.isEmpty()) {
                continue;
            }

            int parentesi = nodoTesto.indexOf('[');
            if (parentesi < 0) {
                throw new IllegalArgumentException(
                        "Nodo senza lista di adiacenze: " + nodoTesto);
            }

            String sorgente = nodoTesto.substring(0, parentesi).trim();
            String adiacenze = nodoTesto.substring(parentesi + 1).trim();

            validaId(sorgente);
            mappa.graph.addNode(sorgente);

            if (adiacenze.isEmpty()) {
                continue;
            }

            for (String adiacenza : adiacenze.split(";")) {
                String[] dati = adiacenza.trim().split(":");

                if (dati.length != 2) {
                    throw new IllegalArgumentException(
                            "Adiacenza non valida '" + adiacenza
                                    + "' nel nodo " + sorgente);
                }

                String destinazione = dati[0].trim();
                validaId(destinazione);

                float distanza;
                try {
                    distanza = Float.parseFloat(dati[1].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Distanza non valida nell'adiacenza '" + adiacenza + "'", e);
                }

                if (!Float.isFinite(distanza) || distanza < 0) {
                    throw new IllegalArgumentException(
                            "La distanza deve essere finita e non negativa: " + distanza);
                }

                mappa.graph.addEdge(sorgente, destinazione, distanza);
            }
        }

        return mappa;
    }

    private static void validaId(String id) {
        if (!id.matches("\\d{4}")) {
            throw new IllegalArgumentException(
                    "Identificativo nodo non valido (attese 4 cifre): " + id);
        }
    }

    public boolean containsNode(String id) {
        HashMap<String, Distance<String>> distanze = graph.dijkstra(id);
        return distanze.containsKey(id);
    }

    public List<String> shortestPath(String partenza, String destinazione) {
        if (!containsNode(partenza)) {
            throw new IllegalArgumentException(
                    "Nodo sorgente inesistente: " + partenza);
        }
        if (!containsNode(destinazione)) {
            throw new IllegalArgumentException(
                    "Nodo destinazione inesistente: " + destinazione);
        }

        HashMap<String, Distance<String>> distanze = graph.dijkstra(partenza);
        Distance<String> arrivo = distanze.get(destinazione);

        if (arrivo == null
                || arrivo.getDistance() == Float.POSITIVE_INFINITY) {
            return new ArrayList<>();
        }

        ArrayList<String> percorso = new ArrayList<>();
        String corrente = destinazione;

        while (corrente != null) {
            percorso.add(0, corrente);

            if (corrente.equals(partenza)) {
                break;
            }

            Distance<String> informazioni = distanze.get(corrente);
            corrente = informazioni == null ? null : informazioni.getPrev();
        }

        if (percorso.isEmpty()
                || !percorso.get(0).equals(partenza)) {
            return new ArrayList<>();
        }

        return percorso;
    }

    public HashMap<String, Distance<String>> distancesFrom(String nodo) {
        return graph.dijkstra(nodo);
    }

    public List<String> getNodes() {
        return new ArrayList<>(graph.nodes.keySet());
    }

    @Override
    public String toString() {
        return graph.toString();
    }
}