
import javax.swing.JFrame;


public class Main {
    public static void main(String[] args) {
        JFrame Windows = new JFrame("New Game");
        Windows.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Windows.setSize(1280, 800);
        Windows.setResizable(false);
        Windows.setFocusable(true);

        GamePanel gameWindow = new GamePanel(Windows);
        Windows.add(gameWindow);
        Windows.setVisible(true);
    }
}
