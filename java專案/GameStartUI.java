// GameStartUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class GameStartUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameStartUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // 建立主框架
        JFrame frame = new JFrame("Game Start UI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setResizable(false);

        // 建立主要面板
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // 添加標題
        JLabel title = new JLabel("Welcome to the Game", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        // 添加按鈕
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton startButton = new JButton("Start Game");
        JButton exitButton = new JButton("Exit");

        buttonPanel.add(startButton);
        buttonPanel.add(exitButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        // 添加事件監聽器
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // 關閉當前窗口
                showServerList();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // 結束程式
            }
        });

        // 顯示框架
        frame.add(panel);
        frame.setVisible(true);
    }

    private static void showServerList() {
        JFrame serverListFrame = new JFrame("Available Servers");
        serverListFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverListFrame.setSize(400, 300);
        serverListFrame.setResizable(false);

        JPanel serverPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Available Servers", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        serverPanel.add(titleLabel, BorderLayout.NORTH);

        DefaultListModel<String> serverListModel = new DefaultListModel<>();
        JList<String> serverList = new JList<>(serverListModel);
        JScrollPane scrollPane = new JScrollPane(serverList);
        serverPanel.add(scrollPane, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel("Searching for servers...", SwingConstants.CENTER);
        serverPanel.add(statusLabel, BorderLayout.SOUTH);

        serverListFrame.add(serverPanel);
        serverListFrame.setVisible(true);

        // 實際伺服器搜索
        new Thread(() -> {
            try {
                ArrayList<String> servers = discoverServers();
                SwingUtilities.invokeLater(() -> {
                    if (servers.isEmpty()) {
                        statusLabel.setText("No servers found.");
                    } else {
                        statusLabel.setText("Select a server to join.");
                        for (String server : servers) {
                            serverListModel.addElement(server);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> statusLabel.setText("Error discovering servers."));
            }
        }).start();

        serverList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                String selectedServer = serverList.getSelectedValue();
                connectToServer(selectedServer);
                serverListFrame.dispose();
            }
        });
    }

    private static ArrayList<String> discoverServers() {
        ArrayList<String> servers = new ArrayList<>();
        try {
            for (int port = 8080; port <= 8090; port++) {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress("localhost", port), 1000);
                    servers.add("Server at localhost:" + port);
                } catch (IOException ignored) {
                    // 沒有伺服器在此埠口運行
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return servers;
    }

    private static void connectToServer(String server) {
        JFrame waitingFrame = new JFrame("Connecting to Server");
        waitingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        waitingFrame.setSize(300, 200);
        waitingFrame.setResizable(false);

        JPanel waitingPanel = new JPanel(new BorderLayout());
        JLabel waitingLabel = new JLabel("Connecting to " + server + "...", SwingConstants.CENTER);
        waitingLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        waitingPanel.add(waitingLabel, BorderLayout.CENTER);

        waitingFrame.add(waitingPanel);
        waitingFrame.setVisible(true);

        new Thread(() -> {
            try {
                // 模擬連線
                Thread.sleep(2000);
                SwingUtilities.invokeLater(() -> {
                    waitingFrame.dispose();
                    startGame(server);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void startGame(String server) {
        JFrame gameFrame = new JFrame("Game");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(1280, 800);
        gameFrame.setResizable(false);

        // 示例：添加遊戲面板
        try {
            String[] parts = server.split(":");
            String host = parts[1].trim();
            int port = Integer.parseInt(parts[2].trim());
            GamePanel gamePanel = new GamePanel(gameFrame, host, port);
            gameFrame.add(gamePanel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        gameFrame.setVisible(true);
    }
}
