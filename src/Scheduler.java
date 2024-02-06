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
    private List<ElevatorTrip> requestQueue; // queue of pending elevator calls
    private Map<Integer, Integer> elevatorCarPositions;
    private Map<Integer, ElevatorTrip> activeTrips;

    //Singleton object 
    private static Scheduler scheduler;
    

    private Scheduler() {
        FloorSubsystemNodes = new ArrayList<>();
        elevatorSubsystemNodes = new ArrayList<>();
        requestQueue = new ArrayList<>();
        elevatorCarPositions = new HashMap<>();
        activeTrips = new HashMap<>();
        requestsComplete = false;
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
        if (elevatorSubsystemNode instanceof Elevator){
            elevatorSubsystemNodes.add(elevatorSubsystemNode);
        }
        else{
            System.out.println("The Object trying to be registered is of type: " + elevatorSubsystemNode.getClass() + "\nAllowed object type is 'ElevatorSubsystem'");
        }
    }

    public synchronized void addRequest(ElevatorTrip elevatorTrip){
        //try to adding the coming request to an existing request
        System.out.println(String.format("[SCHEDULER] Received new elevator call: \n%s", elevatorTrip));
        for (ElevatorTrip elevatorTripIterator : activeTrips.values()) {
            if (elevatorTripIterator.mergeRequest(elevatorTrip)){
                return;
            }
        }
        //If no current request satisfy the coming request then append at the end of the requests list
        requestQueue.add(elevatorTrip);
        notifyAll();
    }

    public void signalRequestsComplete() {
        requestsComplete = true;
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

        ElevatorTrip nextRequest = requestQueue.remove(0);
        nextRequest.setCurrentFloor(elevatorCarPositions.get(elevatorId));

        List<ElevatorTrip> merged = new ArrayList<>();
        for (ElevatorTrip request : requestQueue) {
            if (nextRequest.mergeRequest(request)) {
                merged.add(request);
            }
        }
        requestQueue.removeAll(merged);

        activeTrips.put(elevatorId, nextRequest);

        return true;
    }

    public Elevator.Action getNextAction(int elevatorId, int currentFloor) {
        elevatorCarPositions.put(elevatorId, currentFloor);

        if ((activeTrips.get(elevatorId) == null || activeTrips.get(elevatorId).getNextTargetFloor() == null) && !assignTrip(elevatorId)) {
            return Elevator.Action.QUIT;
        }

        ElevatorTrip trip = activeTrips.get(elevatorId);

        int prevTargetFloor = trip.getNextTargetFloor();
        trip.setCurrentFloor(currentFloor);
        if (currentFloor == prevTargetFloor) {
            return Elevator.Action.TOGGLE_DOORS;
        } else if (currentFloor < trip.getNextTargetFloor()) {
            return Elevator.Action.UP;
        } else {
            return Elevator.Action.DOWN;
        }
    }
}