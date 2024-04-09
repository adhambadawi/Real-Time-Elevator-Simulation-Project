/*
* @author Jaden Sutton
* @author Amr Abdelazeem
* @author Sameh Gawish
* @version 3.00
*/

import java.util.*;
import java.net.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

        if (elevatorCall == null) {
            //System.out.println("Attempted to add a null ElevatorCall to the queue.");
            return;
        }
        
        for (ElevatorCall elevatorCallIterator : context.getActiveTrips().values()) {
            System.out.println(elevatorCallIterator);
            if (elevatorCall != null && elevatorCallIterator.mergeRequest(elevatorCall)) {
                return; // Request merged, no need to add to the queue
            }
        }


        // Request not merged, add to the queue
        context.getRequestsQueue().add(elevatorCall);
        context.notifyAll(); 

        // After handling, transition state back to waiting for a request state
        
        context.setState("WaitingForRequest");
    }

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

        boolean activeTrip = context.getActiveTrips().get(elevatorId) != null && context.getActiveTrips().get(elevatorId).getNextTargetFloor() != null;

        if (!activeTrip && !context.assignTrip(elevatorId)) {
            //retrieving the original state for the Scheduler: WaitingForRequest state (IDLE)
            //trigging event (assigned action)
            context.setState("WaitingForRequest");
            if (context.isRequestsComplete()) {
                return ElevatorSubsystem.Action.QUIT;
            } else {
                return ElevatorSubsystem.Action.IDLE;
            }
        }

        ElevatorCall trip = context.getActiveTrips().get(elevatorId);

        int prevTargetFloor = trip.getNextTargetFloor();
        trip.setCurrentFloor(currentFloor);
        ElevatorSubsystem.Action action;

        if (currentFloor == prevTargetFloor) {
            action = ElevatorSubsystem.Action.TOGGLE_DOORS;
            trip.setTripStarted();
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
    private List<ElevatorCall> requestsQueue; // queue of pending elevator calls
    private Map<Integer, Integer> elevatorCarPositions;
    private Map<Integer, ElevatorCall> activeTrips;
    private DatagramSocket elevatorSendReceiveSocket, floorSendReceiveSocket, floorSendSocket;
    private Set<Integer> disabledElevatorCars = Collections.synchronizedSet(new HashSet<>());
    private int elevatorMoves = 0;
    private Date executionStart;



    //Singleton object
    private static Scheduler scheduler;

    //The scheduler is continuously active and can be in one of the following states:
    //1- WAITING_FOR_REQUESTS : a state where the Scheduler is in IDLE state
    //2- ASSIGNING_ACTIONS: a momentarily state where the scheduler is assigning an action (movement direction, toggle door, etc. ) to an elevator car
    //3- Adding_Actions: a momentarily state where the scheduler is adding requests to the elevator requests queue
    Map<String, SchedulerState> states;

    Scheduler() {
        requestsQueue = Collections.synchronizedList(new ArrayList<>());
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
            scheduler.listenToElevatorSubSystemCalls();
            scheduler.listenToFloorSubSystemCalls();
            scheduler.listenForFaultNotifications();
        }
        return scheduler;
    }
    
    public List<ElevatorCall> getRequestsQueue() {
        return requestsQueue;
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

    /**
     * Adding the request for the elevator call given to the suitable queue
     * 
     * @param elevatorCall: elevator request to be added
     */
    public synchronized void addRequest(ElevatorCall elevatorCall){
        //try to adding the coming request to an existing request
        System.out.println(String.format("[SCHEDULER] Received new elevator call: \n%s", elevatorCall));

        if (elevatorCall == null) {
            //System.out.println("Attempted to add a null ElevatorCall to the queue.");
            return;
        }
    
        //Delegate the task to the corresponding state 
        currentState.addRequest(this, elevatorCall);  
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


    /**
     * Assigns the next available elevator call to the specified elevator car, if the car is not disabled.
     * This method first checks if the elevator car is disabled and if not, it attempts to assign an available
     * trip from the request queue. If a trip is successfully assigned, it also attempts to merge similar
     * requests to optimize the trip.
     *
     * @param elevatorId The ID of the elevator car to which a trip might be assigned.
     * @return {@code true} if a trip was successfully assigned to the elevator car, {@code false} if no trip
     *         could be assigned either because the car is disabled, there are no pending requests, or some
     *         other condition prevents assignment.
     */
    public synchronized boolean assignTrip(int elevatorId) {
        if (disabledElevatorCars.contains(elevatorId)) {
            System.out.println("Elevator car " + elevatorId + " is disabled and cannot be assigned trips.");
            return false;
        }

        if (requestsQueue.isEmpty()) {
            return false;
        }

        ElevatorCall nextRequest = requestsQueue.remove(0);
        if (nextRequest == null) {
            System.out.println("No valid request to assign for elevator " + elevatorId);
            return false;
        }
        Integer currentPosition = elevatorCarPositions.get(elevatorId);
        if (currentPosition != null) {
            nextRequest.setCurrentFloor(currentPosition);
        } else {
            System.out.println("Current position for elevator " + elevatorId + " is undefined.");
        }

        List<ElevatorCall> merged = new ArrayList<>();
        for (ElevatorCall request : requestsQueue) {
            if (request != null && nextRequest.mergeRequest(request)) {
                merged.add(request);
            } else {
                System.out.println("Failed to merge request or request was null for elevator " + elevatorId);
            }
        }
        requestsQueue.removeAll(merged);
        activeTrips.put(elevatorId, nextRequest);
        System.out.println("Elevator car " + elevatorId + " got assigned the request: " + nextRequest);
        return true;
    }


    /**
     * a running thread to keep the class in always state of listening to the ElevatorSubsystem
     */
    private void listenToElevatorSubSystemCalls() {
        Thread ElevatorSubsystemListenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                listenToElevatorSubsystemRequests();
            }
        });
        ElevatorSubsystemListenerThread.start();
    }

    /**
     * a running thread to keep the class in always state of listening to the FloorSubsystem
     */
    private void listenToFloorSubSystemCalls() {
        Thread floorSubsystemListenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                listenToFloorSubsystemRequests();
            }
        });
        floorSubsystemListenerThread.start();
    }


    /**
     * creates the receiving socket and packet, decodes them and get the needed data from the scheduler class then sends
     * this data in a new packet to the same port
     */
    private void listenToElevatorSubsystemRequests() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        try {
            elevatorSendReceiveSocket = new DatagramSocket(69);
            //timeout if no calls received for one minute 
            elevatorSendReceiveSocket.setSoTimeout(60000);
            byte[] receiveData = new byte[Integer.BYTES * 2];

            while (true) {
                try{
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    elevatorSendReceiveSocket.receive(receivePacket);
                    ByteBuffer byteBuffer = ByteBuffer.wrap(receivePacket.getData());
                    int elevatorId = byteBuffer.getInt();
                    int currentFloor = byteBuffer.getInt();

                    SendDisplayInfoToFloorSubsystem(elevatorId, currentFloor);

                    ElevatorSubsystem.Action action;
                    if (disabledElevatorCars.contains(elevatorId)) {
                        action = ElevatorSubsystem.Action.QUIT;
                    } else {
                        action = getNextAction(elevatorId, currentFloor);
                    }

                    byte[] sendData = action.name().getBytes("UTF-8");

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
                    elevatorSendReceiveSocket.send(sendPacket);

                    int direction = 0;
                    switch (action) {
                        case UP:
                            direction = 1;
                            elevatorMoves++;
                            break;
                        case DOWN:
                            direction = -1;
                            elevatorMoves++;
                            break;
                    }

                    if (direction != 0) {
                        final int expectedFloor = currentFloor + direction;
                        // Change to 200 for testing purposes
                        executor.schedule(() -> verifyElevatorCarArrival(elevatorId, expectedFloor), ElevatorCar.getMoveTime() + 2000, TimeUnit.MILLISECONDS); // Schedule method to verify the arrival of the elevator car at the desired floor within 2s of the expected time
                    }

                    // Resetting the timeout
                    elevatorSendReceiveSocket.setSoTimeout(60000);
                }
                catch (SocketTimeoutException e){
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Ensure the socket is closed to release resources
            if (elevatorSendReceiveSocket != null && !elevatorSendReceiveSocket.isClosed()) {
                elevatorSendReceiveSocket.close();
                executor.shutdown();
            }

            System.out.println("Total elevator moves: " + elevatorMoves);
        }
    }

    /**
     * creates the receiving socket and packet, decodes them and get the needed data from the scheduler class then sends
     * this data in a new packet to the same port
     */
    private void listenToFloorSubsystemRequests() {
        try {
                floorSendReceiveSocket = new DatagramSocket(23);
                //timeout if no calls received for two minute 
                floorSendReceiveSocket.setSoTimeout(120000);
                byte[] receiveData = new byte[Integer.BYTES * 10];
                
                while (true) {
                    try{
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    floorSendReceiveSocket.receive(receivePacket);
                    ByteBuffer byteBuffer = ByteBuffer.wrap(receivePacket.getData());
                    //Decode the timestamp
                    int hours = byteBuffer.getInt();
                    int minutes = byteBuffer.getInt();
                    int seconds = byteBuffer.getInt();
                    //create a Date object of the decoded time
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hours);
                    calendar.set(Calendar.MINUTE, minutes);
                    calendar.set(Calendar.SECOND, seconds);
                    Date timestamp = calendar.getTime();

                    //Decode the starting floor, direction, and target floor
                    int startingFloor = byteBuffer.getInt();
                    int targetFloor = byteBuffer.getInt();
                    int directionDecoded = byteBuffer.getInt();
                    String direction = (directionDecoded == 1) ? "Up" : "Down";
                    ElevatorCall elevatorCall = new ElevatorCall(timestamp, startingFloor, targetFloor, direction);

                    this.addRequest(elevatorCall);
                    // Resetting the timeout
                    floorSendReceiveSocket.setSoTimeout(120000);
                }
                catch (SocketTimeoutException e) {
                //signal that no more requests available 
                this.signalRequestsComplete();
                break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Ensure the socket is closed to release resources
            if (floorSendReceiveSocket != null && !floorSendReceiveSocket.isClosed()) {
                floorSendReceiveSocket.close();
            }
        }
        requestsComplete = true;
    }

    public void SendDisplayInfoToFloorSubsystem(int elevatorCarID, int currentFloor) {
        try {
            floorSendSocket = new DatagramSocket();
            floorSendSocket.setSoTimeout(60000);
        } catch (SocketException se) {
            se.printStackTrace();
        }
        byte[] sendData; // data to be sent

        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES * 2);
            byteBuffer.putInt(elevatorCarID);
            byteBuffer.putInt(currentFloor);
            sendData = byteBuffer.array();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getLocalHost(), 80);

            floorSendSocket.send(sendPacket);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Ensure the socket is closed to release resources
            if (floorSendSocket != null && !floorSendSocket.isClosed()) {
                floorSendSocket.close();
            }
        }
    }

    /**
     * Starts a background thread that listens for fault notifications on a specific UDP port.
     * This method creates a socket to listen for incoming fault messages from elevator cars indicating
     * issues such as door faults. Upon receiving a fault message, it handles the message by calling
     * {@code handleFaultMessage}.
     *
     * This listener runs in a separate thread to avoid blocking the main application flow and continues
     * listening until the thread is interrupted.
     */
    private void listenForFaultNotifications() {
        Thread faultListenerThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(100)) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (!Thread.currentThread().isInterrupted()) {
                    socket.receive(packet);
                    String faultMessage = new String(packet.getData(), 0, packet.getLength());
                    handleFaultMessage(faultMessage);
                }
            } catch (SocketException e) {
                System.err.println("SocketException in fault listener: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("IOException in fault listener: " + e.getMessage());
            }
        });
        faultListenerThread.start();
    }

    /**
     * Handles incoming fault messages from elevator cars. This method is called when a fault message
     * is received, indicating a problem such as a door fault with an elevator car. The message format
     * is expected to be "DOOR_FAULT:{elevatorId}", where {elevatorId} is the ID of the elevator car with
     * the fault.
     *
     * Upon receiving a fault message, this method marks the specified elevator car as disabled, preventing
     * it from being assigned new trips, and reassigns any active trip to another elevator.
     *
     * @param faultMessage The received fault message in the format "DOOR_FAULT:{elevatorId}".
     */
    private void handleFaultMessage(String faultMessage) {
        System.out.println("Received fault message: " + faultMessage);
        // Example fault message format: "DOOR_FAULT:3"
        if (faultMessage.startsWith("PERM_DISABLE:")) {
            int elevatorId = Integer.parseInt(faultMessage.split(":")[1]);
            disabledElevatorCars.add(elevatorId);
            requestsQueue.add(activeTrips.remove(elevatorId));// Add the active trip back to queue so that it can be serviced by another elevator
            System.out.println("Elevator car " + elevatorId + " has been permanently disabled due to a door fault.");
        } else if (faultMessage.startsWith("TEMP_DISABLE:")) {
            int elevatorId = Integer.parseInt(faultMessage.split(":")[1]);
            disabledElevatorCars.add(elevatorId);
            requestsQueue.add(activeTrips.remove(elevatorId));// Add the active trip back to queue so that it can be serviced by another elevator
            System.out.println("Elevator car " + elevatorId + " has been temporarily disabled due to a potential door fault.");
        }
    }


    /**
     * Check that the elevator has reached the next floor within the expected time, output a fault and remove the car from service if so
     * @param elevatorId The ID of the elevator to chceck
     * @param expectedFloor The floor the elevator should be on
     */
    private void verifyElevatorCarArrival(int elevatorId, int expectedFloor) {
        if (elevatorCarPositions.get(elevatorId) == expectedFloor) {
            return;
        }

        System.out.println(String.format("[SCHEDULER] FAULT DETECTED: Elevator car %d failed to arrive at floor %d", elevatorId, expectedFloor));
        disabledElevatorCars.add(elevatorId);
        requestsQueue.add(activeTrips.remove(elevatorId));  // Add the active trip back to queue so that it can be serviced by another elevator
    }
}