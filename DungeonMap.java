import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DungeonMap {
    private final GraphOriented<String> graph = new GraphOriented<>();

    private DungeonMap() {
    }

    /**
     * Formato atteso:
     * 0000[0001:1;0400:4]0400[0402:2]
     */
    public static DungeonMap parse(String testo) {
        if (testo == null || testo.trim().isEmpty()) {
            throw new IllegalArgumentException("La mappa è vuota");
        }

        DungeonMap mappa = new DungeonMap();
        String[] nodi = testo.trim().split("]");

        for (String parte : nodi) {
            parte = parte.trim();
            if (parte.isEmpty()) {
                continue;
            }

            int parentesi = parte.indexOf('[');
            if (parentesi < 0) {
                throw new IllegalArgumentException("Nodo senza lista di adiacenze: " + parte);
            }

            String sorgente = parte.substring(0, parentesi).trim();
            String adiacenze = parte.substring(parentesi + 1).trim();

            validaId(sorgente);
            mappa.graph.addNode(sorgente);

            if (adiacenze.isEmpty()) {
                continue;
            }

            for (String adiacenza : adiacenze.split(";")) {
                String[] campi = adiacenza.trim().split(":");
                if (campi.length != 2) {
                    throw new IllegalArgumentException(
                            "Adiacenza non valida '" + adiacenza + "' nel nodo " + sorgente);
                }

                String destinazione = campi[0].trim();
                validaId(destinazione);

                float distanza;
                try {
                    distanza = Float.parseFloat(campi[1].trim());
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

    public List<String> shortestPath(String partenza, String destinazione) {
        HashMap<String, Distance<String>> distanze = graph.dijkstra(partenza);
        Distance<String> arrivo = distanze.get(destinazione);

        if (arrivo == null || arrivo.getDistance() == Float.POSITIVE_INFINITY) {
            return new ArrayList<>();
        }

        ArrayList<String> percorso = new ArrayList<>();
        String corrente = destinazione;

        while (corrente != null) {
            percorso.add(0, corrente);
            if (corrente.equals(partenza)) {
                break;
            }

            Distance<String> info = distanze.get(corrente);
            corrente = info == null ? null : info.getPrev();
        }

        if (percorso.isEmpty() || !percorso.get(0).equals(partenza)) {
            return new ArrayList<>();
        }
        return percorso;
    }

    public Map<String, Distance<String>> distancesFrom(String nodo) {
        return graph.dijkstra(nodo);
    }

    @Override
    public String toString() {
        return graph.toString();
    }
}