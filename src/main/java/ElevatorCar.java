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
 * @author Adham Badawi
 * @version 1.0
*/
public class ElevatorCar implements Runnable{
    private ElevatorSubsystem elevatorSubsystem; //the owner elevatorSubsystem
    private static final int MOVE_TIME = 10000; //change to 1000 for testing purposes
    protected static final int DOOR_OPEN_TIME = 3000; //change to 300 for testing purposes
    protected static final int BOARDING_TIME = 5000; //change to 500 for testing purposes
    private static final int STARTING_FLOOR = 1;
    private static int elevatorCarIDCounter = 0;
    int elevatorCarID;
    protected int currentFloor = STARTING_FLOOR;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    protected ScheduledFuture<?> doorOperationFuture;
    protected int doorOperationRetryCount = 0;
    private static final int DOOR_FAULT_TIMEOUT = 5; // seconds
    private static final int MAX_DOOR_OPERATION_RETRIES = 3;
    protected boolean running = true;
    protected boolean doorOperationCompleted = false;
    protected boolean isInFaultMode = false; //Indicates if the car is in fault mode and should not proceed with normal operations
    protected boolean isTemporarilyDisabled = false;
    protected boolean isPermanentlyDisabled = false;
    private final Object lock = new Object(); // To handle synchronization

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

            if (!running) {
                return;
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
        // If the car is permanently disabled, do not proceed with any operation
        if (isPermanentlyDisabled) {
            System.out.println(String.format("[Elevator Car %d] is permanently disabled and cannot operate doors", getElevatorCarID()));
            return;
        }

        // Initiating door operation, only if not temporarily disabled
        if (!isTemporarilyDisabled) {
            startDoorOperationTimer();
        }

        doorOperationCompleted = false;
        long startTime = System.currentTimeMillis();

        System.out.println(String.format("[Elevator Car %d] reached target floor %d", elevatorCarID, currentFloor));
        System.out.println(String.format("[Elevator Car %d] Turn off floor %d light", elevatorCarID, currentFloor));
        try {
            System.out.println(String.format("[Elevator Car %d] Door opening", elevatorCarID));
            Thread.sleep(DOOR_OPEN_TIME);
            System.out.println(String.format("[Elevator Car %d] Door opened, now boarding passenger(s)", elevatorCarID));
            Thread.sleep(BOARDING_TIME);
            System.out.println(String.format("[Elevator Car %d] Passenger(s) boarded, now closing door", elevatorCarID));
            Thread.sleep(DOOR_OPEN_TIME);
            System.out.println(String.format("[Elevator Car %d] Doors closed", elevatorCarID));

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            if (duration <= 11500) { //Check if the operation took 11.5 seconds or less
                doorOperationCompleted = true; // Set flag to true as operation completed within expected time
                isTemporarilyDisabled = false;
                System.out.println(String.format("[Elevator Car %d] Door operation completed successfully in %d ms.", elevatorCarID, duration));

                // Operation successful - cancel the future and reset retry count
                doorOperationFuture.cancel(false);
                doorOperationRetryCount = 0;

            } else { //Operation took more than 11.5 seconds
                System.out.println(String.format("[Elevator Car %d] Door operation took too long (%d ms), which may indicate a fault.", elevatorCarID, duration));
            }

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
    public void startDoorOperationTimer() {
        Runnable faultDetectionTask = () -> {
            if (!doorOperationCompleted) {
                if (doorOperationRetryCount < MAX_DOOR_OPERATION_RETRIES) {
                    // Temporarily disable the car for a retry
                    temporarilyDisableCar();
                    System.out.println(String.format("[Elevator Car %d] Door operation took too long, attempting retry %d", getElevatorCarID(), doorOperationRetryCount + 1));

                    // Increment the retry count and attempt to toggle the doors again
                    doorOperationRetryCount++;
                    toggleDoors();
                } else {
                    // The retries have been exceeded, permanently disable the car
                    System.out.println(String.format("[Elevator Car %d] Door operation fault detected, exceeded retries", getElevatorCarID()));
                    notifySchedulerOfFault();
                }
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
    protected void notifySchedulerOfFault() {
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
    public void temporarilyDisableCar() {
        synchronized (lock) {
            isTemporarilyDisabled = true;
            notifySchedulerOfTemporaryDisable();
        }
        System.out.println(String.format("[Elevator Car %d] has been temporarily disabled.", getElevatorCarID()));
    }

    protected void notifySchedulerOfTemporaryDisable() {
        String disableMessage = "TEMP_DISABLE:" + getElevatorCarID();
        byte[] messageBytes = disableMessage.getBytes();

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress schedulerAddress = InetAddress.getLocalHost(); // Scheduler's IP address
            int schedulerPort = 100; // Scheduler's listening port for handling temporary disable messages

            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, schedulerAddress, schedulerPort);
            socket.send(packet);

            System.out.println(String.format("[Elevator Car %d] Sent temporary disable notification to Scheduler", getElevatorCarID()));
        } catch (Exception e) {
            System.err.println("Error sending temporary disable notification to Scheduler: " + e.getMessage());
        }
    }

    public void permanentlyDisableCar() {
        isTemporarilyDisabled = false;
        isPermanentlyDisabled = true;
        // Additional logic for permanent disable, such as notifying the scheduler
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