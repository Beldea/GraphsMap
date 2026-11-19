import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void start() throws IOException {
        start("127.0.0.1", 9000);
    }

    public void start(String host, int port) throws IOException {
        socket = new Socket(host, port);
        // autoflush = true: senza, println non invia nulla finche' il buffer non si riempie
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /** Legge una riga inviata dal server (es. il messaggio START iniziale). */
    public String readMessage() throws IOException {
        return in.readLine();
    }

    /** Invia una riga al server e ritorna la sua risposta. */
    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}