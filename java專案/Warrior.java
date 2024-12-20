import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class Warrior {
    private int health;
    private int moveRange;
    private int basicAttack; 
    private String name;
    private LinkedList<Skill> Skills;
    private boolean control=false;
    public Point size=new Point(30,50);
    public Rectangle Body;
    public Ellipse2D rangeHitBox; 
    public BufferedImage image;
    private Team team;
    public Skill selectSkill;
    public State state=State.NULL;

    public Warrior(int health, int attack, int moveRange, BufferedImage img,Team team) {
        this.health = health;
        this.basicAttack = attack;
        this.moveRange = moveRange;
        //this.Body = new Rectangle(position.x, position.y, size.x, size.y);
        this.image = img;
        this.team=team;
        name = "A New Warrior";
        Skills = new LinkedList<>();
    }

    public void addSkill(Skill skill) {
        Skills.add(skill);
    }

    public LinkedList<Skill> getSkills() {
        return Skills;
    }

    public void Move(Point position) {
        if(state==State.MOVE){
            Body = new Rectangle(position.x, position.y, size.x, size.y);
            state=State.NULL;
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

    public boolean gercontrol(){
        return control;
    }

    public void setcontrol(boolean control) {
        this.control = control;
    }

    public void paint(Graphics g) {
        // 繪製本體
        if (image != null) {
            g.drawImage(image, Body.x, Body.y, Body.width, Body.height, null);
        } else {
            g.setColor(Color.RED);
            g.drawRect(Body.x, Body.y, Body.width, Body.height);
        }
    }
    
    public void paintMoveRange(Graphics2D g2d){
        // 繪製移動範圍
        if(control){
            g2d.setColor(Color.BLACK);
            int cx = (int) Body.getCenterX();
            int cy = (int) Body.getCenterY();
            rangeHitBox= new Ellipse2D.Double(cx- moveRange, cy- moveRange,2* moveRange, 2 * moveRange);
            g2d.draw(rangeHitBox);            
        }
        this.setcontrol(false);
    }

    public void paintSkillRange(Graphics2D g2d,Skill skill){
        //繪製技能範圍
        if(skill.getControl()){            
            g2d.setColor(Color.red);
            int cx = (int) Body.getCenterX();
            int cy = (int) Body.getCenterY();
            int range=skill.getRange();
            rangeHitBox= new Ellipse2D.Double(cx- range, cy- range,2* range, 2 * range);
            g2d.draw(rangeHitBox);
        }
        skill.setControl(false);
    }

    @Override
    public String toString() {
        return "";
    }
}