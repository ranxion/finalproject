import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.LinkedList;

public class Warrior implements Serializable,Cloneable {
    private static final long serialVersionUID = 1L;

    private int health;
    private int moveRange;
    private String name;
    private LinkedList<Skill> Skills;
    private boolean Movecontrol = true, Attackcontrol = true;
    public boolean selectControl = false;
    public Point size = new Point(30, 50);
    public Rectangle Body;
    public Ellipse2D rangeHitBox;
    public transient BufferedImage image; // Not serialized
    public Team team;
    public Skill selectSkill;
    public State state = State.NULL;

    public Warrior(int health, int moveRange, BufferedImage img, Team team) {
        this.health = health;
        this.moveRange = moveRange;
        this.image = img;
        this.team = team;
        this.name = "A New Warrior";
        this.Skills = new LinkedList<>();
    }

    public void addSkill(Skill skill) {
        Skills.add(skill);
    }

    public LinkedList<Skill> getSkills() {
        return Skills;
    }

    public void Move(Point position) {
        if (state == State.MOVE && Movecontrol) {
            updatePosition(position);
            state = State.NULL;
            this.setMovecontrol(false);
        }
    }

    public void Attack(Warrior attacked) {
        if (Attackcontrol) {
            attacked.setHealth(attacked.getHealth() - selectSkill.getAttack());
            state = State.NULL;
            setMovecontrol(false);
            setAttackcontrol(false);
        }
    }

    public int getMoveRange() {
        return moveRange;
    }

    public void setMoveRange(int range) {
        if (range < 0 || range > 10000) {
            return;
        }
        this.moveRange = range;
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

    public boolean getMovecontrol() {
        return Movecontrol;
    }

    public boolean getAttackcontrol() {
        return Attackcontrol;
    }

    public void setMovecontrol(boolean control) {
        this.Movecontrol = control;
    }

    public void setAttackcontrol(boolean control) {
        this.Attackcontrol = control;
    }

    public Point getPosition() {
        return Body != null ? new Point(Body.x, Body.y) : null;
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
            throw new AssertionError(); // 理論上不會發生，因為我們實現了 Cloneable
        }
    }

    public void resetControls() {
        this.Movecontrol = true;
        this.Attackcontrol = true;
        this.state = State.NULL;
        this.selectControl = false;
        this.selectSkill = null;
    }

    

    public boolean isAlly(Team team) {
        return this.team == team;
    }


    public void paint(Graphics g) {
        if (Body != null) {
            // 繪製角色矩形
            g.setColor(team == Team.BLUE ? Color.BLUE : Color.RED);
            g.fillRect(Body.x, Body.y, Body.width, Body.height);
    
            // 繪製角色名稱
            g.setColor(Color.WHITE);
            g.drawString(name, Body.x + 5, Body.y + 20);
        }
    }

    public void paintMoveRange(Graphics2D g2d) {
        if (state == State.MOVE && selectControl && Movecontrol) {
            g2d.setColor(Color.BLUE); // 移動範圍顏色
            int cx = (int) Body.getCenterX();
            int cy = (int) Body.getCenterY();
            rangeHitBox = new Ellipse2D.Double(cx - moveRange, cy - moveRange, 2 * moveRange, 2 * moveRange);
            g2d.draw(rangeHitBox);
        }
    }
    

    public void paintSkillRange(Graphics2D g2d, Skill skill) {
        if (skill.getControl() && Attackcontrol) {
            g2d.setColor(Color.RED); // 攻擊範圍顏色
            int cx = (int) Body.getCenterX();
            int cy = (int) Body.getCenterY();
            int range = skill.getRange();
            rangeHitBox = new Ellipse2D.Double(cx - range, cy - range, 2 * range, 2 * range);
            g2d.draw(rangeHitBox);
        }
        skill.setControl(false); // 繪製完成後重置
    }
    
    public void initializeMoveRange() {
        if (rangeHitBox == null) {
            rangeHitBox = new Ellipse2D.Double(
                Body.getX() - moveRange,
                Body.getY() - moveRange,
                moveRange * 2,
                moveRange * 2
            );
        }
    }

    public void initializeSkillRange(Skill skill) {
        if (selectSkill != null) {
            rangeHitBox = new Ellipse2D.Double(
                Body.getX() - skill.getRange(),
                Body.getY() - skill.getRange(),
                skill.getRange() * 2,
                skill.getRange() * 2
            );
        }
    }

    @Override
    public String toString() {
        return String.format("Name: %s, Health: %d, Team: %s", name, health, team);
    }
}
