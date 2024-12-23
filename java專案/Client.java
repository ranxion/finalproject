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
    
                // 處理角色資料
                if (received instanceof HashMap) {
                    warriors = (HashMap<String, Warrior>) received;
                    // 更新本地遊戲狀態，例如更新 GamePanel
                    System.out.println("角色數據已更新：" + warriors);
                } 
                // 處理隊伍資訊
                else if (received instanceof Team) {
                    Team team = (Team) received;
                    System.out.println("分配到的隊伍：" + team);
                    // 在此可以儲存或更新隊伍資訊
                } 
                // 處理回合資訊
                else if (received instanceof Turn) {
                    Turn turn = (Turn) received;
                    System.out.println("當前回合：" + turn);
                    // 在此可以更新回合資訊，並通知遊戲畫面
                } 
                // 處理其他未知數據
                else {
                    System.out.println("收到未知類型數據：" + received.getClass());
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

