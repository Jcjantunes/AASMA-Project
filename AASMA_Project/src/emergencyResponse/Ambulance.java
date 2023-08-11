package emergencyResponse;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import emergencyResponse.CommunicationEmergencyCenter.AmbulanceAction;

public class Ambulance extends ObjectType {
    private Color color;
    private int direction = 90;
    private int numPatients = 0;
    private Point hospitalPoint;
    public enum State {free, ongoing, arrivedAtEmergency, arrivedAtHospital}
    public State state = State.free;
    private Queue<AmbulanceAction> plan;

    private int maxPatients;

    private Emergency currentEmergency;
    private CommunicationEmergencyCenter center;

    public Ambulance(Point point) {
        super(point);
        hospitalPoint = point;
        this.plan = new LinkedList<>();
    }

    public void decision() {
        if(hasPlan()){
            AmbulanceAction action = plan.remove();
            execute(action);
        } else {
            return;
        }
    }


    public Point forwardPosition() {
        Point newPoint = new Point(this.getPoint().x,this.getPoint().y);
        switch(direction) {
            case 0: newPoint.y++; break;
            case 90: newPoint.x++; break;
            case 180: newPoint.y--; break;
            default: newPoint.x--;
        }
        return newPoint;
    }

    private boolean hasPlan() {
        return !plan.isEmpty();
    }

    public void execute(AmbulanceAction action) {
        switch (action){
            case moveForward: moveForward(); return;
            case pickUpPatients: pickUpPatients(); return;
            case dropPatients: dropPatients(); return;
            case turn: turnRandomly(); return;
            case turnRight: turnRight(); return;
            case turnLeft: turnLeft(); return;
        }
    }

    /********************/
    /*****  sensors *****/
    /********************/

    public Color getColor() {
        return color;
    }
    public int getDirection() {
        return direction;
    }
    public int getNumPatients() {
        return numPatients;
    }
    public Emergency getCurrentEmergency() {
        return currentEmergency;
    }
    public Point getHospitalPoint() {
        return hospitalPoint;
    }
    public boolean outOfBounds(int x, int y) {
        return x<0 || y<0 || x >= World.x || y >= World.y;
    }

    /**********************/
    /**** actuators ****/
    /**********************/

    public void moveForward(){
        if(forwardPosition().x == hospitalPoint.x && forwardPosition().y == hospitalPoint.y) {
            this.state = State.free;
        }

        this.setPoint(forwardPosition());
    }

    public void turnRight() {
        direction = (direction+90)%360;
    }

    public void turnLeft() {
        direction = (direction-90+360)%360;
    }

    public void turnRandomly() {
        Random random = new Random();
        if(random.nextBoolean()) turnLeft();
        else turnRight();
    }

    public void pickUpPatients() {
        this.numPatients = currentEmergency.getNumPatients();
        this.state = State.arrivedAtEmergency;
        this.center.receiveAmbulanceMessage(this);
        World.removeObjectType(forwardPosition());
    }

    public void dropPatients() {
        this.state = State.arrivedAtHospital;
        this.center.receiveAmbulanceMessage(this);
    }

    /**********************/
    /**** communication ****/
    /**********************/
    public void receivePlan(Queue<AmbulanceAction> ambulancePlan, Emergency emergency, CommunicationEmergencyCenter center) {
        plan = ambulancePlan;
        currentEmergency = emergency;
        this.center = center;
    }

    /*******************************/
    /****** auxiliary *****/
    /*******************************/
    public void setColor(Color color) {
        this.color = color;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setNumPatients(int numPatients) {
        this.numPatients = numPatients;
    }

    public void setMaxPatients(int maxPatients) {
        this.maxPatients = maxPatients;
    }

}
