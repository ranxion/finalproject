import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class GameStartUI {
    

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Game Start UI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Welcome to the Game", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton startButton = new JButton("Start Game");
        JButton exitButton = new JButton("Exit");
        buttonPanel.add(startButton);
        buttonPanel.add(exitButton);
        panel.add(buttonPanel, BorderLayout.CENTER);

        startButton.addActionListener(e -> {
            frame.dispose(); // 關閉主畫面
            connectToServer("localhost", 8080); // 預設連接本機伺服器
        });

        exitButton.addActionListener(e -> System.exit(0)); // 結束程式

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void connectToServer(String host, int port) {
        JFrame waitingFrame = new JFrame("Connecting to Server");
        waitingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        waitingFrame.setSize(300, 200);
        waitingFrame.setResizable(false);

        JLabel waitingLabel = new JLabel("Connecting to " + host + ":" + port + "...", SwingConstants.CENTER);
        waitingLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        waitingFrame.add(waitingLabel, BorderLayout.CENTER);

        waitingFrame.setVisible(true);

        new Thread(() -> {
            try (Socket socket = new Socket(host, port)) {
                waitingFrame.dispose();
                System.out.println("accesss connet");
                startGame(host, port); // 連線成功，啟動遊戲畫面
            } catch (IOException e) {
                JOptionPane.showMessageDialog(waitingFrame,
                        "Unable to connect to the server.\n" + e.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                waitingFrame.dispose();
                createAndShowGUI(); // 返回主畫面
            }
        }).start();
    }

    private static void startGame(String host, int port) {
        System.out.println("accesss");
        JFrame gameFrame = new JFrame("Game");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(1280, 800);
        gameFrame.setResizable(false);

        try {
            GamePanel gamePanel = new GamePanel(gameFrame, host, port);
            gameFrame.add(gamePanel);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gameFrame,
                    "Failed to start the game.\n" + e.getMessage(),
                    "Game Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        
        gameFrame.setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameStartUI::createAndShowGUI);
    }
}
