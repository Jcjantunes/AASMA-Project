package emergencyResponse;

import java.awt.*;

public class IntensiveCareSupportAmbulance extends Ambulance {
    public IntensiveCareSupportAmbulance(Point point) {
        super(point);
        this.setColor(Color.red);
        this.setMaxPatients(1);
    }
}
