import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Window extends JPanel {
    private Warrior warrior;
    private JPanel controlPanel; // 控制按鈕區域
    private JTextArea infoArea;  // 角色資訊區域

    public Window(JPanel controlPanel, JTextArea infoArea) {
        this.controlPanel = controlPanel;
        this.infoArea = infoArea;

        // 初始化角色
        warrior = new Warrior(100, 10, "Fireball");
        warrior.setPosition(100, 100);

        // 添加滑鼠事件監聽器
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (warrior.getBounds().contains(e.getPoint())) {
                    showControlPanel(); // 顯示控制按鈕和角色資訊
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 繪製角色
        warrior.drawAppearance(g2d);

    }

    // 顯示控制按鈕和角色資訊
    private void showControlPanel() {
        controlPanel.removeAll(); // 清空控制按鈕區域

        // 添加按鈕
        for (int i = 1; i <= 4; i++) {
            JButton button = new JButton("Action " + i);
            controlPanel.add(button);
        }

        // 更新角色資訊
        infoArea.setText("角色資訊：\n");
        infoArea.append("生命值: " + warrior.getHealth() + "\n");
        infoArea.append("移動距離: " + warrior.getMoveDistance() + "\n");
        infoArea.append("技能: " + warrior.getSkill());

        controlPanel.revalidate();
        controlPanel.repaint();
    }

    public static void GameWindowGUI() {
        // 主框架
        JFrame frame = new JFrame("Game Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1500, 1000);
        frame.setResizable(false); // 禁止調整視窗大小

        // 左上部分 (繪圖區域)
        JPanel upperLeftPanel = new JPanel(new BorderLayout());
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));
        infoArea.setText("點擊角色以顯示控制選項和資訊。");
        upperLeftPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);

        JPanel lowerLeftPanel = new JPanel();
        lowerLeftPanel.add(new JLabel("左下區域"));
        // 右下部分 (控制按鈕和角色資訊區域)
        JPanel controlPanel = new JPanel(new GridLayout(1, 4, 10, 10)); // 一排 4 個按鈕
        // 繪圖區域
        Window drawingPanel = new Window(controlPanel, infoArea);

        // 左側分上下部分 (4:1 比例)
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, drawingPanel, lowerLeftPanel);
        verticalSplit.setDividerLocation(750); // 設定分隔線位置 (4/5 高度)
        verticalSplit.setResizeWeight(0.8);
        verticalSplit.setEnabled(false);

        // 右上部分
        JPanel upperRightPanel = new JPanel();
        upperRightPanel.setBackground(Color.LIGHT_GRAY);
        upperRightPanel.add(new JLabel("右上區域"));

        // 右下部分 (控制按鈕和角色資訊區域)
        JPanel lowerRightPanel = new JPanel(new BorderLayout());
        lowerRightPanel.setBackground(Color.DARK_GRAY);
        lowerRightPanel.add(controlPanel, BorderLayout.CENTER);
        lowerRightPanel.add(new JScrollPane(infoArea), BorderLayout.SOUTH);


        // 右側分上下部分 (3:2 比例)
        JSplitPane verticalRightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperRightPanel, lowerRightPanel);
        verticalRightSplit.setDividerLocation(600);
        verticalRightSplit.setResizeWeight(0.6);
        verticalRightSplit.setEnabled(false);

        // 左右分割 (左側占 2/3，右側占 1/3)
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplit, verticalRightSplit);
        horizontalSplit.setDividerLocation(1000);
        horizontalSplit.setResizeWeight(0.66);
        horizontalSplit.setEnabled(false);

        frame.add(horizontalSplit);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Window::GameWindowGUI);
    }
}

