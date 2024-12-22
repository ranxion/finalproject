// Main.java
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 8080;

        JFrame window = new JFrame("Multiplayer Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(1280, 800);
        window.setResizable(false);

        try {
            GamePanel gamePanel = new GamePanel(window, serverAddress, port);
            window.add(gamePanel);
        } catch (Exception e) {
            System.err.println("Unable to connect to server: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        window.setVisible(true);
    }
}

