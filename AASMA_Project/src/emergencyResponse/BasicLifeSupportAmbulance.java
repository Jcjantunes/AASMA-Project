package emergencyResponse;

import java.awt.*;

public class BasicLifeSupportAmbulance extends Ambulance{

    public BasicLifeSupportAmbulance(Point point) {
        super(point);
        this.setColor(Color.yellow);
        this.setMaxPatients(1);
    }
}
