package emergencyResponse;

import java.awt.*;

public class ObjectType extends Thread {
    private Point point;

    public ObjectType(Point point){
        this.point = point;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}
