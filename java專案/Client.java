import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private HashMap<String, Warrior> warriors;

    public void connectToServer(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            new Thread(() -> listenToServer()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenToServer() {
        try {
            while (true) {
                Object received = in.readObject();
                if (received instanceof HashMap) {
                    warriors = (HashMap<String, Warrior>) received;
                    // 更新本地遊戲狀態，例如更新 GamePanel
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendWarriorUpdate(Warrior warrior) {
        try {
            out.writeObject(warrior);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

