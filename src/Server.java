import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Server {
    public static final int PORT = 59057;
    public static LinkedList<ClientHandler> serverList = new LinkedList<>();

    public static void main() throws IOException {
        ServerSocket ss = new ServerSocket(PORT);
        Indexer Index;
        Index = Indexer.main();

        Socket s = null;
        try {
            while (true) {
                try {
                    s = ss.accept();
                    System.out.println("A new client is connected : " + s);
                    try {
                        serverList.add(new ClientHandler(s, Index, serverList));
                    } catch (IOException e) {
                        s.close();
                        System.out.print("Error: " + e);
                    }
                } catch (Exception e) {
                    assert s != null;
                    s.close();
                    e.printStackTrace();
                }
            }
        } finally {
            assert s != null;
            s.close();
        }
    }
}
