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
    private List<ElevatorCall> activeTrips; // active elevator trips indexed by elevator id

    //Singleton object 
    private static Scheduler scheduler;
    

    private Scheduler() {
        FloorSubsystemNodes = new ArrayList<>();
        elevatorSubsystemNodes = new ArrayList<>();
        requestQueue = new ArrayList<>();
        activeTrips = new ArrayList<>();
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
        for (ElevatorCall elevatorCallIterator: activeTrips) {
            if (elevatorCallIterator.mergeRequest(elevatorCall)){
                return;
            }
        }
        //If no current request satisfy the coming request then append at the end of the requests list
        requestQueue.add(elevatorCall);
        notifyAll();
    }

    public void signalRequestsComplete() {
        requestsComplete = true;
    }

    /** Used to notify the Schedular that an elevator cas was detected.
     * Note: This method should only be used by an arrival sensor (ElevatorSubSystem).
     *
     * @param floorNumber: The floor number where the elevator car was detected
     */
    public synchronized void notifySchedulerElevatorDetected(Integer floorNumber, int elevatorSubsystemCarID){
        //Notify the floors to reflect the new location that elevator car is approaching and going away
        notifyFloorSubSystemNodesElevatorDetected(elevatorSubsystemCarID, floorNumber);

        //Notify the elevator to reflect its location on the screen and if the elevator is supposed to stop on that floor reached then makes the motor stop moving,
        // the floor light turn off, and the doors open.
        ElevatorSubsystem elevatorCarDetected = getElevatorCarWithTheGivenID(elevatorSubsystemCarID);
        //Should also be notifying the elevator in the coming iterations for synchronization purpose 
        // notifyElevatorWithFloorDetected(floorNumber, elevatorCarDetected);
    }

    /**
    * Get the elevator car with ID provided
    * @param elevatorSubsystemCarID: the elevator car ID
    * @return ElevatorSubsystem elevatorCar: the elevator car with id given.
    */
    private ElevatorSubsystem getElevatorCarWithTheGivenID(int elevatorSubsystemCarID){

        ElevatorSubsystem targetedElevatorCar = null;

        for (Runnable elevatorCar: elevatorSubsystemNodes){
            if (elevatorCar instanceof ElevatorSubsystem){
                ElevatorSubsystem elevator = (ElevatorSubsystem) elevatorCar;
                if (elevator.getElevatorId() == elevatorSubsystemCarID){
                    targetedElevatorCar = elevator;
                    break;
                }
            }
        }
        //Make sure that the elevator with the given id was found
        if (targetedElevatorCar ==  null){
            System.out.println("ERROR::: ElevatorCar with the given ID: " + elevatorSubsystemCarID + " does not exit!");
            return null;
        }
        return targetedElevatorCar;
    }

    /** Notify the list of subFloorSubsystemNodes with the location of the elevator car.
     * 
     * @param floorNumber: The floor number where the elevator car was detected
     */
    private void notifyFloorSubSystemNodesElevatorDetected(int elevatorSubsystemCarID, int floorNumber){
        for (Runnable floor : FloorSubsystemNodes){
            if (floor instanceof FloorSubsystem){
            FloorSubsystem floorSubSystem = (FloorSubsystem) floor;
            floorSubSystem.updateElevatorCarDisplay(elevatorSubsystemCarID, floorNumber);
            }
        }
    }

    public synchronized ElevatorCall getNextTrip(ElevatorSubsystem caller) {
        if (caller.getCurrentTrip() != null) {
            activeTrips.remove(caller.getCurrentTrip());
        }

        while (requestQueue.size() == 0 && requestsComplete == false) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (requestsComplete) {
            return null;
        }

        ElevatorCall nextRequest = requestQueue.remove(0);
        nextRequest.setOwner(caller);

        for (ElevatorCall request : requestQueue) {
            if (nextRequest.mergeRequest(request)) {
                requestQueue.remove(request);
            }
        }

        activeTrips.add(nextRequest);

        return nextRequest;
    }

    public static void main(String[] args) {
        Scheduler scheduler = getScheduler(new Scheduler());
        Thread elevatorThread, floorThread;
        elevatorThread = new Thread(new ElevatorSubsystem(scheduler));
        floorThread = new Thread(new FloorSubsystem(scheduler, "ElevatorCalls"));

        floorThread.start();
        elevatorThread.start();
    }
}