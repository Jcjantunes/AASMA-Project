package emergencyResponse;

import java.awt.*;
import java.util.*;
import java.util.List;

import static emergencyResponse.Ambulance.State.*;

public class CommunicationEmergencyCenter extends ObjectType {

    public enum Desire {dispatchVehicle, wait, requestHelp, dispatchToHospital, returnToOriginalHospital}
    public enum Action {dispatchVehicle,dispatchToHospital,returnToOriginalHospital, wait, requestHelp}
    public enum AmbulanceAction {pickUpPatients, dropPatients,
        moveForward, turn, turnRight, turnLeft}

    public static int idCount;
    public int id;

    public String color;

    private float deadCount = 0;
    private float sucessfullCount = 0;

    //beliefs
    public Map<Point,ObjectType> world; //internal map of the world
    public ArrayList<Hospital> hospitals;
    public ArrayList<Ambulance> ambulances;
    public ArrayList<Emergency> emergencies;
    public ArrayList<Ambulance> ambulancesArrivedAtEmergency;
    public ArrayList<Ambulance> ambulancesArrivedAtHospital;

    public ArrayList<Desire> desires;
    public AbstractMap.SimpleEntry<Desire,Point> intention;

    public Queue<AmbulanceAction> ambulancePlan;
    public Queue<Action> plan;
    public Queue<Ambulance> ambulanceQueue;
    public Queue<Emergency> emergencyQueue;
    public boolean noAvailableVehicle = false;
    public boolean noAvailableHospital = false;

    public Emergency helpRequestEmergency = null;
    private Ambulance helpRequestAmbulance = null;

    public CommunicationEmergencyCenter(Point point) {
        super(point);
        this.hospitals = new ArrayList<>();
        this.emergencies = new ArrayList<>();
        this.ambulances = new ArrayList<>();
        this.ambulancesArrivedAtEmergency = new ArrayList<>();
        this.ambulancesArrivedAtHospital = new ArrayList<>();
        this.world = new HashMap<>();
        this.id = idCount++;
        this.ambulancePlan = new LinkedList<>();
        this.plan = new LinkedList<>();
        this.ambulanceQueue = new LinkedList<>();

    }

    public void agentDecision() {
        updateBeliefs();

        if(hasPlan() && hasAmbulancePlan() && !impossibleIntention()){
            Action action = plan.remove();
            System.out.println(this.color +  " Center" + " " + action.toString());
            Ambulance amb = ambulanceQueue.element();
            Emergency emergency = emergencyQueue.element();
            execute(action,amb, emergency);

        }else if(hasPlan() && !impossibleIntention()) {
            Action action = plan.remove();
            System.out.println(this.color +  " Center" + " " + action.toString());
            execute(action,null, null);
        }
        else {
            deliberate();
            buildPlan();

        }
    }

    private void updateBeliefs() {
        if(hasEmergencies()) {
            Collections.sort(this.emergencies, new SortBySeverity());
            Double distance = euclideanDistance(getEmergencyPoint(this.emergencies.get(0)), getPoint());
            if(!isHelpRequested(this.emergencies.get(0))) {
                World.sendDistance(distance, this.emergencies.get(0), getEmergencyId(this.emergencies.get(0)), getCenterId());
            }
        }
    }

    private void deliberate() {
        desires = new ArrayList<Desire>();
        Collections.sort(this.emergencies, new SortBySeverity());
        boolean flag = true;

        if(hasNoAvailableVehicle()) {
            desires.add(Desire.requestHelp);
        }

        if(hasAmbulancesArrivedAtEmergency()){
            desires.add(Desire.dispatchToHospital);
        }

        if(hasEmergencies()) {
            if(!isHelpRequested(this.emergencies.get(0))) {
                for (int i = 0; i < getWorldCentersNumber(); i++) {
                    if (getWorldDistances()[getEmergencyId(this.emergencies.get(0)) - 1][i] == null) {
                        desires.add(Desire.wait);
                        flag = false;
                        break;
                    }
                }
            }

            if(flag) {
                desires.add(Desire.dispatchVehicle);
            }
        }

        if(hasAmbulancesArrivedAtHospital()){
            desires.add(Desire.returnToOriginalHospital);
        }

        if(desires.size() != 0) {
            intention = new AbstractMap.SimpleEntry<>(desires.get(0), null);
            switch (intention.getKey()) {
                case dispatchToHospital:
                    Collections.sort(this.ambulancesArrivedAtEmergency, new SortBySeverityAmbulance());
                    Ambulance amb = ambulancesArrivedAtEmergency.get(0);
                    Point selectedHospitalPoint = selectHospital(amb);
                    intention.setValue(selectedHospitalPoint);
                    if(selectedHospitalPoint == null){
                        noAvailableHospital = true;
                    }
                    break;
                case wait:
                case requestHelp:
                    break;
                case dispatchVehicle:
                    Collections.sort(this.emergencies, new SortBySeverity());
                    intention.setValue(getEmergencyPoint(this.emergencies.get(0)));
                    this.emergencies.remove(0);
                    break;
                case returnToOriginalHospital:
                    Ambulance returningAmb = this.ambulancesArrivedAtHospital.get(0);
                    intention.setValue(getAmbulanceHospitalPoint(returningAmb));
                    break;


            }
        }
        else{
            intention = null;
        }
    }

    private void buildPlan() {

        plan = new LinkedList<Action>();
        ambulancePlan = new LinkedList<AmbulanceAction>();
        ambulanceQueue = new LinkedList<Ambulance>();
        emergencyQueue = new LinkedList<>();

        if(intention == null) {
            return;
        }

        switch (intention.getKey()) {
            case dispatchToHospital:
                if(!hasNoAvailableHospital()) {
                    plan.add(Action.dispatchToHospital);
                    Ambulance ambAtEmergency = ambulancesArrivedAtEmergency.get(0);
                    ambulancePlan = buildPathPlan(getAmbulancePoint(ambAtEmergency), intention.getValue(), ambAtEmergency);
                    ambulanceQueue.add(ambAtEmergency);
                    emergencyQueue.add(getAmbulanceCurrentEmergency(ambAtEmergency));
                    ambulancePlan.add(AmbulanceAction.dropPatients);
                    ambulancesArrivedAtEmergency.remove(0);
                }
                break;
            case wait:
                plan.add(Action.wait);
                break;
            case dispatchVehicle:
                String amb = getNeededAmbulanceType((Emergency) world.get(intention.getValue()));
                Ambulance ambulance = selectAmbulance((Emergency) world.get(intention.getValue()), amb);
                if(ambulance != null) {
                    plan.add(Action.dispatchVehicle);
                    world.put(getAmbulancePoint(ambulance), ambulance);
                    ambulancePlan = buildPathPlan(getAmbulancePoint(ambulance), intention.getValue(), ambulance);
                    ambulanceQueue.add(ambulance);
                    emergencyQueue.add((Emergency) world.get(intention.getValue()));
                    ambulancePlan.add(AmbulanceAction.pickUpPatients);
                }
                else if(hasReservedHelpRequestAmbulance()) {
                    plan.add(Action.dispatchVehicle);
                    world.put(getAmbulancePoint(helpRequestAmbulance), helpRequestAmbulance);
                    ambulancePlan = buildPathPlan(getAmbulancePoint(helpRequestAmbulance), intention.getValue(), helpRequestAmbulance);
                    Ambulance amb2 = helpRequestAmbulance;
                    ambulanceQueue.add(amb2);
                    emergencyQueue.add((Emergency) world.get(intention.getValue()));
                    ambulancePlan.add(AmbulanceAction.pickUpPatients);
                    helpRequestAmbulance = null;
                }
                else {
                    helpRequestEmergency = (Emergency) world.get(intention.getValue());
                    noAvailableVehicle = true;
                }
                break;
            case requestHelp:
                plan.add(Action.requestHelp);
                break;
            case returnToOriginalHospital:
                plan.add(Action.returnToOriginalHospital);
                Ambulance ambAtHospital = ambulancesArrivedAtHospital.get(0);
                ambulancePlan = buildPathPlan(getAmbulancePoint(ambAtHospital), intention.getValue(), ambAtHospital);
                ambulancePlan.add(AmbulanceAction.moveForward);
                ambulanceQueue.add(ambAtHospital);
                emergencyQueue.add(getAmbulanceCurrentEmergency(ambAtHospital));
                ambulancesArrivedAtHospital.remove(0);
                break;
        }
    }

    private void execute(Action action, Ambulance amb, Emergency emergency) {
        switch (action){
            case wait:
                break;
            case returnToOriginalHospital:
            case dispatchToHospital:
            case dispatchVehicle:
                sendAmbulancePlan(amb,emergency);
                break;
            case requestHelp:
                System.out.println(this.color +  " Center" + " has no available ambulance and has requested help");
                helpRequestEmergency.setHelpRequested(true);
                Double[] centerDistances = World.requestHelp(helpRequestEmergency, getCenterId());
                Double minDistance = Double.POSITIVE_INFINITY;
                int selectedCenterId = -1;

                for(int i = 0; i < getWorldCentersNumber(); i++) {
                    if(i != getCenterId() - 1 && centerDistances[i] != null) {
                        if (centerDistances[i] < minDistance) {
                            minDistance = centerDistances[i];
                            selectedCenterId = i;
                        }
                    }
                }

                if(selectedCenterId != -1) {
                    World.sendEmergencyToCenter(helpRequestEmergency, selectedCenterId);
                    for(int i = 0; i < getWorldCentersNumber(); i++) {
                        if(i != getCenterId() - 1 && centerDistances[i] != null) {
                            if (selectedCenterId != i) {
                                helpRequestAmbulance = null;
                            }
                        }
                    }
                    helpRequestEmergency = null;
                    noAvailableVehicle = false;
                }
                if(selectedCenterId == -1) {
                    helpRequestEmergency.setHelpRequested(false);
                }
                break;
        }
    }

    private boolean impossibleIntention() {
        if(intention.getKey().equals(Desire.dispatchToHospital) && intention.getValue() == null)
            return true;
        return false;
    }

    class SortBySeverity implements Comparator<Emergency> {
        // Used for sorting in descending order of emergencies
        public int compare(Emergency a, Emergency b)
        {
            return getEmergencySeverity(b) - getEmergencySeverity(a);
        }
    }

    class SortBySeverityAmbulance implements Comparator<Ambulance> {
        public int compare(Ambulance a, Ambulance b) {
            return getAmbulanceCurrentEmergency(a).getSeverity() - getAmbulanceCurrentEmergency(b).getSeverity();

        }
    }


    /********************/
    /*****  sensors *****/
    /********************/
    //CENTER
    private int getCenterId() {
        return this.id;
    }

    private boolean hasAmbulancesArrivedAtHospital() {
        return ambulancesArrivedAtHospital.size() != 0;
    }

    private boolean hasAmbulancesArrivedAtEmergency() {
        return ambulancesArrivedAtEmergency.size() != 0;
    }

    private boolean hasReservedHelpRequestAmbulance() {
        return helpRequestAmbulance != null;
    }

    private boolean hasNoAvailableVehicle() {
        return noAvailableVehicle;
    }

    private boolean hasNoAvailableHospital() {
        return noAvailableHospital;
    }

    //WORLD
    private int getWorldCentersNumber() {
        return World.getCentersNumber();
    }

    private Double[][] getWorldDistances() {
        return World.getDistances();
    }

    //EMERGENCY
    public void detectEmergency(Emergency emergency){
        this.emergencies.add(emergency);
        world.put(getEmergencyPoint(emergency), emergency);
    }

    public boolean hasEmergencies() {
        return this.emergencies.size() != 0;
    }

    public boolean isHelpRequested(Emergency emergency) {
        return emergency.isHelpRequested();
    }

    public Emergency.EmergencyType getEmergencyType(Emergency emergency) {
        return emergency.getEmergencyType();
    }

    public int getEmergencySeverity(Emergency emergency) {
        return emergency.getSeverity();
    }

    public Point getEmergencyPoint(Emergency emergency) {
        return emergency.getPoint();
    }

    public int getEmergencyId(Emergency emergency) {
        return emergency.id;
    }

    //AMBULANCE
    public int getAmbulanceDirection(Ambulance amb) {
        return amb.getDirection();
    }

    public int getAmbulanceNumPatients(Ambulance ambulance) {
        return ambulance.getNumPatients();
    }

    public Point getAmbulancePoint(Ambulance ambulance) {
        return ambulance.getPoint();
    }

    public Point getAmbulanceHospitalPoint(Ambulance ambulance) {
        return ambulance.getHospitalPoint();
    }

    public Emergency getAmbulanceCurrentEmergency(Ambulance ambulance) {
        return ambulance.getCurrentEmergency();
    }

    private Ambulance.State getAmbulanceState(Ambulance ambulance) {
        return ambulance.state;
    }

    private Point getAmbulanceForwardPosition(Ambulance ambulance) {
        return ambulance.forwardPosition();
    }

    //HOSPITAL
    public ArrayList<Ambulance> getHospitalAmbulances(Hospital hospital) {
        return hospital.getAmbulances();
    }

    public int getHospitalMaxCapacity(Hospital hospital) {
        return hospital.getMaxCapacity();
    }

    private int getHospitalExpectedCapacity(Hospital hospital) {
        return hospital.getExpectedCapacity();
    }

    public Point getHospitalPoint(Hospital hospital) {
        return hospital.getPoint();
    }

    /**********************/
    /**** actuators ****/
    /**********************/

    public Ambulance selectAmbulance(Emergency emergency, String ambulanceName) {
        Ambulance selectedAmbulance = null;
        Double minDistance = Double.POSITIVE_INFINITY;

        for(Ambulance ambulance: this.ambulances) {
            if(ambulance.getClass().getName().equals(ambulanceName)
                    && getAmbulanceNumPatients(ambulance) == 0 && getAmbulanceState(ambulance) == Ambulance.State.free) {
                //&& ambulance.maxPatients <= emergency.numPatients) {
                Double distance = euclideanDistance(getEmergencyPoint(emergency), getAmbulancePoint(ambulance));
                if(distance < minDistance) {
                    minDistance = distance;
                    selectedAmbulance = ambulance;
                    ambulance.state = Ambulance.State.ongoing;
                }
            }
        }

        return selectedAmbulance;
    }

    public Ambulance selectAmbulance2(Emergency emergency, String ambulanceName) {
        Ambulance selectedAmbulance = null;
        Double minDistance = Double.POSITIVE_INFINITY;

        for(Ambulance ambulance: this.ambulances) {
            if(ambulance.getClass().getName().equals(ambulanceName)
                    && getAmbulanceNumPatients(ambulance) == 0 && getAmbulanceState(ambulance) == Ambulance.State.free) {
                //&& ambulance.maxPatients <= emergency.numPatients) {
                Double distance = euclideanDistance(getEmergencyPoint(emergency), getAmbulancePoint(ambulance));
                if(distance < minDistance) {
                    minDistance = distance;
                    selectedAmbulance = ambulance;
                    helpRequestAmbulance = ambulance;
                }
            }
        }

        return selectedAmbulance;
    }

    public Point selectHospital(Ambulance amb) {
        Point selectedHospitalPoint = null;
        Double minDistance = Double.POSITIVE_INFINITY;
        Point closestHospitalsOutOfArea[] = null;
        Hospital selectedHospital = null;

        for(Hospital hospital: this.hospitals) {
            if(getHospitalExpectedCapacity(hospital) + getAmbulanceNumPatients(amb) <= getHospitalMaxCapacity(hospital)) {
                Double distance = euclideanDistance(getHospitalPoint(hospital), getAmbulancePoint(amb));
                if(distance < minDistance) {
                    minDistance = distance;
                    selectedHospitalPoint = getHospitalPoint(hospital);
                    selectedHospital = hospital;
                }
            }
        }
        if(selectedHospitalPoint == null || isHelpRequested(getAmbulanceCurrentEmergency(amb))) {
            System.out.println(this.color +  " Center" + " has no available hospital and has requested help");
            closestHospitalsOutOfArea = World.sendHospitalRequestMessage(getAmbulancePoint(amb),getAmbulanceNumPatients(amb));
            selectedHospitalPoint = selectHospitalOutOfArea(closestHospitalsOutOfArea,getAmbulancePoint(amb));
            if(selectedHospitalPoint != null) {
                selectedHospital = (Hospital) World.getObjectType(selectedHospitalPoint);
            }
        }

        if(selectedHospitalPoint != null) {
            selectedHospital.updateExpectedCapacity(getAmbulanceNumPatients(amb));
        }
        return selectedHospitalPoint;
    }

    public Point selectHospitalOutOfArea(Point[] hospitalsPoint, Point ambPoint) {
        Point selectedHospitalPoint = null;
        Double minDistance = Double.POSITIVE_INFINITY;

        for(int i = 0; i < getWorldCentersNumber(); i++) {
            if(hospitalsPoint[i] != null) {
                Double distance = euclideanDistance(hospitalsPoint[i], ambPoint);
                if (distance < minDistance) {
                    minDistance = distance;
                    selectedHospitalPoint = hospitalsPoint[i];
                }
            }
        }
        return selectedHospitalPoint;
    }

    /**********************/
    /**** communication ****/
    /**********************/

    public void sendAmbulancePlan(Ambulance amb, Emergency emergency) {
        amb.receivePlan(ambulancePlan, emergency, this);
    }

    public void receiveDistance(Double[][] distances, Emergency emergency, int emergencyId) {
        Double centerDistance = euclideanDistance(getEmergencyPoint(emergency), this.getPoint());

        for(int i = 0; i < getWorldCentersNumber(); i++) {
            Double distance = distances[emergencyId - 1][i];
            if (distance != null && distance < centerDistance) {
                this.emergencies.remove(emergency);
                World.setDistances(emergencyId,id);
            }
        }
    }

    public void receiveNewEmergency(Emergency emergency) {
        helpRequestAmbulance.state = ongoing;
        System.out.println(this.color +  " Center" + " has received the help request and will send an ambulance");
        this.emergencies.add(emergency);
    }

    public Double receiveHelpRequest(Emergency emergency) {
        String amb = getNeededAmbulanceType(emergency);
        Ambulance ambulance = selectAmbulance2(emergency, amb);
        if(ambulance != null) {
            return euclideanDistance(getAmbulancePoint(ambulance), getEmergencyPoint(emergency));
        }

        return null;
    }

    public Point receiveHospitalRequestMessage(Point ambPoint, int ambNumPatients) {
        Hospital selectedHospital = null;
        Double minDistance = Double.POSITIVE_INFINITY;

        for(Hospital hospital: this.hospitals) {
            if(getHospitalExpectedCapacity(hospital) + ambNumPatients <= getHospitalMaxCapacity(hospital)) {
                Double distance = euclideanDistance(getHospitalPoint(hospital), ambPoint);
                if(distance < minDistance) {
                    minDistance = distance;
                    selectedHospital = hospital;
                }
            }
        }
        if(selectedHospital != null){
            System.out.println(this.color +  " Center" + " has received the request and will send a hospital location");
            return getHospitalPoint(selectedHospital);
        }
        return null;

    }

    public void receiveAmbulanceMessage(Ambulance amb) {
        if(getAmbulanceState(amb) == arrivedAtEmergency){
            ambulancesArrivedAtEmergency.add(amb);
        }
        if(getAmbulanceState(amb) == arrivedAtHospital){
            Hospital hosp = (Hospital) World.getObjectType(getAmbulanceForwardPosition(amb));
            Emergency currentEmergency = getAmbulanceCurrentEmergency(amb);
            if(!currentEmergency.isDead){
                hosp.updateCurrCapacity(getAmbulanceNumPatients(amb));
                this.sucessfullCount++;
            }
            else{
                hosp.updateExpectedCapacity(-getAmbulanceNumPatients(amb));
                this.deadCount++;
            }
            amb.setNumPatients(0);
            ambulancesArrivedAtHospital.add(amb);
            System.out.println("vivos:" + this.sucessfullCount + " mortos:" + this.deadCount);
            float percentage = 100*(this.sucessfullCount/(this.sucessfullCount + this.deadCount));
            String str = String.format("%.02f", percentage);
            System.out.println("Percentage of successful emergencies = " + str + " at " + this.color +  " Center");
        }
    }


    /*******************************/
    /****** auxiliary *****/
    /*******************************/

    private boolean hasPlan() {
        return !plan.isEmpty();
    }

    private boolean hasAmbulancePlan() {
        return !ambulancePlan.isEmpty();
    }

    public Double euclideanDistance(Point point1, Point point2) {
        return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
    }

    public void loadHospitalsAndAmbulances(ArrayList<Hospital> worldHospitals) {
        for(Hospital hospital: worldHospitals) {
            this.hospitals.add(hospital);
            this.ambulances.addAll(getHospitalAmbulances(hospital));
            world.put(getHospitalPoint(hospital), hospital);
        }

    }

    public String getNeededAmbulanceType(Emergency emergency) {
        Emergency.EmergencyType emergencyType = getEmergencyType(emergency);
        String neededAmbulance = null;

        switch (emergencyType) {
            case patientTransportation:
                neededAmbulance = "emergencyResponse.PatientTransportAmbulance";
                break;
            case basicSupport:
                neededAmbulance = "emergencyResponse.BasicLifeSupportAmbulance";
                break;
            case intensiveSupport:
                neededAmbulance = "emergencyResponse.IntensiveCareSupportAmbulance";
                break;

        }
        return neededAmbulance;
    }

    /*******************************/
    /****** planning auxiliary *****/
    /*******************************/

    private AmbulanceAction turn(Point p1, Point p2, Ambulance amb) {
        boolean vertical = Math.abs(p1.x-p2.x)<Math.abs(p1.y-p2.y);
        boolean upright = vertical ? p1.y<p2.y : p1.x<p2.x;
        if(vertical) {
            if(upright) { //move up
                if(getAmbulanceDirection(amb) != 0) return getAmbulanceDirection(amb) == 90 ? AmbulanceAction.turnLeft : AmbulanceAction.turnRight;
            } else if(getAmbulanceDirection(amb) != 180) return getAmbulanceDirection(amb) == 90 ? AmbulanceAction.turnRight : AmbulanceAction.turnLeft;
        } else {
            if(upright) { //move right
                if(getAmbulanceDirection(amb) != 90) return getAmbulanceDirection(amb) == 180 ? AmbulanceAction.turnLeft : AmbulanceAction.turnRight;
            } else if(getAmbulanceDirection(amb) != 270) return getAmbulanceDirection(amb) == 180 ? AmbulanceAction.turnRight : AmbulanceAction.turnLeft;
        }
        return null;
    }

    private Queue<AmbulanceAction> buildPathPlan(Point p1, Point p2, Ambulance amb) {
        Stack<Point> path = new Stack<Point>();
        Node node = shortestPath(p1, p2, amb);
        path.add(node.point);
        while(node.parent!=null) {
            node = node.parent;
            path.push(node.point);
        }
        Queue<AmbulanceAction> result = new LinkedList<AmbulanceAction>();
        p1 = path.pop();
        int auxDirection = getAmbulanceDirection(amb);
        while(!path.isEmpty()) {
            p2 = path.pop();
            result.add(AmbulanceAction.moveForward);
            result.addAll(turns(p1, p2, amb));
            p1 = p2;
        }
        amb.setDirection(auxDirection);
        result.remove();
        return result;
    }

    /* For queue used in shortest path */
    public class Node {
        Point point;
        Node parent; //cell's distance to source
        public Node(Point point, Node parent) {
            this.point = point;
            this.parent = parent;
        }
        public String toString() {
            return "("+point.x+","+point.y+")";
        }
    }

    public Node shortestPath(Point src, Point dest, Ambulance amb) {
        boolean[][] visited = new boolean[100][100];
        visited[src.x][src.y] = true;
        Queue<Node> q = new LinkedList<Node>();
        q.add(new Node(src,null)); //enqueue source cell

        //access the 4 neighbours of a given cell
        int row[] = {-1, 0, 0, 1};
        int col[] = {0, -1, 1, 0};

        while (!q.isEmpty()){//do a BFS
            Node curr = q.remove(); //dequeue the front cell and enqueue its adjacent cells
            Point pt = curr.point;
            for (int i = 0; i < 4; i++) {
                int x = pt.x + row[i], y = pt.y + col[i];
                if(x==dest.x && y==dest.y) return new Node(dest,curr);
                if(!amb.outOfBounds(x,y) && !world.containsKey(new Point(x,y)) && !visited[x][y]){
                    visited[x][y] = true;
                    q.add(new Node(new Point(x,y), curr));
                }
            }
        }
        return null; //destination not reached
    }

    private List<AmbulanceAction> turns(Point p1, Point p2, Ambulance amb) {
        List<AmbulanceAction> result = new ArrayList<AmbulanceAction>();
        while(!p2.equals(getAmbulanceForwardPosition(amb))) {
            AmbulanceAction ambulanceAction = turn(p1,p2,amb);
            if(ambulanceAction ==null) break;
            amb.execute(ambulanceAction);
            result.add(ambulanceAction);
        }
        return result;
    }

    public static void setIdCount(int idCount) {
        CommunicationEmergencyCenter.idCount = idCount;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
