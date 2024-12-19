import java.awt.*;
public class Warrior implements Character {
    private int health;
    private int moveDistance;
    private String skill;
    // 建構子
    public Warrior(int health, int moveDistance,String skill) {
        this.health = health;
        this.moveDistance = moveDistance;
        this.skill=skill;
    }

    // 實作 getHealth
    @Override
    public int getHealth() {
        return health;
    }

    // 實作 setHealth
    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    // 實作 getMoveDistance
    @Override
    public int getMoveDistance() {
        return moveDistance;
    }

    // 實作 setMoveDistance
    @Override
    public void setMoveDistance(int distance) {
        this.moveDistance = distance;
    }
    public String getSkill() {
        return skill;
    }
    // 實作 Skill
    @Override
    public void setSkill(String skillName) {
        this.skill = skillName;
    }
    
    public void drawAppearance(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.fillRect(50, 50, 60, 80); // 劍士的身體
    }

    // 範圍
    public void Skillrange(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(30, 30, 100, 100);
    }
}
