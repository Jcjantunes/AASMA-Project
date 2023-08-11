package emergencyResponse;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Emergency extends ObjectType {
    public enum State {successful, unsuccessful, ongoing, free}
    public State state;
    private int numPatients;
    public enum EmergencyType {patientTransportation, basicSupport, intensiveSupport}

    private EmergencyType emergencyType;
    private int severity;
    private double threshold;
    public boolean isDead;

    private boolean helpRequested = false;

    public static int idCount;
    public int id;


    public Emergency(Point point) {
        super(point);

        this.id = idCount++;

        Random r = new Random();
        int number = r.nextInt(6);

        if(number == 0) {
            emergencyType = EmergencyType.intensiveSupport;
            severity = r.nextInt(11-8) + 8;
            numPatients = 1;
            this.threshold = 0.01;
        }
        else if( number <= 2) {
            emergencyType = EmergencyType.basicSupport;
            severity = r.nextInt(8-4) + 4;
            numPatients = 1;
            this.threshold = 0.005;
        }
        else {
            emergencyType = EmergencyType.patientTransportation;
            severity = r.nextInt(4-1) + 1;
            numPatients = 1;
            this.threshold = 0.0;
        }

        isDead = false;
        state = State.ongoing;
    }


    public int getSeverity() {
        return severity;
    }

    public EmergencyType getEmergencyType() {
        return emergencyType;
    }

    public int getNumPatients() {
        return numPatients;
    }

    public static void setIdCount(int idCount) {
        Emergency.idCount = idCount;
    }

    public boolean isHelpRequested() {
        return helpRequested;
    }

    public void setHelpRequested(boolean helpRequested) {
        this.helpRequested = helpRequested;
    }

    public void updateChanceToDie() {

        Random rand = new Random();
        float chanceToDie = rand.nextFloat();

        if (this.emergencyType == EmergencyType.basicSupport && chanceToDie <= this.getSeverity() * threshold && this.state == State.ongoing) {
            this.isDead = true;
            System.out.println("Emergency:" + this.id + " has been unsuccessful");
        } else if (this.emergencyType == EmergencyType.intensiveSupport && chanceToDie <= this.getSeverity() * threshold && this.state == State.ongoing) {
            this.isDead = true;
            System.out.println("Emergency:" + this.id + " has been unsuccessful");
        }
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }
    public void setEmergencyType(EmergencyType emergencyType) {
        this.emergencyType = emergencyType;
    }
}
