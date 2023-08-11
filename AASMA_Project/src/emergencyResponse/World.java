package emergencyResponse;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class World {

    public static int x = 20, y = 20;
    private static Tile[][] world;
    private static ObjectType[][] objects;
    private static ArrayList<CommunicationEmergencyCenter> centers;
    private static ArrayList<Hospital> hospitals;
    private static ArrayList<Emergency> emergencies;
    private static ArrayList<Ambulance> ambulances;
    private static Double distances[][];
    private static GUI GUI;
    private static RunThread runThread;
    private static int centersNumber = 4;
    private static int hospitalsNumber = 6;

    private static int emergenciesNumber = 5;

    //creates world
    public static void initialize() {
        world = new Tile[x][y];
        for(int i=0; i < x; i++)
            for(int j=0; j < y; j++)
                world[i][j] = new Tile(Color.lightGray);

        objects = new ObjectType[x][y];
        distances = new Double[emergenciesNumber][centersNumber];

        for(int i = 0; i < emergenciesNumber; i++) {
            for(int j = 0; j < centersNumber; j++) {
                distances[i][j] = null;
            }
        }


        // create agents
        ////-------------------------------------------------
        ArrayList<Point> centersLoc = new ArrayList<Point>();
        centersLoc.add(new Point(0,0));
        centersLoc.add(new Point(0,19));
        centersLoc.add(new Point(19,0));
        centersLoc.add(new Point(19,19));

        ArrayList<String> centersColor = new ArrayList<>();
        centersColor.add("Blue");
        centersColor.add("Orange");
        centersColor.add("Green");
        centersColor.add("Purple");
        ////---------------------------------------------------

        CommunicationEmergencyCenter.setIdCount(1);
        centers = new ArrayList<CommunicationEmergencyCenter>();
        for(int i = 0; i < centersNumber; i++) {
            CommunicationEmergencyCenter center = new CommunicationEmergencyCenter(centersLoc.get(i));
            center.setColor(centersColor.get(i));
            centers.add(center);
            objects[center.getPoint().x][center.getPoint().y] = center;
        }

        ////-------------------------------------------------
        ArrayList<Point> hospitalsLoc = new ArrayList<Point>();
        //torre 1
        hospitalsLoc.add(new Point(3,6));
        //torre 2
        hospitalsLoc.add(new Point(2,14));
        hospitalsLoc.add(new Point(7,15));
        //torre 3
        hospitalsLoc.add(new Point(15,4));
        hospitalsLoc.add(new Point(19,9));
        //torre 4
        hospitalsLoc.add(new Point(14,14));
        ////---------------------------------------------------

        // create hospitals
        hospitals = new ArrayList<Hospital>();
        for(int i = 0; i < hospitalsNumber; i++) {
            Hospital hospital = new Hospital(hospitalsLoc.get(i));
            hospitals.add(hospital);
            objects[hospital.getPoint().x][hospital.getPoint().y] = hospital;
        }

        hospitals.get(0).setMaxCapacity(3);
        hospitals.get(1).setMaxCapacity(1);
        hospitals.get(2).setMaxCapacity(2);
        hospitals.get(3).setMaxCapacity(1);
        hospitals.get(4).setMaxCapacity(2);
        hospitals.get(5).setMaxCapacity(1);


        //creates ambulances
        ambulances = new ArrayList<>();

        //torre 1
        //Hospital 1
        Ambulance ambulance1 = new IntensiveCareSupportAmbulance(new Point(3,6));
        ambulances.add(ambulance1);
        hospitals.get(0).addAmbulance(ambulance1);

        Ambulance ambulance2 = new BasicLifeSupportAmbulance(new Point(3,6));
        ambulances.add(ambulance2);
        hospitals.get(0).addAmbulance(ambulance2);

        Ambulance ambulance3 = new PatientTransportAmbulance(new Point(3,6));
        ambulances.add(ambulance3);
        hospitals.get(0).addAmbulance(ambulance3);

        //torre 2
        //Hospital 2
        Ambulance ambulance4 = new BasicLifeSupportAmbulance(new Point(2,14));
        ambulances.add(ambulance4);
        hospitals.get(1).addAmbulance(ambulance4);

        //Hospital 3
        Ambulance ambulance5 = new PatientTransportAmbulance(new Point(7,15));
        ambulances.add(ambulance5);
        hospitals.get(2).addAmbulance(ambulance5);

        //torre 3
        //Hospital 4
        Ambulance ambulance6 = new PatientTransportAmbulance(new Point(15,4));
        ambulances.add(ambulance6);
        hospitals.get(3).addAmbulance(ambulance6);

        Ambulance ambulance7 = new BasicLifeSupportAmbulance(new Point(15,4));
        ambulances.add(ambulance7);
        hospitals.get(3).addAmbulance(ambulance7);

        //Hospital 5
        Ambulance ambulance8 = new IntensiveCareSupportAmbulance(new Point(19,9));
        ambulances.add(ambulance8);
        hospitals.get(4).addAmbulance(ambulance8);

        //torre 4
        //Hospital 6
        Ambulance ambulance9 = new PatientTransportAmbulance(new Point(14,14));
        ambulances.add(ambulance9);
        hospitals.get(5).addAmbulance(ambulance9);

        sendHospitals(hospitals);

        //creates emergencies
        Emergency.setIdCount(1);
        emergencies = new ArrayList<Emergency>();

        Random r = new Random();
        int low = 0;
        int high = x;

        //execução normal de emegencias aleatorias
        dynamicExample(r,high, low);

        //exemplo estatico (do video)
        //staticExample();
    }

    /****************************
     ***** world METHODS *****
     ****************************/

    public static ObjectType getObjectType(Point point) {
        return objects[point.x][point.y];
    }
    public static Tile getTile(Point point) {
        return world[point.x][point.y];
    }
    public static void removeObjectType(Point point) {
        ObjectType object = getObjectType(point);
        emergencies.remove(object);
        objects[point.x][point.y] = null;
    }


    /***********************************
     ***** ELICIT AGENT ACTIONS *****
     ***********************************/

    public static class RunThread extends Thread {

        int time;

        public RunThread(int time){
            this.time = time*time;
        }

        public void run() {
            while(true){
                step();
                try {
                    sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void run(int time) {
        World.runThread = new RunThread(time);
        World.runThread.start();
    }

    public static void reset() {
        removeObjects();
        initialize();
        GUI.displayWorld();
        displayObjects();
        GUI.update();
    }

    public static void step() {
        removeObjects();
        for(CommunicationEmergencyCenter center : centers) center.agentDecision();
        for(Ambulance ambulance: ambulances) ambulance.decision();
        for(Emergency emergency: emergencies) emergency.updateChanceToDie();
        displayObjects();
        GUI.update();
    }

    public static void stop() {
        runThread.interrupt();
        runThread.stop();
    }

    public static void displayObjects(){
        for(CommunicationEmergencyCenter center: centers) {
            GUI.displayObject(center);
        }

        for(Ambulance ambulance: ambulances) {
            GUI.displayObject(ambulance);
        }

        for(Hospital hospital: hospitals) {
            GUI.displayObject(hospital);
        }

        for(Emergency emergency: emergencies) {
            GUI.displayObject(emergency);
        }
    }

    public static void removeObjects(){

        for(CommunicationEmergencyCenter center: centers) {
            GUI.removeObject(center);
        }
        for(Hospital hospital: hospitals) {
            GUI.removeObject(hospital);
        }

        for(Emergency emergency: emergencies) {
            GUI.removeObject(emergency);
        }

        for(Ambulance ambulance: ambulances) {
            GUI.removeObject(ambulance);
        }
    }

    /***********************************
     ***** COMMUNICATION *****
     ***********************************/

    public static void broadcastEmergency(Emergency emergency) {
        for(CommunicationEmergencyCenter center : centers) {
            center.detectEmergency(emergency);
        }
    }

    public static void sendHospitals(ArrayList<Hospital> hospitals) {
        ArrayList<Hospital> hospitalArrayList;
        for(CommunicationEmergencyCenter center: centers) {
            switch(center.id) {
                case 1:
                    hospitalArrayList = new ArrayList<>();
                    hospitals.get(0).setCenterId(1);
                    hospitalArrayList.add(hospitals.get(0));
                    center.loadHospitalsAndAmbulances(hospitalArrayList);
                    break;
                case 2:
                    hospitals.get(1).setCenterId(2);
                    hospitals.get(2).setCenterId(2);
                    hospitalArrayList = new ArrayList<>();
                    hospitalArrayList.add(hospitals.get(1));
                    hospitalArrayList.add(hospitals.get(2));
                    center.loadHospitalsAndAmbulances(hospitalArrayList);
                    break;
                case 3:
                    hospitals.get(3).setCenterId(3);
                    hospitals.get(4).setCenterId(3);
                    hospitalArrayList = new ArrayList<>();
                    hospitalArrayList.add(hospitals.get(3));
                    hospitalArrayList.add(hospitals.get(4));
                    center.loadHospitalsAndAmbulances(hospitalArrayList);
                    break;
                case 4:
                    hospitals.get(5).setCenterId(4);
                    hospitalArrayList = new ArrayList<>();
                    hospitalArrayList.add(hospitals.get(5));
                    center.loadHospitalsAndAmbulances(hospitalArrayList);
                    break;
            }
        }
    }

    public static Double[] requestHelp(Emergency emergency, int centerId) {
        Double[] centerDistance = new Double[getCentersNumber()];
        for(CommunicationEmergencyCenter center: centers) {
            if(center.id != centerId) {
                centerDistance[center.id - 1] = center.receiveHelpRequest(emergency);
            }
        }

        return centerDistance;
    }

    public static void sendDistance(Double distance, Emergency emergency, int emergencyId, int centerId) {

        distances[emergencyId-1][centerId-1] = distance;

        for(CommunicationEmergencyCenter center: centers) {
            center.receiveDistance(distances, emergency, emergencyId);
        }
    }

    public static void sendEmergencyToCenter(Emergency emergency, int centerId) {
        for(CommunicationEmergencyCenter center: centers) {
            if(center.id == centerId + 1 ) {
                center.receiveNewEmergency(emergency);
            }
        }
    }

    public static Point[] sendHospitalRequestMessage(Point ambPoint, int ambNumPatients) {
        Point[] hospitalsPoint = new Point[getCentersNumber()];
        for(CommunicationEmergencyCenter center: centers) {
            hospitalsPoint[center.id-1] = center.receiveHospitalRequestMessage(ambPoint, ambNumPatients);
        }

        return hospitalsPoint;
    }

    /*******************************/
    /****** auxiliary *****/
    /*******************************/

    public static Double[][] getDistances() {
        return distances;
    }

    public static void setDistances(int emergencyId, int centerId) {
        for(int i = 0; i < centersNumber; i++) {
            distances[emergencyId-1][centerId-1] = Double.POSITIVE_INFINITY;
        }
    }

    public static void setGUI(emergencyResponse.GUI GUI) {
        World.GUI = GUI;
    }

    public static int getCentersNumber() {
        return centersNumber;
    }

    /*******************************/
    /****** Emergency execution *****/
    /*******************************/

    public static void dynamicExample(Random r, int high, int low) {
        Point emergencyLocation;
        for (int i = 0; i < emergenciesNumber; i++) {
            emergencyLocation = new Point(r.nextInt(high - low) + low, r.nextInt(high - low) + low);
            while(getObjectType(emergencyLocation) != null) {
                emergencyLocation = new Point(r.nextInt(high - low) + low, r.nextInt(high - low) + low);
            }
            Emergency emergency = new Emergency(emergencyLocation);
            emergencies.add(emergency);
            objects[emergency.getPoint().x][emergency.getPoint().y] = emergency;
            broadcastEmergency(emergency);
        }
    }

    public static void staticExample() {
        //example1(); //exemplo simples
        //example2();  //torre 4 pede ajuda ao centro 3
        //example3(); //hospital cheio
        //example4(); //prioridade em conjunto com o example3
        example5(); //todos os anteriores
    }

    public static void example1() {
        Emergency emergency3 = new Emergency(new Point(14,6));
        emergency3.setSeverity(8);
        emergency3.setEmergencyType(Emergency.EmergencyType.intensiveSupport);
        emergencies.add(emergency3);
        objects[emergency3.getPoint().x][emergency3.getPoint().y] = emergency3;
        broadcastEmergency(emergency3);
    }

    public static void example2() {
        Emergency emergency = new Emergency(new Point(15,16));
        emergency.setSeverity(9);
        emergency.setEmergencyType(Emergency.EmergencyType.intensiveSupport);
        emergencies.add(emergency);
        objects[emergency.getPoint().x][emergency.getPoint().y] = emergency;
        broadcastEmergency(emergency);
    }

    public static void example3() {
        Emergency emergency4 = new Emergency(new Point(3,17));
        emergency4.setSeverity(2);
        emergency4.setEmergencyType(Emergency.EmergencyType.patientTransportation);
        emergencies.add(emergency4);
        objects[emergency4.getPoint().x][emergency4.getPoint().y] = emergency4;
        broadcastEmergency(emergency4);

        Emergency emergency5 = new Emergency(new Point(1,15));
        emergency5.setSeverity(1);
        emergency5.setEmergencyType(Emergency.EmergencyType.patientTransportation);
        emergencies.add(emergency5);
        objects[emergency5.getPoint().x][emergency5.getPoint().y] = emergency5;
        broadcastEmergency(emergency5);
    }

    public static void example4() {
        Emergency emergency6 = new Emergency(new Point(9,19));
        emergency6.setSeverity(5);
        emergency6.setEmergencyType(Emergency.EmergencyType.basicSupport);
        emergencies.add(emergency6);
        objects[emergency6.getPoint().x][emergency6.getPoint().y] = emergency6;
        broadcastEmergency(emergency6);
    }

    public static void example5() {
        Emergency emergency3 = new Emergency(new Point(14,6));
        emergency3.setSeverity(8);
        emergency3.setEmergencyType(Emergency.EmergencyType.intensiveSupport);
        emergencies.add(emergency3);
        objects[emergency3.getPoint().x][emergency3.getPoint().y] = emergency3;
        broadcastEmergency(emergency3);

        Emergency emergency = new Emergency(new Point(15,16));
        emergency.setSeverity(9);
        emergency.setEmergencyType(Emergency.EmergencyType.intensiveSupport);
        emergencies.add(emergency);
        objects[emergency.getPoint().x][emergency.getPoint().y] = emergency;
        broadcastEmergency(emergency);

        Emergency emergency4 = new Emergency(new Point(3,17));
        emergency4.setSeverity(2);
        emergency4.setEmergencyType(Emergency.EmergencyType.patientTransportation);
        emergencies.add(emergency4);
        objects[emergency4.getPoint().x][emergency4.getPoint().y] = emergency4;
        broadcastEmergency(emergency4);

        Emergency emergency5 = new Emergency(new Point(1,15));
        emergency5.setSeverity(1);
        emergency5.setEmergencyType(Emergency.EmergencyType.patientTransportation);
        emergencies.add(emergency5);
        objects[emergency5.getPoint().x][emergency5.getPoint().y] = emergency5;
        broadcastEmergency(emergency5);

        Emergency emergency6 = new Emergency(new Point(9,19));
        emergency6.setSeverity(5);
        emergency6.setEmergencyType(Emergency.EmergencyType.basicSupport);
        emergencies.add(emergency6);
        objects[emergency6.getPoint().x][emergency6.getPoint().y] = emergency6;
        broadcastEmergency(emergency6);
    }
}
