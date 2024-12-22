// 修改後的 GameServer.java
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private HashMap<String, Warrior> warriors = new HashMap<>();
    private HashMap<Socket, ObjectOutputStream> clients = new HashMap<>();
    private Map<Socket, Team> clientTeams = new HashMap<>();
    private Turn currentTurn = Turn.FIRST;

    public void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);

            while (clients.size() < 2) {
                Socket clientSocket = serverSocket.accept();
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                Team assignedTeam = clients.isEmpty() ? Team.BLUE : Team.RED;
                clientTeams.put(clientSocket, assignedTeam);

                clients.put(clientSocket, out);
                sendTeamAssignment(out, assignedTeam);

                new Thread(() -> handleClient(clientSocket, in)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTeamAssignment(ObjectOutputStream out, Team team) {
        try {
            out.writeObject(team);
            out.flush();
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
                    broadcastGameState();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void broadcastGameState() {
        try {
            for (Map.Entry<Socket, ObjectOutputStream> entry : clients.entrySet()) {
                entry.getValue().writeObject(new LinkedList<>(warriors.values()));
                entry.getValue().writeObject(currentTurn);
                entry.getValue().flush();
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

