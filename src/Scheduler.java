import java.util.*;

/**
 * Class reposnisple for receiving and notifying the different elevator system 
 * classes with the data received/provided whenever there is a signal received
 * from the Arrival sensor, or the Elevator or the Subfloor classes.
 *
 * Follow singleton design pattern to only allows instantiation of one Schedular object.
 * Satisfy the requirements where only one Schedular is needed in the system.
 *
 * @author Jaden Sutton
 * @author Amr Abdelazeem
 * @version 1.00
 */

public class Scheduler {
    private boolean requestsComplete;
    private List<Runnable>  FloorSubsystemNodes; //Represents the list of subFloorSubsystem threads.
    private List<Runnable>  elevatorSubsystemNodes; //Represents the list of elevatorSubsystem threads.
    private List<ElevatorCall> requestQueue; // queue of pending elevator calls
    private Map<Integer, Integer> elevatorCarPositions;
    private Map<Integer, ElevatorCall> activeTrips;

    //Singleton object
    private static Scheduler scheduler;


    //The scheduler is continuously active and can be in one of the following states:
    //1- WAITING_FOR_REQUESTS : a state where the Scheduler is in IDLE state
    //2- ASSIGNING_ACTIONS: a momentarily state where the scheduler is assigning an action (trip info) to an elevator
    //3- Adding_Actions: a momentarily state where the scheduler is adding requests to the elevator requests queue
    private enum State {WAITING_FOR_REQUEST, ASSIGNING_ACTION, ADDING_REQUEST}
    private State currentState;


    Scheduler() {
        FloorSubsystemNodes = new ArrayList<>();
        elevatorSubsystemNodes = new ArrayList<>();
        requestQueue = new ArrayList<>();
        elevatorCarPositions = new HashMap<>();
        activeTrips = new HashMap<>();
        requestsComplete = false;

        //Initial scheduler state where no actions assignations nor requests happened yet
        currentState = State.WAITING_FOR_REQUEST;
    }

    /** Creates and returns Scheduler object upon check singularity.
     * @return Agent object
     */
    public static Scheduler getScheduler(Scheduler scheduler){
        if (scheduler == null){
            synchronized (Scheduler.class) {
                scheduler = new Scheduler();
            }
        }
        return scheduler;
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

    public synchronized void addRequest(ElevatorCall elevatorCall){
        //try to adding the coming request to an existing request
        System.out.println(String.format("[SCHEDULER] Received new elevator call: \n%s", elevatorCall));

        currentState = State.ADDING_REQUEST; //trigging event (add request call) 

        for (ElevatorCall elevatorCallIterator : activeTrips.values()) {
            if (elevatorCallIterator.mergeRequest(elevatorCall)){
                currentState = State.WAITING_FOR_REQUEST; //trigging event (request call merged to an existing call) 
                return;
            }
        }
        //If no current request satisfy the coming request then append at the end of the requests list
        requestQueue.add(elevatorCall);
        notifyAll();
        //trigging event (request call appended to the end of the queue) 
        currentState = State.WAITING_FOR_REQUEST; 
    }

    public void signalRequestsComplete() {
        requestsComplete = true;
    }

    public ElevatorSubsystem.Action getNextAction(int elevatorId, int currentFloor) {
        currentState = State.ASSIGNING_ACTION; //trigging event (assign action) 
        System.out.println(String.format("[SCHEDULER] Received a new elevator assignation request from elevator %d\n", elevatorId));

        elevatorCarPositions.put(elevatorId, currentFloor);

        if ((activeTrips.get(elevatorId) == null || activeTrips.get(elevatorId).getNextTargetFloor() == null) && !assignTrip(elevatorId)) {
            return ElevatorSubsystem.Action.QUIT;
        }

        ElevatorCall trip = activeTrips.get(elevatorId);

        int prevTargetFloor = trip.getNextTargetFloor();
        trip.setCurrentFloor(currentFloor);
        if (currentFloor == prevTargetFloor) {
            currentState = State.WAITING_FOR_REQUEST; //trigging event (assigned action)
            return ElevatorSubsystem.Action.TOGGLE_DOORS;
        } else if (currentFloor < trip.getNextTargetFloor()) {
            currentState = State.WAITING_FOR_REQUEST; //trigging event (assigned action)
            return ElevatorSubsystem.Action.UP;
        } else {
            currentState = State.WAITING_FOR_REQUEST; //trigging event (assigned action)
            return ElevatorSubsystem.Action.DOWN;
        }
    }

    private synchronized boolean assignTrip(int elevatorId) {
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
}