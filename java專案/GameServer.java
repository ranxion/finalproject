import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class GameServer {
    private final Map<String, Warrior> warriors = new HashMap<>(); // 所有角色
    private final Map<Socket, ObjectOutputStream> clients = new HashMap<>(); // 客戶端連接
    private final Map<Socket, Team> clientTeams = new HashMap<>(); // 客戶端分配的隊伍
    private Turn currentTurn = Turn.FIRST;

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.startServer(8080);
    }

    public void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("伺服器已啟動，埠號：" + port);

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

            System.out.println("兩個客戶端已連接，遊戲開始！");
            initializeGame();
            broadcastInitialState();
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

                if (received instanceof SummonRequest) {
                    handleSummonRequest((SummonRequest) received, clientSocket);
                } else if (received instanceof Warrior) {
                    Warrior updatedWarrior = (Warrior) received;
                    warriors.put(updatedWarrior.getName(), updatedWarrior);
                    broadcastGameState();
                } else if ("END_TURN".equals(received)) {
                    switchTurn();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnectClient(clientSocket);
        }
    }

    private void handleSummonRequest(SummonRequest request, Socket clientSocket) {
        Team team = clientTeams.get(clientSocket);
        Warrior warriorToSummon = findWarriorByNameAndTeam(request.getWarriorName(), team);
    
        if (warriorToSummon != null) {
            warriorToSummon.updatePosition(request.getPosition());
            warriors.put(warriorToSummon.getName(), warriorToSummon);   
            // 廣播角色狀態給所有客戶端
            broadcastGameState();
            System.out.println("Summoned: " + warriorToSummon.getName() + " at " + request.getPosition());
        }
    }
    

    private Warrior findWarriorByNameAndTeam(String name, Team team) {
        return warriors.values().stream()
                .filter(warrior -> warrior.getName().equals(name) && warrior.team == team)
                .findFirst()
                .orElse(null);
    }

    private void disconnectClient(Socket clientSocket) {
        try {
            clients.remove(clientSocket);
            clientTeams.remove(clientSocket);
            clientSocket.close();
            System.out.println("客戶端已斷開：" + clientSocket.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeGame() {
        createWarriorsForTeam(Team.BLUE);
        createWarriorsForTeam(Team.RED);
    }

    private void createWarriorsForTeam(Team team) {
        Warrior fighter = new Warrior(100, 150, null, team);
        fighter.setName("Fighte_r"+ team);
        fighter.addSkill(new Skill("Skill 1", 25, 50));
        fighter.addSkill(new Skill("Skill 2", 5, 70));

        Warrior archer = new Warrior(50, 80, null, team);
        archer.setName("Archer_" + team);
        archer.addSkill(new Skill("Skill 1", 35, 120));
        archer.addSkill(new Skill("Skill 2", 600, 150));

        Warrior knight = new Warrior(80, 300, null, team);
        knight.setName("Knight_" + team);
        knight.addSkill(new Skill("Skill 1", 40, 30));
        knight.addSkill(new Skill("Skill 2", 70, 30));

        warriors.put(fighter.getName(), fighter);
        warriors.put(archer.getName(), archer);
        warriors.put(knight.getName(), knight);
    }

    private void broadcastInitialState() {
    try {
        for (ObjectOutputStream out : clients.values()) {
            out.writeObject(new LinkedList<>(warriors.values())); // 發送所有角色的資料
            out.flush();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private void broadcastGameState() {
        try {
            for (ObjectOutputStream out : clients.values()) {
                out.writeObject(new LinkedList<>(warriors.values())); // 發送所有角色
                out.writeObject(currentTurn); // 發送當前回合
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void switchTurn() {
        currentTurn = (currentTurn == Turn.FIRST) ? Turn.SECOND : Turn.FIRST;
        broadcastGameState();
    }
}
