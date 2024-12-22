// GameServer.java
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

            System.out.println("Two clients connected. Starting the game...");
            initializeGame();
            broadcastGameState();

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
                 else if (received instanceof String && "END_TURN".equals(received)) {
                    switchTurn();
                    broadcastGameState(); // 確保回合切換後，廣播最新的遊戲狀態
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnectClient(clientSocket);
        }
    }
    

    private void disconnectClient(Socket clientSocket) {
        try {
            clients.remove(clientSocket);
            clientTeams.remove(clientSocket);
            clientSocket.close();
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastGameState() {
        try {
            for (ObjectOutputStream out : clients.values()) {
                out.writeObject(new LinkedList<>(warriors.values()));
                out.writeObject(currentTurn);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeGame() {
        // Initialize warriors or any game-specific settings
        warriors = new HashMap<>();

        // 初始化角色
        Warrior blueFighter = new Warrior(100, 150, null, Team.BLUE);
        blueFighter.setName("Fighter");
        blueFighter.addSkill(new Skill("Skill 1", 25, 50));
        blueFighter.addSkill(new Skill("Skill 2", 5, 70));
    
        Warrior blueArcher = new Warrior(50, 80, null, Team.BLUE);
        blueArcher.setName("Archer");
        blueArcher.addSkill(new Skill("Skill 1", 35, 120));
        blueArcher.addSkill(new Skill("Skill 2", 600, 150));
    
        Warrior blueKnight = new Warrior(80, 300, null, Team.BLUE);
        blueKnight.setName("Knight");
        blueKnight.addSkill(new Skill("Skill 1", 40, 30));
        blueKnight.addSkill(new Skill("Skill 2", 70, 30));
    
        warriors.put(blueFighter.getName(), blueFighter);
        warriors.put(blueArcher.getName(), blueArcher);
        warriors.put(blueKnight.getName(), blueKnight);
    
        Warrior redFighter = new Warrior(100, 150, null, Team.RED);
        redFighter.setName("Fighter");
        redFighter.addSkill(new Skill("Skill 1", 25, 50));
        redFighter.addSkill(new Skill("Skill 2", 5, 70));
    
        Warrior redArcher = new Warrior(50, 80, null, Team.RED);
        redArcher.setName("Archer");
        redArcher.addSkill(new Skill("Skill 1", 35, 120));
        redArcher.addSkill(new Skill("Skill 2", 600, 150));
    
        Warrior redKnight = new Warrior(80, 300, null, Team.RED);
        redKnight.setName("Knight");
        redKnight.addSkill(new Skill("Skill 1", 40, 30));
        redKnight.addSkill(new Skill("Skill 2", 70, 30));
    
        warriors.put(redFighter.getName(), redFighter);
        warriors.put(redArcher.getName(), redArcher);
        warriors.put(redKnight.getName(), redKnight);
    
        broadcastGameState();
    }

    private void switchTurn() {
        currentTurn = (currentTurn == Turn.FIRST) ? Turn.SECOND : Turn.FIRST;
        broadcastTurn(currentTurn);
    }

    private void broadcastTurn(Turn turn) {
        try {
            for (ObjectOutputStream out : clients.values()) {
                out.writeObject(turn);
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
