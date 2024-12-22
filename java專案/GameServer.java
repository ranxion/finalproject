import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket, ObjectInputStream in) {
        try {
            while (true) {
                Object received = in.readObject();
                
                if (received instanceof String) {
                    // 處理簡單指令，例如要求資料的指令
                    String command = (String) received;
                    if ("GET_WARRIORS".equals(command)) {
                        sendWarriorData(clientSocket);
                    }
                } else if (received instanceof Warrior) {
                    // 接收並更新 Warrior 資料
                    Warrior updatedWarrior = (Warrior) received;
                    warriors.put(updatedWarrior.getName(), updatedWarrior);
                    broadcastWarriorData();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendWarriorData(Socket clientSocket) {
        try {
            ObjectOutputStream out = clients.get(clientSocket);
            out.writeObject(warriors);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastWarriorData() {
        try {
            for (ObjectOutputStream out : clients.values()) {
                out.writeObject(warriors);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
