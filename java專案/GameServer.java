import java.awt.Point;
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private final Map<String, Warrior> warriors = new HashMap<>(); // 所有角色
    private final Map<Socket, ObjectOutputStream> clients = new HashMap<>(); // 客戶端連接
    private final Map<Socket, Team> clientTeams = new HashMap<>(); // 客戶端分配的隊伍
    private Turn currentTurn = Turn.FIRST;
    public int panelWidth,panelHeight;
    private boolean hasSummoned = false;

    public static void main(String[] args) {
        GameServer server = new GameServer(840, 640);
        server.startServer(8080);
    }

    public GameServer(int panelWidth, int panelHeight) {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
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
                    synchronized (warriors) {
                        warriors.put(updatedWarrior.getName(), updatedWarrior);
                    }
                    System.out.println("Updated warrior: " + updatedWarrior.getName());
                    broadcastGameState();
                    checkVictory(); // 每次角色狀態更新後檢查勝利條件
                } else if ("END_TURN".equals(received)) {
                    switchTurn();
                    checkVictory(); // 每次角色狀態更新後檢查勝利條件
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling client data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            disconnectClient(clientSocket);
        }
    }

    private void handleSummonRequest(SummonRequest request, Socket clientSocket) {
        Team team = clientTeams.get(clientSocket);
    
        if (hasSummoned) {
            System.out.println("Summoning already done this turn.");
            sendErrorMessage(clientSocket, "You can only summon once per turn!");
            return;
        }
    
        Warrior warriorToSummon = findWarriorByNameAndTeam(request.getWarriorName(), team);
        if (warriorToSummon != null) {
            warriorToSummon.updatePosition(request.getPosition());
            warriors.put(warriorToSummon.getName(), warriorToSummon);
            hasSummoned = true; // 標記本回合已經召喚
            broadcastGameState(); // 廣播遊戲狀態
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
        initializeKings(); // 初始化國王角色
        createWarriorsForTeam(Team.BLUE);
        createWarriorsForTeam(Team.RED);
    }

    private void initializeKings() {
        Warrior blueKing = new Warrior(100, 0,null, Team.BLUE,type.Fighter);
        blueKing.updatePosition(new Point(0, panelHeight / 2 - blueKing.size.y / 2));
        blueKing.setName("King_Blue");
        blueKing.addSkill(new Skill("Royal Strike", 50, 50,2));
        warriors.put(blueKing.getName(), blueKing); // 加入藍隊國王到角色列表
    
        Warrior redKing = new Warrior(100, 0,null, Team.RED,type.Fighter);
        redKing.updatePosition(new Point(panelWidth - redKing.size.x, panelHeight / 2 - redKing.size.y / 2));
        redKing.setName("King_Red");
        redKing.addSkill(new Skill("Royal Strike", 50, 50,2));
        warriors.put(redKing.getName(), redKing); // 加入紅隊國王到角色列表
    }
    

    private void createWarriorsForTeam(Team team) {
        Warrior fighter = new Warrior(100, 150, null, team,type.Fighter);
        fighter.setName("Fighter_"+ team);
        fighter.addSkill(new Skill("Skill 1", 25, 50,0));
        fighter.addSkill(new Skill("Skill 2", 50, 70,1));

        Warrior archer = new Warrior(50, 80, null, team,type.Archer);
        archer.setName("Archer_" + team);
        archer.addSkill(new Skill("Skill 1", 35, 120,0));
        archer.addSkill(new Skill("Skill 2", 70, 150,3));

        Warrior knight = new Warrior(80, 300, null, team,type.Knight);
        knight.setName("Knight_" + team);
        knight.addSkill(new Skill("Skill 1", 40, 30,0));
        knight.addSkill(new Skill("Skill 2", 70, 30,2));

        warriors.put(fighter.getName(), fighter);
        warriors.put(archer.getName(), archer);
        warriors.put(knight.getName(), knight);
    }

    private void checkVictory() {
        boolean blueKingAlive = warriors.values().stream().anyMatch(w -> w.getName().equals("King_Blue") && w.getHealth() > 0);
        boolean redKingAlive = warriors.values().stream().anyMatch(w -> w.getName().equals("King_Red") && w.getHealth() > 0);
    
        if (!blueKingAlive) {
            broadcastMessage("Red wins!");
            try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            System.exit(0);
        } else if (!redKingAlive) {
            broadcastMessage("Blue wins!");
            try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            System.exit(0);
        }
    }
    private void broadcastMessage(String message) {
        try {
            for (ObjectOutputStream out : clients.values()) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendErrorMessage(Socket clientSocket, String message) {
        try {
            ObjectOutputStream out = clients.get(clientSocket);
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private void broadcastInitialState() {
        try {
            LinkedList<Warrior> allWarriors = new LinkedList<>(warriors.values());
            for (ObjectOutputStream out : clients.values()) {
                out.writeObject(allWarriors); // 發送所有角色（包括國王）給客戶端
                out.writeObject(currentTurn); // 發送當前回合
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
        hasSummoned = false; // 重置召喚標記
        checkVictory(); // 每次角色狀態更新後檢查勝利條件
        broadcastGameState();
    }
}
