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

    private List<Thread>  subFloorSubsystemNodes = new ArrayList<>(); //Represents the list of subFloorSubsystem threads.
    private List<Thread>  elevatorSubsystemNodes = new ArrayList<>(); //Represents the list of elevatorSubsystem threads.
    private List<ArrivalSensor> arrivalSensors = new ArrayList<>(); //Represent the list of the arrival sensors.

    //Singleton object 
     private static Scheduler scheduler;
    

    private Scheduler() {
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
     * @param SubFloorSubsystemNode: SubFloorSubsystem node to be added
     */
    public void registerSubFloorSubsystemNode(Thread subFloorSubsystemNode){

        //ensure the node getting registered is of type SubFloorSubsystemNode
        if (subFloorSubsystemNode instanceof SubFloorSubsystem){
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

    /** Used to notify the Schedular that an elevator call wes made.
     * NOTE: This method should only be used by a SubFloorSubsystem node
     *
     * @param elevatorMovementDirection: The direction at which the elevator wil be moving, must be Up, Down
     * @param floorNumber: The floor number where the elevator call was made
     */
    public synchronized void notifySchedulerElevatorCallButtonClicked(String elevatorMovementDirection, int floorNumber){

        //check the passed arguments is valid
       List<String> validMovementDirections = Arrays.asList("up", "down");
        if (!validMovementDirections.contains(elevatorMovementDirection.toLowerCase())){
            System.out.println("ERROR::: invalid movement direction '" + elevatorMovementDirection + "' was given!");
        }
        else{
            //TODO
        }
    }

    /** Used to notify the Schedular that an elevator cas was detected.
     * Note: This method should only be used by an arrival sensor.
     *
     * @param floorNumber: The floor number where the elevator car was detected
     */
    public synchronized void notifySchedulerElevatorDetected(int floorNumber){
        //TODO
    }

    /** Used to notify the Schedular that destination button(s) were clicked.
     * Note: This method should only be used by an ElevatorSubsystem node
     * 
     * @param destinationButtonsClicked: The list of the buttons clicked
     */
    public synchronized void notifySchedulerDestinationButtonsClicked(List<Integer> destinationButtonsClicked){
        //TODO
    }

    /** Notify the list of subFloorSubsystemNodes with the location of the elevator car.
     * 
     * @param floorNumber: The floor number where the elevator car was detected
     */
    public void notifySubFloorSubSystemNodesWithElevatorDetectedFloor(int floorNumber){
        for (Thread floor : subFloorSubsystemNodes){
            floor.notifyWithTheDetectedElevatorPosition(floorNumber); //TODO
        }
    }

    /** Notify the elevator car that was detected with the floor it got detected at.
     * 
     * @param elevatorCarDetected: The elevator car detected
     * @param floorNumber: The floor number where the elevator car was detected
     */
    public void notifyElevatorWithFloorDetected(int floorNumber, ElevatorSubsystem elevatorCarDetected){
            elevatorCarDetected.notifyWithTheDetectedPosition(floorNumber); //TODO
    }
}