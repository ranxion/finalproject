import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.*;

public class GamePanel extends JPanel {
    private JPanel gamePanel;       // 主要遊戲畫面
    private JPanel cardPanel;       // 卡牌顯示區
    private JPanel controlPanel;    // 角色控制按鈕區
    private JPanel EnemycontrolPanel; // 敵方角色資訊區
    private JTextArea infoArea;     // 角色資訊區
    private JTextArea EnemyinfoArea;    // 敵方角色資訊區
    private LinkedList<Warrior> warriors;      // 所有遊戲角色都存在這
    private Warrior chracter;
    public GameState gameState=GameState.START;
    public turn GameTurn=turn.SECOND;
    public Team playerTeam=Team.RED;
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
                    if(war.Body!=null){
                        war.paint(g);
                        if(war.selectControl){
                            war.paintMoveRange(g2d);
                        }
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


        // 右下部分 (控制按鈕和角色資訊區域)
        controlPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        infoArea = new JTextArea();

        // 右中部分 (控制按鈕和角色資訊區域)
        EnemycontrolPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        EnemyinfoArea = new JTextArea();

        // 合併左側上下版面 (上:下 = 4:1)
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gamePanel, cardPanel);
        verticalSplit.setDividerLocation(4 * windows.getHeight() / 5);
        verticalSplit.setResizeWeight(0.8);
        verticalSplit.setEnabled(false);

        // 右上部分
        JPanel upperRightPanel = new JPanel();
        upperRightPanel.setBackground(Color.LIGHT_GRAY);
        upperRightPanel.add(new JLabel("右上區域"));

        // 右中部分
        JPanel midRightPanel= new JPanel();
        midRightPanel.setBackground(Color.DARK_GRAY);
        midRightPanel.add(EnemycontrolPanel, BorderLayout.CENTER);
        midRightPanel.add(new JScrollPane(EnemyinfoArea), BorderLayout.SOUTH);

        // 右下部分 (控制按鈕和角色資訊區域)
        JPanel lowerRightPanel = new JPanel(new BorderLayout());
        lowerRightPanel.setBackground(Color.DARK_GRAY);
        lowerRightPanel.add(controlPanel, BorderLayout.CENTER);
        lowerRightPanel.add(new JScrollPane(infoArea), BorderLayout.SOUTH);


        // 分割上方與中間 (1:1)
        JSplitPane upperMiddleSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperRightPanel, midRightPanel);
        upperMiddleSplit.setDividerLocation(windows.getHeight() / 3); // 設定分隔線位置
        upperMiddleSplit.setResizeWeight(0.5); // 平分空間
        upperMiddleSplit.setEnabled(false);   // 禁止調整分隔線

        // 再分割中間與下方 (1:1:1)
        JSplitPane verticalRightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperMiddleSplit, lowerRightPanel);
        verticalRightSplit.setDividerLocation(2 * windows.getHeight() / 3); // 設定第二條分隔線位置
        verticalRightSplit.setResizeWeight(0.67);  // 上部分為 2/3 (自動按比例分配)
        verticalRightSplit.setEnabled(false);      // 禁止調整分隔線

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
                switch (GameTurn) {
                    case FIRST:
                        if(playerTeam==Team.BLUE){
                            switch (gameState) {
                                case CALL:
                                    chracter.Body = new Rectangle(click_pos.x, click_pos.y, chracter.size.x, chracter.size.y);
                                    gamePanel.repaint();
                                    gameState=GameState.START;
                                    System.out.println("Select : " + gameState);
                                    return;                
                                case FIGHT:
                                    switch (chracter.state) {
                                        //判斷移動
                                        case MOVE:
                                            if(chracter.rangeHitBox.contains(click_pos)){
                                                chracter.Move(click_pos);
                                                gamePanel.repaint();
                                                return;
                                            }
                                            chracter.selectControl=false;
                                            chracter.state=State.NULL;
                                            gameState=GameState.START;
                                            gamePanel.repaint();
                                            return;
                                        //判斷攻擊                 
                                        case ATTACK:
                                            for (Warrior war : warriors) {                                       
                                                //判斷是否點擊士兵
                                                if (chracter.rangeHitBox.contains(click_pos) && war.Body.contains(click_pos) && chracter.team!=war.team) {
                                                    System.out.println("success attack : " + war.getName());
                                                    if(chracter.team==Team.BLUE){
                                                        chracter.Attack(war);
                                                        gamePanel.repaint();
                                                        gameState=GameState.START;
                                                        return;
                                                    }
                                                }
                                                chracter.state=State.MOVE;
                                                gamePanel.repaint();
                                            }         
                                            return;
                                        case NULL:
                                            for (Warrior war : warriors) {                                       
                                                //判斷是否點擊士兵
                                                if (war.Body!=null &&war.Body.contains(click_pos)) {
                                                    chracter=war;
                                                    showControlPanel(chracter);
                                                    chracter.state=State.MOVE;
                                                    chracter.selectControl=true;
                                                    System.out.println("Select : " + war.getName());
            
                                                    return;
                                                }
                                                gameState=GameState.START;
                                                gamePanel.repaint();
                                            }
                                            clearControlPanel();
                                            return;
                                    }
                                case START:
                                    
                                    for (Warrior war : warriors) {                                       
                                        //判斷是否點擊士兵
                                        if (war.Body!=null && war.Body.contains(click_pos)) {
                                            chracter=war;
                                            showControlPanel(chracter);
                                            if(chracter.team==Team.BLUE){
                                                gameState=GameState.FIGHT;
                                                chracter.state=State.MOVE;
                                                chracter.selectControl=true;
                                                System.out.println("Select : " + war.getName());
                                                gamePanel.repaint();
                                                return;
                                            }
                                            else{
                                                clearEnemyControlPanel();
                                                EnemyshowControlPanel(chracter);
                                                return;
                                            }
                                        }
                                    }
                                    clearControlPanel();
                                    return;
                            }
                        }
                        else{
                            for (Warrior war : warriors) {                                       
                                //判斷是否點擊士兵
                                if (war.Body!=null && war.Body.contains(click_pos)) {
                                    chracter=war;
                                    showControlPanel(chracter);
                                    System.out.println("Select : " + war.getName());
                                    return;
                                }
                            }
                            clearControlPanel();
                            return;
                        }
                
                    case SECOND:
                        if(playerTeam==Team.RED){
                            switch (gameState) {
                                case CALL:
                                    chracter.Body = new Rectangle(click_pos.x, click_pos.y, chracter.size.x, chracter.size.y);
                                    gamePanel.repaint();
                                    gameState=GameState.START;
                                    System.out.println("Select : " + gameState);
                                    return;                
                                case FIGHT:
                                    switch (chracter.state) {
                                        //判斷移動
                                        case MOVE:
                                            if(chracter.rangeHitBox.contains(click_pos)){
                                                chracter.Move(click_pos);
                                                gamePanel.repaint();
                                                return;
                                            }
                                            chracter.selectControl=false;
                                            chracter.state=State.NULL;
                                            gameState=GameState.START;
                                            gamePanel.repaint();
                                            return;
                                        //判斷攻擊                 
                                        case ATTACK:
                                            for (Warrior war : warriors) {                                       
                                                //判斷是否點擊士兵
                                                if (chracter.rangeHitBox.contains(click_pos) && war.Body.contains(click_pos) && chracter.team!=war.team) {
                                                    System.out.println("success attack : " + war.getName());
                                                    if(chracter.team==Team.RED){
                                                        chracter.Attack(war);
                                                        gamePanel.repaint();
                                                        gameState=GameState.START;
                                                        return;
                                                    }
                                                }
                                                chracter.state=State.MOVE;
                                                gamePanel.repaint();
                                            }         
                                            return;
                                        case NULL:
                                            for (Warrior war : warriors) {                                       
                                                //判斷是否點擊士兵
                                                if (war.Body!=null &&war.Body.contains(click_pos)) {
                                                    chracter=war;
                                                    showControlPanel(chracter);
                                                    chracter.state=State.MOVE;
                                                    chracter.selectControl=true;
                                                    System.out.println("Select : " + war.getName());
            
                                                    return;
                                                }
                                                gameState=GameState.START;
                                                gamePanel.repaint();
                                            }
                                            clearControlPanel();
                                            return;
                                    }
                                case START:
                                    
                                    for (Warrior war : warriors) {                                       
                                        //判斷是否點擊士兵
                                        if (war.Body!=null && war.Body.contains(click_pos)) {
                                            chracter=war;
                                            if(chracter.team==Team.RED){
                                                showControlPanel(chracter);
                                                gameState=GameState.FIGHT;
                                                chracter.state=State.MOVE;
                                                chracter.selectControl=true;
                                                System.out.println("Select : " + war.getName());
                                                gamePanel.repaint();
                                                return;
                                            }else{
                                                clearEnemyControlPanel();
                                                EnemyshowControlPanel(chracter);
                                                return;
                                            }
                                        }
                                    }
                                    clearControlPanel();
                                    return;
                            }
                        }
                        else{
                            for (Warrior war : warriors) {                                       
                                //判斷是否點擊士兵
                                if (war.Body!=null && war.Body.contains(click_pos)) {
                                    chracter=war;
                                    if(chracter.team==Team.BLUE){
                                        showControlPanel(chracter);
                                        System.out.println("Select : " + war.getName());
                                        return;
                                    }else{
                                        clearEnemyControlPanel();
                                        EnemyshowControlPanel(chracter);
                                        return;
                                    }
                                }
                            }
                            clearControlPanel();
                            return;
                        }
                }               
            }            
        });
        
        init();
    }

    // 遊戲內容初始化
    private void init() {
        warriors = new LinkedList<>();

        Warrior testWar = new Warrior(100, 1, 50, null,Team.BLUE);
        testWar.setName("fighter");
        testWar.addSkill(new Skill("sk1", 5, 50));
        testWar.addSkill(new Skill("sk2", 5, 70));
        testWar.addSkill(new Skill("sk3", 5, 90));
        Warrior testArcher = new Warrior(100, 1, 50, null,Team.RED);
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
                switch (GameTurn) {
                    case FIRST:
                        if(playerTeam==Team.BLUE && chracter.team==Team.BLUE){
                            warrior.state=State.ATTACK;
                            warrior.selectSkill=skill;
                            skill.setControl(true);
                            gamePanel.repaint();
                        }
                        break;
                
                    default:
                        if(playerTeam==Team.RED && chracter.team==Team.RED){
                            warrior.state=State.ATTACK;
                            warrior.selectSkill=skill;
                            skill.setControl(true);
                            gamePanel.repaint();
                        }
                        break;
                }
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

    // 顯示控制按鈕和敵方角色資訊
    private void EnemyshowControlPanel(Warrior warrior) {
        EnemycontrolPanel.removeAll();
        // 添加技能按鈕
        for (Skill skill : warrior.getSkills()) {
            JButton button = new JButton(skill.getName());           
            EnemycontrolPanel.add(button);
        }

        // 更新角色資訊
        EnemyinfoArea.setText("[角色資訊]\n");
        EnemyinfoArea.append("生命值 : " + warrior.getHealth() + "\n");
        EnemyinfoArea.append("移動距離 : " + warrior.getMoveRange() + "\n");

        EnemycontrolPanel.revalidate();
        EnemycontrolPanel.repaint();
    }

    //顯示卡牌按鈕
    private void showCardPanel(LinkedList<Warrior> WarriorCards){
        for(Warrior wCard: WarriorCards ){
            JButton button = new JButton(wCard.getName());
            button.addActionListener(e->{
                if(chracter!=null){
                    chracter.state=State.NULL;
                    gamePanel.repaint();
                }
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
    private void clearEnemyControlPanel() {
        EnemycontrolPanel.removeAll();
        EnemyinfoArea.setText("");
        EnemycontrolPanel.revalidate();
        EnemycontrolPanel.repaint();
    }
}

