import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class GamePanel extends JPanel {
    private JPanel gamePanel;       // 主要遊戲畫面
    private JPanel cardPanel;       // 卡牌顯示區
    private JPanel controlPanel;    // 角色控制按鈕區
    private JPanel enemyControlPanel; // 敵方角色控制區
    private JTextArea infoArea;     // 角色資訊區
    private JTextArea enemyInfoArea; // 敵方角色資訊區
    private LinkedList<Warrior> warriors; // 所有遊戲角色
    private Warrior selectedCharacter;
    private GameState gameState = GameState.START;
    private turn GameTurn = turn.SECOND;
    private Team playerTeam = Team.RED;

    public GamePanel(JFrame window) {
        initializePanels(window);
        initializeGame();
        configureMouseEvents();
    }

    private void initializePanels(JFrame window) {
        // 左上部分 (遊戲畫面)
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGameObjects(g);
            }
        };
        gamePanel.setLayout(new BorderLayout());
        gamePanel.add(new JLabel("點擊角色以顯示控制選項和資訊。"), BorderLayout.PAGE_START);

        // 左下部分 (卡牌顯示區)
        cardPanel = new JPanel();

        // 右下部分 (控制按鈕和角色資訊區域)
        controlPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        infoArea = new JTextArea();

        // 右中部分 (控制按鈕和敵方角色資訊區域)
        enemyControlPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        enemyInfoArea = new JTextArea();

        configureSplitPanes(window);
    }

    private void configureSplitPanes(JFrame window) {
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gamePanel, cardPanel);
        verticalSplit.setDividerLocation(4 * window.getHeight() / 5);
        verticalSplit.setResizeWeight(0.8);
        verticalSplit.setEnabled(false);

        JPanel upperRightPanel = new JPanel();
        upperRightPanel.setBackground(Color.LIGHT_GRAY);
        upperRightPanel.add(new JLabel("右上區域"));

        JPanel midRightPanel = new JPanel(new BorderLayout());
        midRightPanel.setBackground(Color.DARK_GRAY);
        midRightPanel.add(enemyControlPanel, BorderLayout.CENTER);
        midRightPanel.add(new JScrollPane(enemyInfoArea), BorderLayout.SOUTH);

        JPanel lowerRightPanel = new JPanel(new BorderLayout());
        lowerRightPanel.setBackground(Color.DARK_GRAY);
        lowerRightPanel.add(controlPanel, BorderLayout.CENTER);
        lowerRightPanel.add(new JScrollPane(infoArea), BorderLayout.SOUTH);

        JSplitPane upperMiddleSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperRightPanel, midRightPanel);
        upperMiddleSplit.setDividerLocation(window.getHeight() / 3);
        upperMiddleSplit.setResizeWeight(0.5);
        upperMiddleSplit.setEnabled(false);

        JSplitPane verticalRightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperMiddleSplit, lowerRightPanel);
        verticalRightSplit.setDividerLocation(2 * window.getHeight() / 3);
        verticalRightSplit.setResizeWeight(0.67);
        verticalRightSplit.setEnabled(false);

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplit, verticalRightSplit);
        horizontalSplit.setDividerLocation(2 * window.getWidth() / 3);
        horizontalSplit.setEnabled(true);

        this.setLayout(new BorderLayout());
        this.add(horizontalSplit, BorderLayout.CENTER);
    }

    private void initializeGame() {
        warriors = new LinkedList<>();

        Warrior testWarrior = new Warrior(100, 1, 50, null, Team.BLUE);
        testWarrior.setName("Fighter");
        testWarrior.addSkill(new Skill("Skill 1", 5, 50));
        testWarrior.addSkill(new Skill("Skill 2", 5, 70));

        Warrior testArcher = new Warrior(100, 1, 50, null, Team.RED);
        testArcher.setName("Archer");
        testArcher.addSkill(new Skill("Skill 1", 5, 50));
        testArcher.addSkill(new Skill("Skill 2", 5, 70));

        warriors.add(testWarrior);
        warriors.add(testArcher);

        updateCardPanel();
    }

    private void configureMouseEvents() {
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getPoint());
            }
        });
    }

    private void handleMouseClick(Point click_pos) {
        System.out.println("GameState: " + gameState);
        System.out.println("Click Position: " + click_pos);

        // Implement your game logic for handling mouse clicks
        switch (GameTurn) {
            case FIRST:
                if(playerTeam==Team.BLUE){
                    switch (gameState) {
                        case CALL:
                            selectedCharacter.Body = new Rectangle(click_pos.x, click_pos.y, selectedCharacter.size.x, selectedCharacter.size.y);
                            gamePanel.repaint();
                            gameState=GameState.START;
                            System.out.println("Select : " + gameState);
                            return;                
                        case FIGHT:
                            switch (selectedCharacter.state) {
                                //判斷移動
                                case MOVE:
                                    if(selectedCharacter.rangeHitBox.contains(click_pos)){
                                        selectedCharacter.Move(click_pos);
                                        gamePanel.repaint();
                                        return;
                                    }
                                    selectedCharacter.selectControl=false;
                                    selectedCharacter.state=State.NULL;
                                    gameState=GameState.START;
                                    gamePanel.repaint();
                                    return;
                                //判斷攻擊                 
                                case ATTACK:
                                    for (Warrior war : warriors) {                                       
                                        //判斷是否點擊士兵
                                        if (war.Body!=null && selectedCharacter.rangeHitBox.contains(click_pos) && war.Body.contains(click_pos) && selectedCharacter.team!=war.team) {
                                            System.out.println("success attack : " + war.getName());
                                            if(selectedCharacter.team==Team.BLUE){
                                                selectedCharacter.Attack(war);
                                                gamePanel.repaint();
                                                gameState=GameState.START;
                                                return;
                                            }
                                        }
                                        selectedCharacter.state=State.MOVE;
                                        gamePanel.repaint();
                                    }         
                                    return;
                                case NULL:
                                    for (Warrior war : warriors) {                                       
                                        //判斷是否點擊士兵
                                        if (war.Body!=null &&war.Body.contains(click_pos)) {
                                            selectedCharacter=war;
                                            updateControlPanel(selectedCharacter);
                                            selectedCharacter.state=State.MOVE;
                                            selectedCharacter.selectControl=true;
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
                                    selectedCharacter=war;
                                    updateControlPanel(selectedCharacter);;
                                    if(selectedCharacter.team==Team.BLUE){
                                        gameState=GameState.FIGHT;
                                        selectedCharacter.state=State.MOVE;
                                        selectedCharacter.selectControl=true;
                                        System.out.println("Select : " + war.getName());
                                        gamePanel.repaint();
                                        return;
                                    }
                                    else{
                                        clearEnemyControlPanel();
                                        updateEnemyControlPanel(selectedCharacter);
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
                            selectedCharacter=war;
                            updateControlPanel(selectedCharacter);
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
                            selectedCharacter.Body = new Rectangle(click_pos.x, click_pos.y, selectedCharacter.size.x, selectedCharacter.size.y);
                            gamePanel.repaint();
                            gameState=GameState.START;
                            System.out.println("Select : " + gameState);
                            return;                
                        case FIGHT:
                            switch (selectedCharacter.state) {
                                //判斷移動
                                case MOVE:
                                    if(selectedCharacter.rangeHitBox.contains(click_pos)){
                                        selectedCharacter.Move(click_pos);
                                        gamePanel.repaint();
                                        return;
                                    }
                                    selectedCharacter.selectControl=false;
                                    selectedCharacter.state=State.NULL;
                                    gameState=GameState.START;
                                    gamePanel.repaint();
                                    return;
                                //判斷攻擊                 
                                case ATTACK:
                                    for (Warrior war : warriors) {                                       
                                        //判斷是否點擊士兵
                                        if (war.Body!=null && selectedCharacter.rangeHitBox.contains(click_pos) && war.Body.contains(click_pos) && selectedCharacter.team!=war.team) {
                                            System.out.println("success attack : " + war.getName());
                                            if(selectedCharacter.team==Team.RED){
                                                selectedCharacter.Attack(war);
                                                updateEnemyControlPanel(war);
                                                gamePanel.repaint();
                                                gameState=GameState.START;
                                                return;
                                            }
                                        }
                                        selectedCharacter.state=State.MOVE;
                                        gamePanel.repaint();
                                    }         
                                    return;
                                case NULL:
                                    for (Warrior war : warriors) {                                       
                                        //判斷是否點擊士兵
                                        if (war.Body!=null &&war.Body.contains(click_pos)) {
                                            selectedCharacter=war;
                                            updateControlPanel(selectedCharacter);
                                            selectedCharacter.state=State.MOVE;
                                            selectedCharacter.selectControl=true;
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
                                    selectedCharacter=war;
                                    if(selectedCharacter.team==Team.RED){
                                        updateControlPanel(selectedCharacter);
                                        gameState=GameState.FIGHT;
                                        selectedCharacter.state=State.MOVE;
                                        selectedCharacter.selectControl=true;
                                        System.out.println("Select : " + war.getName());
                                        gamePanel.repaint();
                                        return;
                                    }else{
                                        clearEnemyControlPanel();
                                        updateEnemyControlPanel(selectedCharacter);
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
                            selectedCharacter=war;
                            if(selectedCharacter.team==Team.BLUE){
                                updateControlPanel(selectedCharacter);
                                System.out.println("Select : " + war.getName());
                                return;
                            }else{
                                clearEnemyControlPanel();
                                updateEnemyControlPanel(selectedCharacter);
                                return;
                            }
                        }
                    }
                    clearControlPanel();
                    return;
                }
        }
    }

    private void drawGameObjects(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Warrior warrior : warriors) {
            if (warrior.Body != null) {
                warrior.paint(g);
                if (warrior.selectControl) {
                    warrior.paintMoveRange(g2d);
                }
                for(Skill skill:warrior.getSkills()){
                    warrior.paintSkillRange(g2d, skill);                            
                }
            }
        }
    }

    private void updateCardPanel() {
        cardPanel.removeAll();
        for (Warrior warrior : warriors) {
            JButton button = new JButton(warrior.getName());
            button.addActionListener(e -> {
                selectedCharacter = warrior;
                gameState = GameState.CALL;
                System.out.println("Selected: " + gameState);
            });
            cardPanel.add(button);
        }
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void updateControlPanel(Warrior warrior) {
        controlPanel.removeAll();

        for (Skill skill : warrior.getSkills()) {
            JButton button = new JButton(skill.getName());
            button.addActionListener(e -> {
                // Add skill activation logic here
                switch (GameTurn) {
                    case FIRST:
                        if(playerTeam==Team.BLUE && selectedCharacter.team==Team.BLUE){
                            warrior.state=State.ATTACK;
                            warrior.selectSkill=skill;
                            skill.setControl(true);
                            gamePanel.repaint();
                        }
                        break;
                
                    default:
                        if(playerTeam==Team.RED && selectedCharacter.team==Team.RED){
                            warrior.state=State.ATTACK;
                            warrior.selectSkill=skill;
                            skill.setControl(true);
                            gamePanel.repaint();
                        }
                        break;
                }
                System.out.println("Activated skill: " + skill.getName());
            });
            controlPanel.add(button);
        }

        infoArea.setText("[角色資訊]\n");
        infoArea.append("生命值: " + warrior.getHealth() + "\n");
        infoArea.append("移動距離: " + warrior.getMoveRange() + "\n");

        controlPanel.revalidate();
        controlPanel.repaint();
    }

    private void updateEnemyControlPanel(Warrior warrior) {
        enemyControlPanel.removeAll();

        for (Skill skill : warrior.getSkills()) {
            JButton button = new JButton(skill.getName());
            enemyControlPanel.add(button);
        }

        enemyInfoArea.setText("[敵方角色資訊]\n");
        enemyInfoArea.append("生命值: " + warrior.getHealth() + "\n");
        enemyInfoArea.append("移動距離: " + warrior.getMoveRange() + "\n");

        enemyControlPanel.revalidate();
        enemyControlPanel.repaint();
    }

    private void clearControlPanel() {
        controlPanel.removeAll();
        infoArea.setText("");
        controlPanel.revalidate();
        controlPanel.repaint();
    }

    private void clearEnemyControlPanel() {
        enemyControlPanel.removeAll();
        enemyInfoArea.setText("");
        enemyControlPanel.revalidate();
        enemyControlPanel.repaint();
    }
}
