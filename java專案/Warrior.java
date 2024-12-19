import java.awt.*;
public class Warrior implements Character {
    private int health,range,damage;
    private int moveDistance;
    private Rectangle bounds;

    // 建構子
    public Warrior(int health, int moveDistance) {
        this.health = health;
        this.moveDistance = moveDistance;
        this.bounds = new Rectangle(50, 50, 60, 80); // 預設角色範圍
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


    // 繪製角色外觀
    public void drawAppearance(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height); // 劍士的身體
        g2d.setColor(Color.BLACK);
        g2d.fillOval(bounds.x + 10, bounds.y - 20, 40, 40); // 劍士的頭部
    }

    // 繪製技能範圍
    public void basicrange(int r,String name) {
        this.r=r;
    }

    // 獲取角色的邊界 (用於碰撞或點擊檢測)
    public Rectangle getBounds() {
        return bounds;
    }

    // 設定角色位置
    public void setPosition(int x, int y) {
        this.bounds.setLocation(x, y);
    }
    // 範圍
    public void Skillrange(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(30, 30, 100, 100);
    }
}
