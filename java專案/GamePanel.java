import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class GamePanel extends JPanel {
    private JPanel gamePanel;       // 主要遊戲畫面
    private JPanel cardPanel;       // 卡牌顯示區
    private JPanel turnPanel;       //回合顯示區
    private JPanel controlPanel;    // 角色控制按鈕區
    private JPanel enemyControlPanel; // 敵方角色控制區
    private JTextArea infoArea;     // 角色資訊區
    private JTextArea enemyInfoArea; // 敵方角色資訊區
    private LinkedList<Warrior> warriors; // 所有遊戲角色
    private Warrior selectedCharacter;  //被選擇角色
    private GameState gameState = GameState.START;  //遊戲狀態
    private Turn GameTurn = Turn.FIRST; //遊戲回合
    private Team playerTeam = Team.BLUE;    //玩家隊伍

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    //遊戲執行
    public GamePanel(JFrame window, String serverAddress, int port) throws IOException, ClassNotFoundException {
        socket = new Socket(serverAddress, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        // 接收伺服器分配的 Team
        playerTeam = (Team) in.readObject();
        System.out.println("Assigned team: " + playerTeam);

        new Thread(this::listenToServer).start();

        initializePanels(window);
        initializeGame();
        configureMouseEvents();
    }
    //戰鬥進行UI
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
        cardPanel = new JPanel(new GridLayout(1, 4, 10, 10));

        //右上部分(回合控制區)
        turnPanel=new JPanel(new GridLayout(3, 1, 10, 10));

        // 右下部分 (控制按鈕和角色資訊區域)
        controlPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        infoArea = new JTextArea();

        // 右中部分 (控制按鈕和敵方角色資訊區域)
        enemyControlPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        enemyInfoArea = new JTextArea();

        configureSplitPanes(window);
    }
    //戰鬥初始UI
    private void configureSplitPanes(JFrame window) {
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gamePanel, cardPanel);
        verticalSplit.setDividerLocation(4 * window.getHeight() / 5);
        verticalSplit.setResizeWeight(0.8);
        verticalSplit.setEnabled(false);


        JPanel midRightPanel = new JPanel(new BorderLayout());
        midRightPanel.setBackground(Color.DARK_GRAY);
        midRightPanel.add(enemyControlPanel, BorderLayout.CENTER);
        midRightPanel.add(new JScrollPane(enemyInfoArea), BorderLayout.SOUTH);

        JPanel lowerRightPanel = new JPanel(new BorderLayout());
        lowerRightPanel.setBackground(Color.DARK_GRAY);
        lowerRightPanel.add(controlPanel, BorderLayout.CENTER);
        lowerRightPanel.add(new JScrollPane(infoArea), BorderLayout.SOUTH);

        JSplitPane upperMiddleSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, turnPanel, midRightPanel);
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
    //初始化遊戲
    private void initializeGame() {
        // warriors = new LinkedList<>();

        // Warrior Fighter = new Warrior(100, 150,  null,playerTeam);
        // Fighter.setName("Fighter");
        // Fighter.addSkill(new Skill("Skill 1", 25, 50));
        // Fighter.addSkill(new Skill("Skill 2", 5, 70));

        // Warrior Archer = new Warrior(50, 80,  null,playerTeam);
        // Archer.setName("Archer");
        // Archer.addSkill(new Skill("Skill 1", 35, 120));
        // Archer.addSkill(new Skill("Skill 2", 600, 150));

        // Warrior Knight = new Warrior(80, 300,  null,playerTeam);
        // Knight.setName("Knigh");
        // Knight.addSkill(new Skill("Skill 1", 40, 30));
        // Knight.addSkill(new Skill("Skill 2", 70, 30));

        // warriors.add(Fighter);
        // warriors.add(Archer);
        // warriors.add(Knight);

        // // 傳送初始角色到伺服器
        // for (Warrior warrior : warriors) {
        //     sendWarriorUpdate(warrior);
        // }

        updateCardPanel();
        updateTurnPanel();
    }
    //偵測滑鼠點擊
    private void configureMouseEvents() {
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getPoint());
            }
        });
    }
    //滑鼠點擊事件
    private void handleMouseClick(Point click_pos) {
        System.out.println("GameState: " + gameState);
        System.out.println("Click Position: " + click_pos);

        //回合一判斷
        switch (GameTurn) {
            case FIRST:
                //玩家隊伍藍隊判斷
                if(playerTeam==Team.BLUE){
                    //選擇角色狀態判斷
                    switch (gameState) {
                        //召喚狀態
                        case CALL:
                            selectedCharacter.Body = new Rectangle(click_pos.x, click_pos.y, selectedCharacter.size.x, selectedCharacter.size.y);
                            gameState=GameState.START;
                            sendWarriorUpdate(selectedCharacter);
                            gamePanel.repaint();
                            System.out.println("召喚角色: " + selectedCharacter.getName() + " at " + click_pos);
                            break;
                        //戰鬥狀態                
                        case FIGHT:
                            switch (selectedCharacter.state) {
                                //判斷移動
                                case MOVE:
                                    //是否在範圍內
                                    if(selectedCharacter.rangeHitBox.contains(click_pos)){
                                        selectedCharacter.Move(click_pos);
                                        sendWarriorUpdate(selectedCharacter);
                                        gamePanel.repaint();
                                        return;
                                    }
                                    selectedCharacter.selectControl=false;
                                    //selectedCharacter.state=State.NULL;
                                    gameState=GameState.START;
                                    gamePanel.repaint();
                                    return;
                                //判斷攻擊                 
                                case ATTACK:
                                    for (Warrior war : warriors) {                                       
                                        //判斷是否點擊士兵、攻擊角色是否為同隊、角色是否為空、是否在範圍內
                                        if (war.Body!=null && selectedCharacter.rangeHitBox.contains(click_pos) && war.Body.contains(click_pos) && selectedCharacter.team!=war.team) {
                                            System.out.println("success attack : " + war.getName());
                                            if(selectedCharacter.team==Team.BLUE){
                                                selectedCharacter.Attack(war);
                                                updateEnemyControlPanel(war);
                                                sendWarriorUpdate(selectedCharacter);
                                                sendWarriorUpdate(war);
                                                gamePanel.repaint();
                                                gameState=GameState.START;
                                                return;
                                            }
                                        }
                                        selectedCharacter.state=State.MOVE;
                                        gamePanel.repaint();
                                    }         
                                    return;
                                //待機狀態
                                case NULL:
                                    for (Warrior war : warriors) {                                       
                                        //判斷是否點擊士兵
                                        if (war.Body!=null &&war.Body.contains(click_pos)) {
                                            selectedCharacter=war;
                                            updateControlPanel(selectedCharacter);
                                            selectedCharacter.state=State.MOVE;
                                            selectedCharacter.selectControl=true;
                                            System.out.println("Select : " + war.getName());
                                            gamePanel.repaint();
                                            return;
                                        }
                                        gameState=GameState.START;
                                        gamePanel.repaint();
                                    }
                                    clearControlPanel();
                                    return;
                            }
                        //初始狀態
                        case START:                            
                            for (Warrior war : warriors) {                                       
                                //判斷是否點擊士兵
                                if (war.Body!=null && war.Body.contains(click_pos)) {
                                    selectedCharacter=war;
                                    //角色為藍隊
                                    if(selectedCharacter.team==Team.BLUE){
                                        updateControlPanel(selectedCharacter);
                                        gameState=GameState.FIGHT;
                                        selectedCharacter.state=State.MOVE;
                                        selectedCharacter.selectControl=true;
                                        System.out.println("Select : " + war.getName());
                                        gamePanel.repaint();
                                        return;
                                    }
                                    //角色為紅隊
                                    else{
                                        updateEnemyControlPanel(selectedCharacter);
                                        return;
                                    }
                                }
                            }
                            clearControlPanel();
                            return;
                    }
                }
                //玩家隊伍紅隊判斷
                else{
                    for (Warrior war : warriors) {                                       
                        //判斷是否點擊士兵
                        if (war.Body!=null && war.Body.contains(click_pos)) {
                            selectedCharacter=war;
                            //角色為紅隊
                            if(selectedCharacter.team==Team.RED){
                                updateControlPanel(selectedCharacter);
                                System.out.println("Select : " + war.getName());
                                return;
                            }
                            //角色為藍隊
                            else{
                                updateEnemyControlPanel(selectedCharacter);
                                return;
                            }
                        }                        
                    }
                    clearControlPanel();
                    return;
                }
            //回合二判斷
            case SECOND:
                //玩家隊伍紅隊判斷
                if(playerTeam==Team.RED){
                    switch (gameState) {
                        //召喚狀態
                        case CALL:
                            selectedCharacter.Body = new Rectangle(click_pos.x, click_pos.y, selectedCharacter.size.x, selectedCharacter.size.y);
                            gamePanel.repaint();
                            gameState=GameState.START;
                            sendWarriorUpdate(selectedCharacter);
                            System.out.println("召喚角色: " + selectedCharacter.getName() + " at " + click_pos);
                            break; 
                        //戰鬥狀態               
                        case FIGHT:
                            switch (selectedCharacter.state) {
                                //判斷移動
                                case MOVE:
                                    //是否在範圍內
                                    if(selectedCharacter.rangeHitBox.contains(click_pos)){
                                        selectedCharacter.Move(click_pos);
                                        sendWarriorUpdate(selectedCharacter);
                                        gamePanel.repaint();
                                        return;
                                    }
                                    selectedCharacter.selectControl=false;
                                    //selectedCharacter.state=State.NULL;
                                    gameState=GameState.START;
                                    gamePanel.repaint();
                                    return;
                                //判斷攻擊                 
                                case ATTACK:
                                    for (Warrior war : warriors) {                                       
                                        //判斷是否點擊士兵、攻擊角色是否為同隊、角色是否為空、是否在範圍內
                                        if (war.Body!=null && selectedCharacter.rangeHitBox.contains(click_pos) && war.Body.contains(click_pos) && selectedCharacter.team!=war.team) {
                                            System.out.println("success attack : " + war.getName());
                                            if(selectedCharacter.team==Team.RED){
                                                selectedCharacter.Attack(war);
                                                updateEnemyControlPanel(war);
                                                sendWarriorUpdate(selectedCharacter);
                                                sendWarriorUpdate(war);
                                                gamePanel.repaint();
                                                gameState=GameState.START;
                                                return;
                                            }
                                        }
                                        selectedCharacter.state=State.MOVE;
                                        gamePanel.repaint();
                                    }         
                                    return;
                                //待機
                                case NULL:
                                    for (Warrior war : warriors) {                                       
                                        //判斷是否點擊士兵
                                        if (war.Body!=null &&war.Body.contains(click_pos)) {
                                            selectedCharacter=war;
                                            updateControlPanel(selectedCharacter);
                                            selectedCharacter.state=State.MOVE;
                                            selectedCharacter.selectControl=true;
                                            System.out.println("Select : " + war.getName());
                                            gamePanel.repaint();
                                            return;
                                        }
                                        gameState=GameState.START;
                                        gamePanel.repaint();
                                    }
                                    clearControlPanel();
                                    return;
                            }
                        //準備階段
                        case START:                            
                            for (Warrior war : warriors) {                                       
                                //判斷是否點擊士兵
                                if (war.Body!=null && war.Body.contains(click_pos)) {
                                    selectedCharacter=war;
                                    //角色為紅隊
                                    if(selectedCharacter.team==Team.RED){
                                        updateControlPanel(selectedCharacter);
                                        gameState=GameState.FIGHT;
                                        selectedCharacter.state=State.MOVE;
                                        selectedCharacter.selectControl=true;
                                        System.out.println("Select : " + war.getName());
                                        gamePanel.repaint();
                                        return;
                                    }
                                    //角色為藍隊
                                    else{
                                        updateEnemyControlPanel(selectedCharacter);
                                        return;
                                    }
                                }
                            }
                            clearControlPanel();
                            return;
                    }
                }
                //玩家為藍隊
                else{
                    for (Warrior war : warriors) {                                       
                        //判斷是否點擊士兵
                        if (war.Body!=null && war.Body.contains(click_pos)) {
                            selectedCharacter=war;
                            //角色為藍隊
                            if(selectedCharacter.team==Team.BLUE){
                                updateControlPanel(selectedCharacter);
                                System.out.println("Select : " + war.getName());
                                return;
                            }
                            //角色為紅隊
                            else{
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

    //角色繪製
    private void drawGameObjects(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Warrior warrior : warriors) {
            if (warrior.Body != null && warrior.getHealth() > 0) {
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
    
    //更新卡排版面
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
    
    //更新角色面板
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
                            gameState=GameState.FIGHT;
                            warrior.selectSkill=skill;
                            skill.setControl(true);
                            gamePanel.repaint();
                            System.out.println("Activated skill: " + skill.getName());
                        }
                        break;
                
                    default:
                        if(playerTeam==Team.RED && selectedCharacter.team==Team.RED){
                            warrior.state=State.ATTACK;
                            gameState=GameState.FIGHT;
                            warrior.selectSkill=skill;
                            skill.setControl(true);
                            gamePanel.repaint();
                            System.out.println("Activated skill: " + skill.getName());
                        }
                        break;
                }
            });
            controlPanel.add(button);
        }

        infoArea.setText("[角色資訊]\n");
        infoArea.append("生命值: " + warrior.getHealth() + "\n");
        infoArea.append("移動距離: " + warrior.getMoveRange() + "\n");

        controlPanel.revalidate();
        controlPanel.repaint();
    }
    
    //更新敵方角色面板
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
    
    //清除角色面板
    private void clearControlPanel() {
        controlPanel.removeAll();
        infoArea.setText("");
        controlPanel.revalidate();
        controlPanel.repaint();
    }


    //傳送傳送Warrior資料
    private void sendWarriorUpdate(Warrior warrior) {
        try {
            out.writeObject(warrior);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //從Server接收資料
    private void listenToServer() {
        try {
            while (true) {
                Object received = in.readObject();
                if (received instanceof LinkedList) {
                    @SuppressWarnings("unchecked")
                    LinkedList<Warrior> updatedWarriors = (LinkedList<Warrior>) received;
    
                    // 更新角色列表
                    warriors = updatedWarriors;
                    
                    // 移除本地死亡角色
                    warriors.removeIf(warrior -> warrior.getHealth() <= 0);
    
                    // 重新繪製遊戲畫面
                    repaint();
                } else if (received instanceof Turn) {
                    GameTurn = (Turn) received;
                    updateTurnPanel(); // 確保回合顯示面板更新
                    System.out.println("Current turn: " + GameTurn);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    //回合結束控制
    private void endTurn() {
        System.out.println("Ending turn for " + playerTeam); 
        try {
            out.writeObject("END_TURN");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Warrior warrior : warriors) {
            if(warrior.Body!=null){
                sendWarriorUpdate(warrior);
            }
        }
        turnPanel.removeAll();
        switchTurn();
        updateTurnPanel();
        repaint();
    }
    
    //回合交換控制
    private void switchTurn() {
        if (playerTeam == Team.BLUE) {
            GameTurn = Turn.SECOND;
        } else {
            GameTurn = Turn.FIRST;
        }        
        System.out.println("Now it's " + playerTeam + "'s turn.");
        System.out.println("Now it's " + GameTurn.toString());
    }
    
    //回合結束按鈕
    private void updateTurnPanel() { 
        turnPanel.removeAll();
        turnPanel.add(new JLabel(playerTeam.toString()),BorderLayout.NORTH);
        turnPanel.add(new JLabel(GameTurn.toString()),BorderLayout.CENTER);   
        if ((GameTurn == Turn.FIRST && playerTeam == Team.BLUE) || 
        (GameTurn == Turn.SECOND && playerTeam == Team.RED)) {
        JButton endTurnButton = new JButton("End Turn");
        endTurnButton.addActionListener(e -> endTurn());
        turnPanel.add(endTurnButton, BorderLayout.SOUTH);
    }           
        turnPanel.revalidate();
        turnPanel.repaint();
    }
    
}
