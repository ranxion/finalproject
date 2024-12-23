import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.LinkedList;

public class Warrior implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private int health;
    private int moveRange;
    private String name;
    private LinkedList<Skill> skills;
    private boolean moveControl = true, attackControl = true;
    public boolean selectControl = false;
    public Point size = new Point(30, 50);
    public Rectangle Body;
    public Ellipse2D rangeHitBox;
    public transient BufferedImage image; // 圖片不序列化
    public Team team;
    public Skill selectSkill;
    public State state = State.NULL;
    private type type;

    public Warrior(int health, int moveRange, BufferedImage img, Team team,type type) {
        this.health = health;
        this.moveRange = moveRange;
        this.image = img;
        this.team = team;
        this.name = "新角色";
        this.type=type;
        this.skills = new LinkedList<>();
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    public LinkedList<Skill> getSkills() {
        return skills;
    }

    // 移動方法，增加邊界限制
    public void move(Point position, Rectangle bounds) {
        if (state == State.MOVE && moveControl) {
            int newX = Math.max(bounds.x, Math.min(bounds.x + bounds.width - size.x, position.x));
            int newY = Math.max(bounds.y, Math.min(bounds.y + bounds.height - size.y, position.y));
            updatePosition(new Point(newX, newY));
            state = State.NULL;
            this.setMoveControl(false);
        }
    }

    public void attack(Warrior attacked) {
        if (attackControl && selectSkill != null) {
            attacked.setHealth(attacked.getHealth() - selectSkill.getAttack());
            selectSkill.startCooldown();
            state = State.NULL;
            setMoveControl(false);
            setAttackControl(false);
        }
    }

    public void reduceSkillCooldowns() {
        for (Skill skill : skills) { // 假設角色有一個技能列表 `skills`
            skill.reduceCooldown();
        }
    }

    public int getMoveRange() {
        return moveRange;
    }

    public void setMoveRange(int range) {
        this.moveRange = Math.max(0, Math.min(range, 10000));
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getMoveControl() {
        return moveControl;
    }

    public boolean getAttackControl() {
        return attackControl;
    }

    public void setMoveControl(boolean control) {
        this.moveControl = control;
    }

    public void setAttackControl(boolean control) {
        this.attackControl = control;
    }

    public Point getPosition() {
        return Body != null ? new Point(Body.x, Body.y) : null;
    }

    public type getType() {
        return this.type; // 例如 "Archer" 或 "Knight"
    }

    public void updatePosition(Point position) {
        if (Body == null) {
            Body = new Rectangle(position.x, position.y, size.x, size.y);
        } else {
            Body.setLocation(position);
        }
    }

    @Override
    public Warrior clone() {
        try {
            return (Warrior) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void resetControls() {
        this.moveControl = true;
        this.attackControl = true;
        this.state = State.NULL;
        this.selectControl = false;
        this.selectSkill = null;
    }

    public boolean isAlly(Team team) {
        return this.team == team;
    }


    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
        // 根據角色類型設計外觀
        switch (this.getName()) {
            case "King_Blue":
                g2d.setColor(Color.BLUE);
                g2d.fillOval(Body.x, Body.y, size.x, size.y); // 藍國王
                break;
            case "King_Red":
                g2d.setColor(Color.RED);
                g2d.fillOval(Body.x, Body.y, size.x, size.y); // 紅國王
                break;
            case "Fighter_Blue":
                g2d.setColor(Color.BLUE);
                g2d.fillRect(Body.x, Body.y, size.x, size.y);
                break;
            case "Fighter_Red":
                g2d.setColor(Color.RED);
                g2d.fillOval(Body.x, Body.y, size.x, size.y / 2); 
                break;
            case "Archer_Red":
                g2d.setColor(Color.RED);
                g2d.fillOval(Body.x, Body.y, size.x, size.y / 2); 
                break;
            case "Archer_Blue":
                g2d.setColor(Color.BLUE);
                g2d.fillRect(Body.x, Body.y, size.x, size.y);
                break;
            case "Knight_Red":
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(Body.x, Body.y, size.x, size.y); 
                break;
            case "Knight_Blue":
                g2d.setColor(Color.GRAY);
                g2d.fillRect(Body.x, Body.y, size.x, size.y); 
                break;
        }
    
        // 畫出角色的生命值
        g2d.setColor(Color.BLACK);
        g2d.drawString("HP: " + health, Body.x, Body.y - 5);
    }
    

    public void paintMoveRange(Graphics2D g2d) {
        if (state == State.MOVE && selectControl && moveControl) {
            g2d.setColor(Color.BLUE);
            int cx = (int) Body.getCenterX();
            int cy = (int) Body.getCenterY();
            rangeHitBox = new Ellipse2D.Double(cx - moveRange, cy - moveRange, 2 * moveRange, 2 * moveRange);
            g2d.draw(rangeHitBox);
        }
    }

    public void paintSkillRange(Graphics2D g2d, Skill skill) {
        if (skill != null && skill.getControl() && attackControl) {
            int cx = (int) Body.getCenterX();
            int cy = (int) Body.getCenterY();
            int range = skill.getRange();
            rangeHitBox = new Ellipse2D.Double(cx - range, cy - range, 2 * range, 2 * range);
            g2d.draw(rangeHitBox);
        }

    }
    


    public void initializeMoveRange() {
        rangeHitBox = new Ellipse2D.Double(
            Body.getX() - moveRange,
            Body.getY() - moveRange,
            moveRange * 2,
            moveRange * 2
        );
    }

    public void initializeSkillRange(Skill skill) {
        if (Body != null && skill != null) {
            int cx = (int) Body.getCenterX();
            int cy = (int) Body.getCenterY();
            rangeHitBox = new Ellipse2D.Double(
                cx - skill.getRange(),
                cy - skill.getRange(),
                skill.getRange() * 2,
                skill.getRange() * 2
            );
        }
    }
    

    @Override
    public String toString() {
        return String.format("名稱: %s, 生命值: %d, 隊伍: %s", name, health, team);
    }
}
