import java.io.IOException;
import java.util.Random;

/**
 * Client per il server sulla porta 9000.
 *
 * 1. Riceve START,colonna,riga (posizione iniziale).
 * 2. Sceglie una destinazione a caso, calcola lo shortest path (Dijkstra) sul grafo
 *    della mappa e invia un comando move,colonna,riga per ogni passo del percorso.
 * 3. All'arrivo il server risponde OK,colonna,riga.
 * 4. Ripete i punti 2-3 per TRATTE volte (la prima destinazione piu' le successive).
 *
 * Uso: java MapClient [colonne] [righe]
 */
public class MapClient {

    // dimensioni della mappa (il server non le comunica: vanno impostate o passate da riga di comando)
    private static final int COLONNE = 10;
    private static final int RIGHE = 10;

    // numero di destinazioni da raggiungere
    private static final int TRATTE = 3;

    public static void main(String[] args) throws IOException {
        int colonne = args.length > 0 ? Integer.parseInt(args[0]) : COLONNE;
        int righe = args.length > 1 ? Integer.parseInt(args[1]) : RIGHE;

        Map mappa = costruisciMappa(colonne, righe);

        Client client = new Client();
        client.start();

        try {
            // messaggio iniziale START,colonna,riga
            int[] start = leggiPosizione(client.readMessage(), "START");
            String corrente = NodeMap.buildId(start[1], start[0]);
            if (mappa.getGridNode(corrente) == null) {
                throw new IllegalStateException("Posizione iniziale fuori dalla mappa: " + corrente);
            }
            System.out.println("START in colonna " + start[0] + ", riga " + start[1]);

            Random random = new Random();

            for (int tratta = 1; tratta <= TRATTE; tratta++) {
                String destinazione = scegliDestinazione(colonne, righe, corrente, random);
                String[] percorso = mappa.dijkstra(corrente, destinazione);
                NodeMap dest = mappa.getGridNode(destinazione);

                System.out.println("Tratta " + tratta + "/" + TRATTE + ": da " + corrente
                        + " a " + destinazione + " in " + (percorso.length - 1) + " passi");

                if (percorso.length == 0) {
                    throw new IllegalStateException("Destinazione non raggiungibile: " + destinazione);
                }

                // percorso[0] e' la posizione attuale: si parte dal secondo nodo
                String risposta = null;
                for (int i = 1; i < percorso.length; i++) {
                    NodeMap passo = mappa.getGridNode(percorso[i]);
                    risposta = client.sendMessage("move," + passo.getColonna() + "," + passo.getRiga());
                    System.out.println("  move " + passo.getColonna() + "," + passo.getRiga() + " -> " + risposta);
                }

                // l'ultima risposta deve essere OK,colonna,riga della destinazione
                int[] ok = leggiPosizione(risposta, "OK");
                if (ok[0] != dest.getColonna() || ok[1] != dest.getRiga()) {
                    throw new IllegalStateException("Il server conferma una posizione diversa da " + destinazione
                            + ": " + risposta);
                }
                corrente = destinazione;
            }

            System.out.println("Fatto: raggiunte " + TRATTE + " destinazioni");
        } finally {
            client.stopConnection();
        }
    }

    /** Mappa a griglia colonne x righe, tutte le celle collegate alle 4 vicine. */
    private static Map costruisciMappa(int colonne, int righe) {
        Map mappa = new Map();
        for (int r = 0; r < righe; r++) {
            for (int c = 0; c < colonne; c++) {
                mappa.addNode(r, c);
            }
        }
        for (int r = 0; r < righe; r++) {
            for (int c = 0; c < colonne; c++) {
                String id = NodeMap.buildId(r, c);
                if (c + 1 < colonne) mappa.addEdge(id, NodeMap.buildId(r, c + 1));
                if (r + 1 < righe) mappa.addEdge(id, NodeMap.buildId(r + 1, c));
            }
        }
        return mappa;
    }

    /** Cella a caso diversa da quella in cui ci si trova. */
    private static String scegliDestinazione(int colonne, int righe, String corrente, Random random) {
        String dest;
        do {
            dest = NodeMap.buildId(random.nextInt(righe), random.nextInt(colonne));
        } while (dest.equals(corrente));
        return dest;
    }

    /** Legge "TIPO,colonna,riga" e ritorna {colonna, riga}. */
    private static int[] leggiPosizione(String messaggio, String tipoAtteso) {
        if (messaggio == null) {
            throw new IllegalStateException("Connessione chiusa dal server (atteso " + tipoAtteso + ")");
        }
        String[] parti = messaggio.trim().split(",");
        if (parti.length != 3 || !parti[0].trim().equalsIgnoreCase(tipoAtteso)) {
            throw new IllegalStateException("Messaggio inatteso (atteso " + tipoAtteso + ",colonna,riga): " + messaggio);
        }
        return new int[] { Integer.parseInt(parti[1].trim()), Integer.parseInt(parti[2].trim()) };
    }
}