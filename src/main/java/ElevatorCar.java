import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**Class represents an elevator car under the elevator subsystem
 * 
 * @author Amr Abdelazeem
 * @author Jaden Sutton
 * @version 1.0
*/
public class ElevatorCar implements Runnable{
    private ElevatorSubsystem elevatorSubsystem; //the owner elevatorSubsystem
    private static final int MOVE_TIME = 8006;
    protected static final int DOOR_OPEN_TIME = 3238;
    private static final int STARTING_FLOOR = 1;
    private static int elevatorCarIDCounter = 0;
    private int elevatorCarID;
    private int currentFloor = STARTING_FLOOR;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> doorOperationFuture;
    private int doorOperationRetryCount = 0;
    private static final int DOOR_FAULT_TIMEOUT = 5; // seconds
    private static final int MAX_DOOR_OPERATION_RETRIES = 3;

    public ElevatorCar(ElevatorSubsystem elevatorSubsystem){
        //this is considered the owner elevatorSubsystem under which an instantiated
        // elevator car object is registered.
        this.elevatorSubsystem = elevatorSubsystem;
        this.elevatorCarID = elevatorCarIDCounter++; //elevator (shaft) ID
        //register elevatorCar to the elevatorSubsystem
        this.elevatorSubsystem.registerElevatorCar(this);
    }

    //Getters and setters
    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) { //only for testing purposes
        this.currentFloor = currentFloor;
    }

    public int getElevatorCarID() {
        return elevatorCarID;
    }

    public static int getMoveTime() {
        return MOVE_TIME;
    }

    @Override
    public void run(){
        ElevatorSubsystem.Action action = elevatorSubsystem.getAction(this.elevatorCarID);
        while (action != ElevatorSubsystem.Action.QUIT) {
            switch (action) {
                case UP:
                    move(1);
                    break;
                case DOWN:
                    move(-1);
                    break;
                case TOGGLE_DOORS:
                    toggleDoors();
                    break;
                case IDLE:
                    try {
                        // Sleep for 5 seconds before requesting again to conserve bandwidth
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }
            action = elevatorSubsystem.getAction(this.elevatorCarID);
        }
    }

    /**
     * Simulates the opening and closing of elevator doors. This method initiates a door operation timer to detect faults.
     * If the doors do not open or close within the expected timeframe, it attempts to retry the operation up to a maximum number of retries.
     * If the operation exceeds the retry limit, it notifies the scheduler of a persistent fault and takes further action.
     *
     * @throws RuntimeException - if the thread is interrupted while waiting for the door operation to complete.
     */
    public void toggleDoors() {
        // Initiating door operation
        startDoorOperationTimer();

        System.out.println(String.format("[Elevator Car %d] Opening and closing doors at floor %d", elevatorCarID, currentFloor));
        System.out.println(String.format("[Elevator Car %d] Turn off floor %d light", elevatorCarID, currentFloor));
        try {
            Thread.sleep(DOOR_OPEN_TIME);
            System.out.println(String.format("[Elevator Car %d] Doors open", elevatorCarID));
            Thread.sleep(DOOR_OPEN_TIME);
            System.out.println(String.format("[Elevator Car %d] Doors closed", elevatorCarID));

            // Cancel the timer if the operation was completed within the expected timeframe
            doorOperationFuture.cancel(false);
            doorOperationRetryCount = 0; // Reset retry count after successful operation
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts a timer to detect door operation faults. If the door operation takes longer than a specified timeout, it retries the operation.
     * After a certain number of retries, if the door still hasn't operated correctly, it notifies the scheduler of a persistent fault and takes further action.
     *
     * This method schedules a fault detection task that runs after a predefined timeout period to handle potential door operation faults.
     */
    private void startDoorOperationTimer() {
        Runnable faultDetectionTask = () -> {
            if (doorOperationRetryCount < MAX_DOOR_OPERATION_RETRIES) {
                System.out.println(String.format("[Elevator Car %d] Door operation took too long, attempting retry %d", elevatorCarID, doorOperationRetryCount + 1));
                doorOperationRetryCount++;
                toggleDoors(); // Retry door operation since it is a  transient fault. Handle the situation gracefully (unlike the LRT ;) )
            } else {
                System.out.println(String.format("[Elevator Car %d] Door operation fault detected, exceeded retries", elevatorCarID));

                notifySchedulerOfFault(); // Notify Scheduler of the persistent fault
                System.out.println(String.format("Sending Engineer to Elevator Car %d", elevatorCarID));
            }
        };

        doorOperationFuture = scheduler.schedule(faultDetectionTask, DOOR_FAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * Notifies the Scheduler of a door operation fault by sending a fault message via UDP. This method constructs a fault message and sends it to the Scheduler's listening port.
     *
     * The fault message format is "DOOR_FAULT:{elevatorCarID}", where {elevatorCarID} is the ID of the elevator car experiencing the fault.
     *
     * @throws Exception if there is an error sending the fault notification to the Scheduler.
     */
    private void notifySchedulerOfFault() {
        String faultMessage = "DOOR_FAULT:" + elevatorCarID;
        byte[] messageBytes = faultMessage.getBytes();

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress schedulerAddress = InetAddress.getLocalHost();
            int schedulerPort = 100;

            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, schedulerAddress, schedulerPort);
            socket.send(packet);

            System.out.println(String.format("[Elevator Car %d] Sent door fault notification to Scheduler", elevatorCarID));
        } catch (Exception e) {
            System.err.println("Error sending door fault notification to Scheduler: " + e.getMessage());
        }
    }


    public void move(int direction) {
        try {
            Thread.sleep(MOVE_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        currentFloor += direction;
        System.out.println(String.format("[Elevator Car %d] Moved to floor %d", elevatorCarID, currentFloor));
    }
}