package emergencyResponse;

import java.awt.*;

public class PatientTransportAmbulance extends Ambulance{
    public PatientTransportAmbulance(Point point) {
        super(point);
        this.setColor(Color.white);
        this.setMaxPatients(1);
    }
}
