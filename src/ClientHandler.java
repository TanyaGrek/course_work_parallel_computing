import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;

class ClientHandler extends Thread {
    private final LinkedList<ClientHandler> serverList;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    Indexer Index;

    public ClientHandler(Socket socket, Indexer Ind, LinkedList<ClientHandler> serverList) throws IOException {
        this.socket = socket;
        this.Index = Ind;
        this.serverList = serverList;
         dis = new DataInputStream(socket.getInputStream());
         dos = new DataOutputStream(socket.getOutputStream());
        start();
    }

    @Override
    public void run() {
        String word;
        String Answer;

        try {
            for (ClientHandler vr : serverList) {
                vr.SendToClient("Please enter a word");
            }
            while (true) {
                word = dis.readUTF();
                System.out.println("Client asked for " + word);

                if (word.equals("Exit")) {
                    break;
                } else {
                    Answer = Indexer.search(Arrays.asList(word.split("\\W+")), Index.Index, Index.Files);
                }

                for (ClientHandler vr : serverList) {
                    vr.SendToClient(Answer + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void SendToClient(String msg) throws IOException {
        try {
            dos.writeUTF(msg + "\n");
            dos.flush();
        } catch (IOException ignored) {
            dos.flush();
        }
    }
}