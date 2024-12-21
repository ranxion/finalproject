import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.*;

public class GamePanel extends JPanel {
    private JPanel gamePanel;       // 主要遊戲畫面
    private JPanel cardPanel;       // 卡牌顯示區
    private JPanel controlPanel;    // 角色控制按鈕區
    private JTextArea infoArea;     // 角色資訊區
    private LinkedList<Warrior> warriors;      // 所有遊戲角色都存在這
    private Warrior chracter;
    public GameState gameState=GameState.START;
    public GamePanel(JFrame windows) {
        // 左上部分 (遊戲畫面)
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 繪製所有遊戲物件
                for (Warrior war : warriors) {
                    if(gameState==GameState.FIGHT && war.Body!=null){
                        war.paint(g);
                        war.paintMoveRange(g2d);
                        for(Skill skill:war.getSkills()){
                            war.paintSkillRange(g2d, skill);                            
                        }
                    }
                }         
            }
        };
        
        gamePanel.add(new JLabel("點擊角色以顯示控制選項和資訊。"), BorderLayout.PAGE_START);

        // 左下部分 (卡牌顯示區)
        cardPanel = new JPanel();
        //cardPanel.add(new JLabel("卡牌顯示區"));


        // 右下部分 (控制按鈕和角色資訊區域)
        controlPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        infoArea = new JTextArea();

        // 合併左側上下版面 (上:下 = 4:1)
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gamePanel, cardPanel);
        verticalSplit.setDividerLocation(4 * windows.getHeight() / 5);
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

        // 合併右側上下版面 (上:下 = 3:2)
        JSplitPane verticalRightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperRightPanel, lowerRightPanel);
        verticalRightSplit.setDividerLocation(3 * windows.getHeight() / 5);
        verticalRightSplit.setEnabled(true);

        // 合併左右版面 (左:右 = 2:1)
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplit, verticalRightSplit);
        horizontalSplit.setDividerLocation(2 * windows.getWidth() / 3);
        horizontalSplit.setEnabled(true);

        this.setSize(windows.getWidth(), windows.getHeight());
        this.setVisible(true);
        this.setLayout(new BorderLayout());
        this.add(horizontalSplit, BorderLayout.CENTER);

        // mouse click event -> gamePanel        
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point click_pos = e.getPoint();
                System.out.println("Select : " + gameState);
                System.out.println("Click : " + click_pos.toString());
                switch (gameState) {
                    case CALL:
                        chracter.Body = new Rectangle(click_pos.x, click_pos.y, chracter.size.x, chracter.size.y);
                        gamePanel.repaint();
                        gameState=GameState.FIGHT;
                        System.out.println("Select : " + gameState);
                        return;                
                    case FIGHT:
                        switch (chracter.state) {
                            //判斷移動
                            case MOVE:
                                System.out.println("true");
                                if(chracter.rangeHitBox.contains(click_pos)){
                                    chracter.Move(click_pos);
                                    gamePanel.repaint();
                                    return;
                                }
                                chracter.setcontrol(false);
                                chracter.state=State.NULL;
                                gamePanel.repaint();
                                return;
                            //判斷攻擊                 
                            case ATTACK:
                                for (Warrior war : warriors) {                                       
                                    //判斷是否點擊士兵
                                    if (chracter.rangeHitBox.contains(click_pos) && war.Body.contains(click_pos) && war!=chracter) {
                                        System.out.println("success attack : " + war.getName());
                                        war.setHealth(war.getHealth()-chracter.selectSkill.getRange());
                                        gamePanel.repaint();
                                        chracter.state=State.NULL;
                                        return;
                                    }
                                    chracter.state=State.MOVE;
                                    chracter.setcontrol(true);
                                    gamePanel.repaint();
                                }         
                                return;
                            case NULL:
                                for (Warrior war : warriors) {                                       
                                    //判斷是否點擊士兵
                                    if (war.Body!=null &&war.Body.contains(click_pos)) {
                                        showControlPanel(war);
                                        war.state=State.MOVE;
                                        war.setcontrol(true);
                                        System.out.println("Select : " + war.getName());
                                        chracter=war;
                                        gamePanel.repaint();
                                        return;
                                    }
                                    war.setcontrol(false);
                                    gamePanel.repaint();
                                }
                                clearControlPanel();
                                return;
                        }
                    case START:
                        
                }                
            }            
        });
        
        init();
    }

    // 遊戲內容初始化
    private void init() {
        warriors = new LinkedList<>();

        Warrior testWar = new Warrior(100, 1, 50, null,Team.FIRST);
        testWar.setName("fighter");
        testWar.addSkill(new Skill("sk1", 5, 50));
        testWar.addSkill(new Skill("sk2", 5, 70));
        testWar.addSkill(new Skill("sk3", 5, 90));
        Warrior testArcher = new Warrior(100, 1, 50, null,Team.SECOND);
        testArcher.addSkill(new Skill("sk1", 5, 50));
        testArcher.addSkill(new Skill("sk2", 5, 70));
        testArcher.addSkill(new Skill("sk3", 5, 90));
        testArcher.setName("Archer");
        warriors.add(testArcher);
        warriors.add(testWar);
        showCardPanel(warriors);
        cardPanel.setLayout(new GridLayout(1,warriors.size(),10,10));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    // 顯示控制按鈕和角色資訊
    private void showControlPanel(Warrior warrior) {
        controlPanel.removeAll();
        // 添加技能按鈕
        for (Skill skill : warrior.getSkills()) {
            JButton button = new JButton(skill.getName());
            button.addActionListener(e->{
                warrior.state=State.ATTACK;
                warrior.selectSkill=skill;
                skill.setControl(true);
                gamePanel.repaint();
            });
            controlPanel.add(button);
        }

        // 更新角色資訊
        infoArea.setText("[角色資訊]\n");
        infoArea.append("生命值 : " + warrior.getHealth() + "\n");
        infoArea.append("移動距離 : " + warrior.getMoveRange() + "\n");

        controlPanel.revalidate();
        controlPanel.repaint();
    }

    //顯示卡牌按鈕
    private void showCardPanel(LinkedList<Warrior> WarriorCards){
        for(Warrior wCard: WarriorCards ){
            JButton button = new JButton(wCard.getName());
            button.addActionListener(e->{
                chracter=wCard;
                gameState=GameState.CALL;
                System.out.println("Select : " + gameState);
            });
            cardPanel.add(button);
        }        
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void clearControlPanel() {
        controlPanel.removeAll();
        infoArea.setText("");
        controlPanel.revalidate();
        controlPanel.repaint();
    }
}

