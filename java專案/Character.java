import java.awt.*;
public interface Character {
    int getHealth();
    void setHealth(int health);
    int getMoveDistance();
    void setMoveDistance(int distance);
    String getSkill();
    void setSkill(String skillName);
    void Skillrange(Graphics2D g2d); // 使用技能，繪製相關效果
    void drawAppearance(Graphics2D g2d); // 繪製角色外觀
}