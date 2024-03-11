/*
* @author Jaden Sutton
* @author Amr Abdelazeem
* @version 2.00
*/

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.*;
import java.io.IOException;

/**
* Scheduler states interface
*/
interface SchedulerState {
    /**
     * add an elevator request to the list of elevator calls 
     * @param context the scheduler object 
     * @param elevatorCall the call to the elevator
     */
    default void addRequest(Scheduler context, ElevatorCall elevatorCall){
        context.setState("AddingRequest");
        
        for (ElevatorCall elevatorCallIterator : context.getActiveTrips().values()) {
            if (elevatorCallIterator.mergeRequest(elevatorCall)) {
                break; // Request merged, no need to add to the queue
            }
        }

        
        // Request not merged, add to the queue
        context.getRequestQueue().add(elevatorCall);
        context.notifyAll(); 

        // After handling, transition state back to waiting for a request state
        
        context.setState("WaitingForRequest");
    };

    /**
     * 
     * @param context the scheduler object
     * @param elevatorId the elevator car id
     * @param currentFloor the current floor for the elevator car with the given id
     * @return an action command to the car with the given id, representing the car next state
     */
    default ElevatorSubsystem.Action getNextAction(Scheduler context, int elevatorId, int currentFloor){
        context.setState("AssigningAction");

        context.getElevatorCarPositions().put(elevatorId, currentFloor);

        if ((context.getActiveTrips().get(elevatorId) == null || context.getActiveTrips().get(elevatorId).getNextTargetFloor() == null) && !context.assignTrip(elevatorId)) {
            //retrieving the original state for the Scheduler: WaitingForRequest state (IDLE)
            //trigging event (assigned action)
            context.setState("WaitingForRequest");  
            return ElevatorSubsystem.Action.QUIT;
        }

        ElevatorCall trip = context.getActiveTrips().get(elevatorId);

        int prevTargetFloor = trip.getNextTargetFloor();
        trip.setCurrentFloor(currentFloor);
        ElevatorSubsystem.Action action;

        if (currentFloor == prevTargetFloor) {
             action = ElevatorSubsystem.Action.TOGGLE_DOORS;
        } else if (currentFloor < trip.getNextTargetFloor()) {
            action = ElevatorSubsystem.Action.UP;
        } else {
            action = ElevatorSubsystem.Action.DOWN;
        }

        //retrieving the original state for the Scheduler: WaitingForRequest state (IDLE)
        //trigging event (assigned action)   
        context.setState("WaitingForRequest");
        return action;
    }

    /**
     * Display the current scheduler state
     */
    void displayState();
}

/**
* Scheduler WaitingForRequest state concrete class
*/
class WaitingForRequest implements SchedulerState {
    @Override
    public void displayState() {
        System.out.println("Scheduler State: IDLE, waiting to process an elevator call or an action request...\n");
    }
}


/**
* Scheduler AddingRequest state concrete class
*/
class AddingRequest implements SchedulerState {

    @Override
    public void displayState() {
        System.out.println("Scheduler State: Processing an elevator call\n");
    }
}

/**
* Scheduler AssigningAction state concrete class
*/
class AssigningAction implements SchedulerState {

    @Override
    public void displayState() {
        System.out.println("Scheduler State: Assigning elevator car action\n");
    }
}


/**
 * Class responsible for receiving and notifying the different elevator system 
 * classes with the data received/provided whenever there is a signal received
 * from the Arrival sensor, or the Elevator or the Subfloor classes.
 *
 * Follow singleton design pattern to only allows instantiation of one Schedular object.
 * Satisfy the requirements where only one Schedular is needed in the system.
 */

public class Scheduler {
    private SchedulerState currentState; //represents the Scheduler current state
    private boolean requestsComplete;
    private List<Runnable>  FloorSubsystemNodes; //Represents the list of subFloorSubsystem threads.
    private List<Runnable>  elevatorSubsystemNodes; //Represents the list of elevatorSubsystem threads.
    private List<ElevatorCall> requestQueue; // queue of pending elevator calls
    private Map<Integer, Integer> elevatorCarPositions;
    private Map<Integer, ElevatorCall> activeTrips;
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;



    //Singleton object
    private static Scheduler scheduler;

    //The scheduler is continuously active and can be in one of the following states:
    //1- WAITING_FOR_REQUESTS : a state where the Scheduler is in IDLE state
    //2- ASSIGNING_ACTIONS: a momentarily state where the scheduler is assigning an action (movement direction, toggle door, etc. ) to an elevator car
    //3- Adding_Actions: a momentarily state where the scheduler is adding requests to the elevator requests queue
    Map<String, SchedulerState> states;

    Scheduler() {
        FloorSubsystemNodes = new ArrayList<>();
        elevatorSubsystemNodes = new ArrayList<>();
        requestQueue = new ArrayList<>();
        elevatorCarPositions = new HashMap<>();
        activeTrips = new HashMap<>();
        requestsComplete = false;
        states = new HashMap<>();

        //Adding the states to the states hashmap and linking it to the relative state 
        states.put("WaitingForRequest", new WaitingForRequest());
        states.put("AddingRequest", new AddingRequest());
        states.put("AssigningAction", new AssigningAction());


        //Initial scheduler state where no actions assignations nor requests happened yet
        this.currentState = new WaitingForRequest();
    }

    /** Creates and returns Scheduler object upon check singularity.
     * and calls method for listening for UDP packets
     * @return Agent object
     */
    public static Scheduler getScheduler(){
        if (scheduler == null){
            synchronized (Scheduler.class) {
                scheduler = new Scheduler();
            }
            scheduler.startListeningElevator();
        }
        return scheduler;
    }
    
    public List<ElevatorCall> getRequestQueue() {
        return requestQueue;
    }

    public Map<Integer, ElevatorCall> getActiveTrips() {
        return activeTrips;
    }


    public boolean isRequestsComplete() {
        return requestsComplete;
    }

    public Map<Integer, Integer> getElevatorCarPositions() {
        return elevatorCarPositions;
    }

    public SchedulerState getCurrentState() {
        return currentState;
    }

    public void setState(String stateName) {
        this.currentState = states.get(stateName);
    }
    
    /** Used to register SubFloorSubsystem nodes (floors) to the SubFloorSubsystemNodes arraylist
     *  Uses dependency injection to avoid circular dependency
     *
     * @param subFloorSubsystemNode: SubFloorSubsystem node to be added
     */
    public void registerSubFloorSubsystemNode(Runnable subFloorSubsystemNode){

        //ensure the node getting registered is of type SubFloorSubsystemNode
        if (subFloorSubsystemNode instanceof FloorSubsystem){
            FloorSubsystemNodes.add(subFloorSubsystemNode);
        }
        else{
            System.out.println("The Object trying to be registered is of type: " + subFloorSubsystemNode.getClass() + "\nAllowed object type is 'SubFloorSubsystem'");
        }
    }

    /** Used to register ElevatorSubsystem nodes (elevator cars) to the elevatorSubsystemNodes arraylist
     *  Uses dependency injection to avoid circular dependency
     *
     * NOTE: Should only has a size of 1 for this iteration since there is only one Elevator car
     *
     * @param elevatorSubsystemNode: elevatorSubsystem node to be added
     */
    public void registerElevatorSubsystemNode(Runnable elevatorSubsystemNode){

        //ensure the node getting registered is of type SubFloorSubsystemNode
        if (elevatorSubsystemNode instanceof ElevatorSubsystem){
            elevatorSubsystemNodes.add(elevatorSubsystemNode);
        }
        else{
            System.out.println("The Object trying to be registered is of type: " + elevatorSubsystemNode.getClass() + "\nAllowed object type is 'ElevatorSubsystem'");
        }
    }

    /**
     * Adding the request for the elevator call given to the suitable queue
     * 
     * @param elevatorCall: elevator request to be added
     */
    public synchronized void addRequest(ElevatorCall elevatorCall){
        //Delegate the task to the corresponding state 
        currentState.addRequest(this, elevatorCall);
        
        //try to adding the coming request to an existing request
        System.out.println(String.format("[SCHEDULER] Received new elevator call: \n%s", elevatorCall));
    }

    public void signalRequestsComplete() {
        requestsComplete = true;
    }

    /**
     * to be used by the Elevator Subsystem to notify the scheduler that it reached a floor,
     * so that scheduler provide the elevator car with the next elevator action
     * 
     * @param elevatorId: elevator identification
     * @param currentFloor: the current floor that the elevator in.
     */
    public synchronized ElevatorSubsystem.Action getNextAction(int elevatorId, int currentFloor) {
        //Delegate the task to the corresponding state 
        return currentState.getNextAction(this, elevatorId, currentFloor);
    }


    public synchronized boolean assignTrip(int elevatorId) {
        while (requestQueue.size() == 0 && requestsComplete == false) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (requestQueue.size() == 0) {
            return false;
        }

        ElevatorCall nextRequest = requestQueue.remove(0);
        nextRequest.setCurrentFloor(elevatorCarPositions.get(elevatorId));

        List<ElevatorCall> merged = new ArrayList<>();
        for (ElevatorCall request : requestQueue) {
            if (nextRequest.mergeRequest(request)) {
                merged.add(request);
            }
        }
        requestQueue.removeAll(merged);

        activeTrips.put(elevatorId, nextRequest);

        return true;
    }

    public void startListeningElevator() {
        Thread listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                listenForElevatorRequests();
            }
        });
        listenerThread.start();
    }


    /**
     * creats the recieving socket and packet, decodes them and get the needed data from the scheduler class then sends
     * this data in a new packet to the same port
     *
     *
     */
    public void listenForElevatorRequests() {
        try {

            sendReceiveSocket = new DatagramSocket(69);
            byte[] receiveData = new byte[Integer.BYTES * 2];

            while (true) {
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                sendReceiveSocket.receive(receivePacket);

                ByteBuffer byteBuffer = ByteBuffer.wrap(receivePacket.getData());
                int elevatorId = byteBuffer.getInt();
                int currentFloor = byteBuffer.getInt();

                ElevatorSubsystem.Action action = getNextAction(elevatorId, currentFloor);


                byte[] sendData = action.name().getBytes("UTF-8");

                sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
                sendReceiveSocket.send(sendPacket);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Ensure the socket is closed to release resources
            if (sendReceiveSocket != null && !sendReceiveSocket.isClosed()) {
                sendReceiveSocket.close();
            }
        }
    }

}