import javax.swing.*;
import java.awt.*;

public class Window extends JPanel{
    public Window(){
    }
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // // 設定畫筆顏色
        // g2d.setColor(Color.BLUE);
        //  // 繪製空心圓
        //  int x = 50;   // 圓心的 x 座標
        //  int y = 50;   // 圓心的 y 座標
        //  int width = 100;  // 圓的寬度
        //  int height = 100; // 圓的高度
        //  g2d.drawOval(x, y, width, height);

    }
    public static void GameWindowGUI() {
        // 主框架
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1500, 1000);
        frame.setResizable(false); // 禁止調整視窗大小

        // 左上部分
        JPanel upperLeftPanel = new JPanel();
        upperLeftPanel.add(new JLabel("左上區域"));

        // 左下部分
        JPanel lowerLeftPanel = new JPanel();
        lowerLeftPanel.add(new JLabel("左下區域"));

        // 左側分上下部分 (4:1 比例)
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperLeftPanel, lowerLeftPanel);
        verticalSplit.setDividerLocation(750); // 設定分隔線位置 (4/5 高度)
        verticalSplit.setResizeWeight(0.8);    // 上部分比例為 4/5
        verticalSplit.setEnabled(false);       // 禁止調整分隔線

        // 右側區域 (繪圖區域)
        Window rightPanel = new Window();

        // 左右分割 (左側占 2/3，右側占 1/3)
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplit, rightPanel);
        horizontalSplit.setDividerLocation(1000); // 設定分隔線位置 (2/3 寬度)
        horizontalSplit.setResizeWeight(0.66);    // 左側比例為 2/3
        horizontalSplit.setEnabled(false);        // 禁止調整分隔線

        // 添加到框架
        frame.add(horizontalSplit);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Window::GameWindowGUI);
    }
}
