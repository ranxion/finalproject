import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class Warrior {
    private int health;
    private int moveRange;
    private int basicAttack;
    private String name;
    private LinkedList<Skill> Skills;

    public Rectangle Body;
    public BufferedImage image;

    public Warrior(int health, int attack, int moveRange, Point size, Point position, BufferedImage img) {
        this.health = health;
        this.basicAttack = attack;
        this.moveRange = moveRange;
        this.Body = new Rectangle(position.x, position.y, size.x, size.y);
        this.image = img;

        name = "A New Warrior";
        Skills = new LinkedList<>();
    }

    public void addSkill(Skill skill) {
        Skills.add(skill);
    }

    public LinkedList<Skill> getSkills() {
        return Skills;
    }

    public void Move(int distance) {

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

    public void paint(Graphics g) {
        // 繪製本體
        if (image != null) {
            g.drawImage(image, Body.x, Body.y, Body.width, Body.height, null);
        } else {
            g.setColor(Color.RED);
            g.drawRect(Body.x, Body.y, Body.width, Body.height);
        }

        // 繪製移動範圍
        g.setColor(Color.BLACK);
        int cx = (int) Body.getCenterX();
        int cy = (int) Body.getCenterY();
        g.drawOval(cx - moveRange, cy - moveRange, 2* moveRange, 2 * moveRange);
    }
    
    @Override
    public String toString() {
        return "";
    }
}
