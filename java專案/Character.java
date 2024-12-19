import java.awt.*;
public interface Character {
    int getHealth();
    void setHealth(int health);
    int getMoveDistance();
    void setMoveDistance(int distance);
    void skill(int range,int damage);
    void circle(Graphics2D g2d);
    void drawAppearance(Graphics2D g2d);
}