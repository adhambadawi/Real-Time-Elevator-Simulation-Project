import java.util.*;

/**
 * Class reposnisple for receiving and notifying the different elevator system 
 * classes with the data received/provided whenever there is a signal received
 * from the Arrival sensor, or the Elevator or the Subfloor classes.
 * 
 * Follow singleton design pattern to only allows instantiation of one Schedular object.
 * Satisfy the requirements where only one Schedular is needed in the system.
 * 
 * @author Amr Abdelazeem
 * @version 1.00
 */

public class Scheduler {

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

    /** Used to notify the Schedular that an elevator cas was detected.
     * Note: This method should only be used by an arrival sensor.
     *
     * @param floorNumber: The floor number where the elevator car was detected
     */
    public synchronized void notifySchedulerElevatorDetected(Integer floorNumber, int elevatorSubsystemCarID){
        //Notify the floors to reflect the new location that elevator car is approaching and going away
        notifyFloorSubSystemNodesElevatorDetected(elevatorSubsystemCarID, floorNumber);

        //Notify the elevator to reflect its location on the screen and if the elevator is supposed to stop on that floor reached then makes the motor stop moving,
        // the floor light turn off, and the doors open.
        ElevatorSubsystem elevatorCarDetected = getElevatorCarWithTheGivenID(elevatorSubsystemCarID);

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

        while (requestQueue.size() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
        Scheduler scheduler = new Scheduler();
        Thread elevatorThread, floorThread;
        elevatorThread = new Thread(new ElevatorSubsystem(scheduler));
        floorThread = new Thread(new FloorSubsystem(scheduler, "ElevatorCalls"));

        floorThread.start();
        elevatorThread.start();
    }
}



































































//     /** Used to notify the Schedular that an elevator call wes made.
//      * NOTE: This method should only be used by a SubFloorSubsystem node
//      *
//      * @param elevatorMovementDirection: The direction at which the elevator wil be moving, must be Up, Down
//      * @param floorNumber: The floor number where the elevator call was made
//      */
//     public synchronized void notifySchedulerElevatorCallButtonClicked(HashMap<String, object> callCommand){

//         //check the passed arguments is valid
//        List<String> validMovementDirections = Arrays.asList("up", "down");
//         if (!validMovementDirections.contains(elevatorMovementDirection.toLowerCase())){
//             System.out.println("ERROR::: invalid movement direction '" + elevatorMovementDirection + "' was given!");
//         }
//         else{
//             //TODO
//         }
//     }


//     public void analyzeRequests(){

//         //arrange the requests by time first then by direction
//         requests.sort(Comparator.comparing(Request::getTime).thenComparing(Request::getFloor).thenComparing(Request::getDirection));

//         //group the requests based on the floor elevator call, time proximity, and direction
//         // List<List<Request>> groupedRequests = new ArrayList<>();
//         // for (Request request : requests){
//         //     boolean added = false;
//         //     for (List<Request> group: groupedRequests){
//         //         Request lastRequest = group.get(group.size() - 1);
//         //         if(request.getFloor() == lastRequest.getFloor() &&
//         //             request.getDirection().equals(lastRequest.getDirection()) &&
//         //             Math.abs(request.getTime().toSecondOfDay() - lastRequest.time.toSecondOfDay()) <= 300) {
//         //                 group.add(request);
//         //                 added = true;
//         //                 break;
//         //         }
//         //     }
//         //     if (! added){
//         //         List<Request> newGroup = new ArrayList<>();
//         //         newGroup.add(request);
//         //         groupedRequests.add(newGroup);
//         //     }
//         // }

//         // Process requests and optimize for picking up passengers along the way
//         List<Request> servingRequests = new ArrayList<>();
//         for (Request request : requests) {
//             if (canAddToServingRequests(request, servingRequests)) {
//                 servingRequests.add(request);
//             } else {
//                 processServingRequests(servingRequests);
//                 servingRequests.clear();
//                 servingRequests.add(request);
//             }
//         }

//         // Process any remaining requests
//         processServingRequests(servingRequests);

//         //ProcessRequest
//         processRequest();
//     }

//     private boolean canAddToServingRequests(Request newRequest, List<Request> servingRequests) {
//         // Check if the new request can be added to the serving requests based on direction and floor
//         if (servingRequests.isEmpty()) {
//             return true;
//         }

//         Request lastRequest = servingRequests.get(servingRequests.size() - 1);
//         return newRequest.direction.equals(lastRequest.direction) &&
//                 ((newRequest.direction.equals("UP") && newRequest.floor >= lastRequest.floor) ||
//                  (newRequest.direction.equals("DOWN") && newRequest.floor <= lastRequest.floor));
//     }

//     private void processServingRequests(List<Request> servingRequests) {
//         // Process the serving requests (e.g., move the elevator, open/close doors)
//         for (Request request : servingRequests) {
//             System.out.println("Elevator serving request: " + request);
//         }
//     }
        


//     public void processRequest(){
//         Request firstRequest = requests.get(0);

//     }




//     /** Used to notify the Schedular that destination button(s) were clicked.
//      * Note: This method should only be used by an ElevatorSubsystem node
//      * 
//      * @param destinationButtonsClicked: The list of the buttons clicked
//      */
//     public synchronized void notifySchedulerDestinationButtonsClicked(List<Integer> destinationButtonsClicked){
//         //TODO
//     }
// }