package emergencyResponse;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Hospital extends ObjectType{

    private ArrayList<Ambulance> ambulances = new ArrayList<Ambulance>();

    private int currCapacity = 0;
    private int expectedCapacity = currCapacity;

    private int centerId;

    private int maxCapacity; // = r.nextInt(100-50) + 50;

    public Hospital(Point point) {
        super(point);
    }

    public int getCurrCapacity() {
        return this.currCapacity;
    }

    public int getExpectedCapacity() {
        return this.expectedCapacity;
    }

    public void updateCurrCapacity(int capacity) {
        this.currCapacity += capacity;
    }

    public void updateExpectedCapacity(int capacity) {
        this.expectedCapacity += capacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public ArrayList<Ambulance> getAmbulances() {
        return this.ambulances;
    }


    public int getCenterId() {
        return centerId;
    }
    public void setCenterId(int centerId) {
        this.centerId = centerId;
    }
    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void addAmbulance(Ambulance ambulance)
    {
        this.ambulances.add(ambulance);
    }
}
