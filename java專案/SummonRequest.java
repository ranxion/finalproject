import java.io.Serializable;
import java.awt.Point;

public class SummonRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String warriorName;
    private Point position;

    public SummonRequest(String warriorName, Point position) {
        this.warriorName = warriorName;
        this.position = position;
    }

    public String getWarriorName() {
        return warriorName;
    }

    public Point getPosition() {
        return position;
    }
}
