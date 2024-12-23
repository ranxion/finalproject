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
    private LinkedList<Warrior> warriors= new LinkedList<>();; // 所有遊戲角色
    private Warrior selectedCharacter;  //被選擇角色
    private GameState gameState = GameState.START;  //遊戲狀態
    private Turn GameTurn = Turn.FIRST; //遊戲回合
    private Team playerTeam = Team.BLUE;    //玩家隊伍
    private LinkedList<Warrior> cardWarriors = new LinkedList<>(); // 卡片角色
    private LinkedList<Warrior> summonedWarriors = new LinkedList<>(); // 已召喚角色

    private ObjectOutputStream out;
    private ObjectInputStream in;

    //遊戲執行
    public GamePanel(JFrame window, String serverAddress, int port) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(serverAddress, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    
        // 接收伺服器分配的 Team
        playerTeam = (Team) in.readObject();
        System.out.println("Assigned team: " + playerTeam);
    
        // 接收初始化角色資料
        warriors = (LinkedList<Warrior>) in.readObject();
        for (Warrior warrior : warriors) {
            if (warrior.team == playerTeam) {
                cardWarriors.add(warrior); // 初始化卡片角色
            }
        }
    
        new Thread(this::listenToServer).start();
    
        initializePanels(window);
        configureMouseEvents();
        updateCardPanel();
        updateTurnPanel();
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

        // 左下部分 (卡牌顯示區)
        cardPanel = new JPanel(new GridLayout(1, 6, 10, 10));

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
    //偵測滑鼠點擊
    private void configureMouseEvents() {
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getPoint());
            }
        });
    }

    // 滑鼠點擊事件處理
    private void handleMouseClick(Point click_pos) {
        System.out.println("GameState: " + gameState);
        System.out.println("Click Position: " + click_pos);
        if ((GameTurn == Turn.FIRST && playerTeam == Team.BLUE) || (GameTurn == Turn.SECOND && playerTeam == Team.RED)) {
            processClickForTeam(click_pos);
        } else {
            notYourTurnSelect(click_pos);
        }
    }
    
    // 滑鼠點擊遊戲狀態處理
    private void processClickForTeam(Point click_pos) {    
        switch (gameState) {
            case CALL:
                if (selectedCharacter != null) {                  
                    summonedWarriors.add(selectedCharacter);
                    cardWarriors.remove(selectedCharacter);
                    updateCardPanel(); // 更新卡牌面板
                    selectedCharacter.updatePosition(click_pos);
                    warriors.add(selectedCharacter); // 確保添加到渲染集合
                    sendSummonRequest(selectedCharacter.getName(), click_pos);
                    sendWarriorUpdate(selectedCharacter); 
                    gameState = GameState.START;
                    repaint();
                }
                break;
    
            case FIGHT:
                if (selectedCharacter != null) {
                    processFightAction(click_pos);
                }
                break;
    
            case START:
                selectCharacterAt(click_pos);
                break;
        }
    }
    
    // 處理移動或攻擊    
    private void processFightAction(Point click_pos) {

        switch (selectedCharacter.state) {
            case MOVE:
                if (selectedCharacter.rangeHitBox != null && selectedCharacter.rangeHitBox.contains(click_pos)) {
                    selectedCharacter.Move(click_pos);
                    selectedCharacter.rangeHitBox = null;
                    sendWarriorUpdate(selectedCharacter); // 更新伺服器
                    gameState=GameState.START;
                    break;
                }
                selectedCharacter.state=State.NULL;
                selectedCharacter.selectControl=false;
                sendWarriorUpdate(selectedCharacter);
                gameState=GameState.START;
                clearControlPanel();
                repaint();
                break;
            case ATTACK:
                for (Warrior war : warriors) {
                    if (selectedCharacter.rangeHitBox != null && war.Body != null && selectedCharacter.rangeHitBox.contains(click_pos) &&
                        war.Body.contains(click_pos) && !selectedCharacter.isAlly(war.team)) {
                        selectedCharacter.Attack(war);
                        sendWarriorUpdate(selectedCharacter); // 更新伺服器
                        sendWarriorUpdate(war);              // 同步攻擊目標
                        selectedCharacter.selectControl = false;
                        selectedCharacter.rangeHitBox = null;
                        repaint();
                        gameState=GameState.START;
                        break;
                    }
                }
                selectedCharacter.selectSkill.setControl(false);
                selectedCharacter.state=State.MOVE;
                selectedCharacter.rangeHitBox = null;
                repaint();
                break;
            default:
                return;
                
        }
    }
    
    // 你的回合選擇角色
    private void selectCharacterAt(Point click_pos) {
        for (Warrior war : warriors) {
            if (war.Body != null && war.Body.contains(click_pos)) {
                selectedCharacter = war;
                if (selectedCharacter.team == playerTeam) {
                    gameState = GameState.FIGHT;
                    selectedCharacter.state=State.MOVE;
                    selectedCharacter.selectControl=true;
                    System.out.println(selectedCharacter.state);
                    // 初始化移動範圍
                    if (selectedCharacter.rangeHitBox == null) {
                        selectedCharacter.initializeMoveRange();
                    }                    
                    updateControlPanel(selectedCharacter);
                    repaint();
                    break;
                }
                updateEnemyControlPanel(selectedCharacter);
                repaint();
                break;
            }
            clearControlPanel();
        }       

    }

    //不是你的回合選擇角色
    private void notYourTurnSelect(Point click_pos) {
        for (Warrior war : warriors) {
            if (war.Body != null && war.Body.contains(click_pos)) {
                selectedCharacter = war;
                if (selectedCharacter.team == playerTeam) {             
                    updateControlPanel(selectedCharacter);
                } else {
                    updateEnemyControlPanel(selectedCharacter);
                }
                repaint();
            }
        }
    }

    //角色繪製
    private void drawGameObjects(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
        // 清除畫布，避免殘影
        g.clearRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());
    
        for (Warrior warrior : warriors) {
            if (warrior.Body != null && warrior.getHealth() > 0) {
                // 繪製角色
                warrior.paint(g);
    
                // 當前回合才顯示移動範圍和技能範圍
                if (warrior.team == playerTeam && warrior == selectedCharacter) {
                    if (warrior.state == State.MOVE && warrior.selectControl) {
                        warrior.paintMoveRange(g2d);
                    }
                    if (warrior.state == State.ATTACK && warrior.selectSkill != null) {
                        warrior.paintSkillRange(g2d, warrior.selectSkill);
                    }
                }
            }
        }
    }
    
    
    //更新卡排版面
    private void updateCardPanel() {
        cardPanel.removeAll();
        for (Warrior warrior : cardWarriors) {
            JButton button = new JButton(warrior.getName());
            button.addActionListener(e -> {
                selectedCharacter = warrior;
                gameState = GameState.CALL;
                System.out.println("GameState: " + gameState);
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
                selectedCharacter=warrior;
                // Add skill activation logic here
                switch (GameTurn) {
                    case FIRST:
                        if(playerTeam==Team.BLUE && selectedCharacter.team==Team.BLUE){
                            selectedCharacter.state=State.ATTACK;
                            gameState=GameState.FIGHT;
                            selectedCharacter.selectSkill=skill;
                            skill.setControl(true);
                            //selectedCharacter.initializeSkillRange(skill);
                            repaint();
                            System.out.println("Activated skill: " + skill.getName());
                        }
                        break;
                
                    default:
                        if(playerTeam==Team.RED && selectedCharacter.team==Team.RED){
                            selectedCharacter.state=State.ATTACK;
                            gameState=GameState.FIGHT;
                            selectedCharacter.selectSkill=skill;
                            skill.setControl(true);
                            //selectedCharacter.initializeSkillRange(skill);
                            repaint();
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
    
    //傳送召喚資訊
    private void sendSummonRequest(String warriorName, Point position) {
        try {
            SummonRequest summonRequest = new SummonRequest(warriorName, position);
            out.writeObject(summonRequest);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //傳送傳送Warrior資料
    private void sendWarriorUpdate(Warrior warrior) {
        try {
            out.writeObject(warrior);  // 傳送完整角色物件
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
    
                    warriors.clear(); // 清空當前角色
                    warriors.addAll(updatedWarriors); // 添加伺服器發送的最新角色數據
    
                    repaint(); // 重繪遊戲畫面
                } else if (received instanceof Turn) {
                    GameTurn = (Turn) received;
                    updateTurnPanel(); // 更新回合顯示
                    System.out.println("Current turn: " + GameTurn);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    
    //回合結束控制
    private void endTurn() {
        try {
            out.writeObject("END_TURN");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        // 使用副本來迭代，避免 ConcurrentModificationException
        LinkedList<Warrior> warriorsCopy = new LinkedList<>(warriors);
        for (Warrior warrior : warriorsCopy) {
            warrior.resetControls();
            warrior.selectControl = false;
            warrior.rangeHitBox = null;
    
            // 傳送更新的戰士數據
            sendWarriorUpdate(warrior);
        }
    
        // 更新本地畫面
        repaint();
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