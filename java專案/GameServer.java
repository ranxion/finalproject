// GameServer.java
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private HashMap<String, Warrior> warriors = new HashMap<>();
    private HashMap<Socket, ObjectOutputStream> clients = new HashMap<>();

    public void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                clients.put(clientSocket, out);
                new Thread(() -> handleClient(clientSocket, in)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket, ObjectInputStream in) {
        try {
            while (true) {
                Object received = in.readObject();
                if (received instanceof Warrior) {
                    Warrior updatedWarrior = (Warrior) received;
                    warriors.put(updatedWarrior.getName(), updatedWarrior);
                    broadcastWarriorData();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void broadcastWarriorData() {
        try {
            for (ObjectOutputStream out : clients.values()) {
                out.writeObject(new LinkedList<>(warriors.values()));
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.startServer(8080);
    }
}
