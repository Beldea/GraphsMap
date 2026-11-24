import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class MapClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORTA = 9000;

    public static void main(String[] args) throws IOException {
        int personaggio = args.length > 0
                ? Integer.parseInt(args[0])
                : 0;

        if (personaggio < 0 || personaggio > 2) {
            throw new IllegalArgumentException(
                    "Il personaggio deve essere 0, 1 o 2");
        }

        try (Client client = new Client();
             Scanner console = new Scanner(System.in)) {

            client.start(HOST, PORTA);

            DungeonMap mappa = client.login(personaggio);
            System.out.println("Login effettuato come personaggio " + personaggio);
            System.out.println("Mappa ricevuta:");
            System.out.println(mappa);

            String partenza = "0000";
            String destinazione = "0602";

            List<String> percorso = mappa.shortestPath(partenza, destinazione);
            if (percorso.isEmpty()) {
                System.out.println(
                        "Nessun percorso da " + partenza + " a " + destinazione);
            } else {
                System.out.println("Percorso da " + partenza + " a " + destinazione + ":");
                System.out.println(String.join(" -> ", percorso));
            }

            System.out.println();
            System.out.println("Personaggio ancora collegato.");
            System.out.println("Premi Invio per uscire e inviare logout.");
            console.nextLine();
        }
    }
}