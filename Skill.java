public class Skill {
    private String name;
    private String description;
    private int attack;
    private int range;

    
    public Skill(String name, int attack, int range) {
        this.name = name;
        this.attack = attack;
        this.range = range;
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

    @Override
    public String toString(){
        return String.format("名稱 : %s\n傷害 : %s\n攻擊距離 : %s\n描述 : %s", name, attack, range, description);
    }
}
