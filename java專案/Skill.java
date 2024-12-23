import java.io.Serializable;

public class Skill implements Serializable{
    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
    private int attack;
    private int range;
    private boolean control;
    private int cooldown; // 冷卻時間（回合數或秒數）
    private int cooldownRemaining; // 剩餘冷卻時間

    
    public Skill(String name, int attack, int range,int cooldown) {
        this.name = name;
        this.attack = attack;
        this.range = range;
        this.cooldown = cooldown;
        this.cooldownRemaining = 0; // 初始冷卻為 0
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public int getAttack() {
        return attack;
    }


    public void setAttack(int attack) {
        this.attack = attack;
    }


    public int getRange() {
        return range;
    }


    public void setRange(int range) {
        this.range = range;
    }

    public boolean getControl() {
        return control;
    }


    public void setControl(boolean control) {
        this.control = control;
    }

    public void startCooldown() {
        cooldownRemaining = cooldown;
    }

    public void reduceCooldown() {
        if (cooldownRemaining > 0) {
            cooldownRemaining--;
        }
    }

    public int getCooldownRemaining() {
        return cooldownRemaining;
    }

    @Override
    public String toString(){
        return String.format("名稱 : %s\n傷害 : %s\n攻擊距離 : %s\n描述 : %s", name, attack, range, description);
    }
}