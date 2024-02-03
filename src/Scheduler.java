import java.time.LocalTime;
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

    private List<Runnable>  subFloorSubsystemNodes; //Represents the list of subFloorSubsystem threads.
    private List<Runnable>  elevatorSubsystemNodes; //Represents the list of elevatorSubsystem threads.
    private List<Request> requests; // list of the elevator requests

    //Singleton object 
     private static Scheduler scheduler;
    

    private Scheduler() {
        subFloorSubsystemNodes = new ArrayList<>();
        elevatorSubsystemNodes = new ArrayList<>();
        requests = new ArrayList<>();
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

    public void addRequest(Request request){
        requests.add(request);
    }

    /** Used to register SubFloorSubsystem nodes (floors) to the SubFloorSubsystemNodes arraylist
     *  Uses dependency injection to avoid circular dependency
     *
     * @param SubFloorSubsystemNode: SubFloorSubsystem node to be added
     */
    public void registerSubFloorSubsystemNode(Runnable subFloorSubsystemNode){

        //ensure the node getting registered is of type SubFloorSubsystemNode
        if (subFloorSubsystemNode instanceof FloorSubsystem){
            subFloorSubsystemNodes.add(subFloorSubsystemNode);
        }
        else{
            System.out.println("The Object trying to be registered is of type: " + subFloorSubsystemNode.getClass() + "\nAllowed object type is 'SubFloorSubsystem'");
        }
    }

    /** Used to register ElevatorSubsystem nodes (elevator cars) to the elevatorSubsystemNodes arraylist
     *  Uses dependency injection to avoid circular dependency
     *
     * @param elevatorSubsystemNode: elevatorSubsystem node to be added
     */
    public void registerElevatorSubsystemNode(Thread elevatorSubsystemNode){

        //ensure the node getting registered is of type SubFloorSubsystemNode
        if (elevatorSubsystemNode instanceof ElevatorSubsystem){
            elevatorSubsystemNodes.add(elevatorSubsystemNode);
        }
        else{
            System.out.println("The Object trying to be registered is of type: " + elevatorSubsystemNode.getClass() + "\nAllowed object type is 'ElevatorSubsystem'");
        }
    }

    /** Used to notify the Schedular that an elevator cas was detected.
     * Note: This method should only be used by an arrival sensor.
     *
     * @param floorNumber: The floor number where the elevator car was detected
     */
    public synchronized void notifySchedulerElevatorDetected(Integer floorNumber, int elevatorSubsystemCarID){
        //Notify the floors to reflect the new location that elevator car is approaching and going away
        notifyFloorSubSystemNodesElevatorDetected(floorNumber);

        //Notify the elevator to reflect its location on the screen and if the elevator is supposed to stop on that floor reached then makes the motor stop moving,
        // the floor light turn off, and the doors open.
        ElevatorSubsystem elevatorCarDetected = getElevatorCarWithTheGivenID(elevatorSubsystemCarID);
        notifyElevatorWithFloorDetected(floorNumber, elevatorCarDetected);
    }

    /**
    * Get the elevator car with ID provided
    * @param elevatorSubsystemCarID: the elevator car ID
    * @return ElevatorSubsystem elevatorCar: the elevator car with id given.
    */
    private ElevatorSubsystem getElevatorCarWithTheGivenID(int elevatorSubsystemCarID){

        ElevatorSubsystem targetedElevatorCar = null;

        for (Runnable elevatorCar: elevatorSubsystemNodes){
            if (elevatorCar.getElevatorCarID().equals(elevatorSubsystemCarID)){
                targetedElevatorCar = elevatorCar;
                break;
            }
        }
        //Make sure that the elevator with the given id was found
        if (targetedElevatorCar = null){
            System.out.println("ERROR::: ElevatorCar with the given ID: " + elevatorSubsystemCarID + " does not exit!");
            return null;
        }
        return targetedElevatorCar;
    }

    /** Notify the list of subFloorSubsystemNodes with the location of the elevator car.
     * 
     * @param floorNumber: The floor number where the elevator car was detected
     */
    private void notifyFloorSubSystemNodesElevatorDetected(int floorNumber){
        for (Thread floor : subFloorSubsystemNodes){
            floor.notifyWithTheDetectedElevatorPosition(floorNumber);
        }
    }

    /** Notify the elevator car that was detected with the floor it got detected at.
     * 
     * @param elevatorCarDetected: The elevator car detected
     * @param floorNumber: The floor number where the elevator car was detected
     */
    private void notifyElevatorWithFloorDetected(int floorNumber, ElevatorSubsystem elevatorCarDetected){
            elevatorCarDetected.notifyWithTheDetectedPosition(floorNumber);
    }


    /** Used to notify the Schedular that an elevator call wes made.
     * NOTE: This method should only be used by a SubFloorSubsystem node
     *
     * @param elevatorMovementDirection: The direction at which the elevator wil be moving, must be Up, Down
     * @param floorNumber: The floor number where the elevator call was made
     */
    public synchronized void notifySchedulerElevatorCallButtonClicked(HashMap<String, object> callCommand){

        //check the passed arguments is valid
       List<String> validMovementDirections = Arrays.asList("up", "down");
        if (!validMovementDirections.contains(elevatorMovementDirection.toLowerCase())){
            System.out.println("ERROR::: invalid movement direction '" + elevatorMovementDirection + "' was given!");
        }
        else{
            //TODO
        }
    }


    public void analyzeRequests(){

        //arrange the requests by time first then by direction
        requests.sort(Comparator.comparing(Request::getTime).thenComparing(Request::getDirection));

        //group the requests based on the floor elevator call, time proximity, and direction
        List<List<Request>> groupedRequests = new ArrayList<>();
        for (Request request : requests){
            boolean added = false;
            for (List<Request> group: groupedRequests){
                Request lastRequest = group.get(group.size() - 1);
                if(request.getFloor() == lastRequest.getFloor() &&
                    request.getDirection().equals(lastRequest.getDirection()) &&
                    Math.abs(request.getTime().toSecondOfDay() - lastRequest.time.toSecondOfDay()) <= 300) {
                        group.add(request);
                        added = true;
                        break;
                }
            }
            if (! added){
                List<Request> newGroup = new ArrayList<>();
                newGroup.add(request);
                groupedRequests.add(newGroup);
            }
        }

        //ProcessRequest
        processRequest();
    }
        


    public void processRequest(){
        Request firstRequest = requests.get(0);
        
    }




    /** Used to notify the Schedular that destination button(s) were clicked.
     * Note: This method should only be used by an ElevatorSubsystem node
     * 
     * @param destinationButtonsClicked: The list of the buttons clicked
     */
    public synchronized void notifySchedulerDestinationButtonsClicked(List<Integer> destinationButtonsClicked){
        //TODO
    }
}