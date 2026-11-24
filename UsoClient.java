import java.util.List;

public class UsoClient {
    public static void main(String[] args) throws Exception {
        int personaggio = args.length > 0 ? Integer.parseInt(args[0]) : 0;

        try (Client client = new Client()) {
            client.start();

            DungeonMap mappa = client.login(personaggio);
            System.out.println("Mappa ricevuta:");
            System.out.println(mappa);

            // Il nodo di partenza va fornito in base alle informazioni del server.
            List<String> percorso = mappa.shortestPath("0000", "0602");
            System.out.println("Percorso: " + percorso);
        }
    }
}