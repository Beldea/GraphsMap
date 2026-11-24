import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MapClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORTA = 9000;

    public static void main(String[] args) throws IOException {
        int personaggio = args.length > 0 ? Integer.parseInt(args[0]) : 0;

        if (personaggio < 0 || personaggio > 2) {
            throw new IllegalArgumentException("Il personaggio deve essere 0, 1 o 2");
        }

        try (Client client = new Client()) {
            client.start(HOST, PORTA);

            DungeonMap mappa = client.login(personaggio);
            Random random = new Random();

            System.out.println("Personaggio " + personaggio + " collegato.");
            System.out.println("Mappa ricevuta:");
            System.out.println(mappa);

            String posizione = "0000";
            List<String> nodi = mappa.getNodes();

            if (nodi.isEmpty()) {
                throw new IllegalStateException("La mappa non contiene nodi");
            }

            if (!nodi.contains(posizione)) {
                posizione = nodi.get(random.nextInt(nodi.size()));
            }

            System.out.println("Posizione iniziale: " + posizione);

            while (true) {
                List<String> raggiungibili = new ArrayList<>(nodi);
                raggiungibili.remove(posizione);

                if (raggiungibili.isEmpty()) {
                    break;
                }

                String destinazione;
                do {
                    destinazione = raggiungibili.get(random.nextInt(raggiungibili.size()));
                } while (!mappa.containsNode(destinazione));

                List<String> percorso = mappa.shortestPath(posizione, destinazione);

                if (percorso.isEmpty() || percorso.size() < 2) {
                    continue;
                }

                for (int i = 1; i < percorso.size(); i++) {
                    NodeMap passo = new NodeMap(percorso.get(i));
                    String risposta = client.sendCommand(
                            "move," + passo.getColonna() + "," + passo.getRiga());

                    System.out.println(
                            "move " + passo.getColonna() + "," + passo.getRiga()
                                    + " -> " + risposta);

                    posizione = percorso.get(i);

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }
}