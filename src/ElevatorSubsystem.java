/**
 * @author Adham Badawi
 */

public class ElevatorSubsystem implements Runnable{
    private static final int MOVE_TIME = 8000;
    private static final int DOOR_TIME = 4000;

    private Scheduler scheduler;
    private static int elevatorIdCounter = 0;
    private int elevatorId;
    private int currentFloor;
    private ElevatorCall currentTrip;

    /**
     * Constructor for ElevatorSubsystem thread
     * @param scheduler The elevator scheduler
     */
    public void ElevatorSubsystemystem(Scheduler scheduler) {
        elevatorId = elevatorIdCounter++;
        this.scheduler = scheduler;
        currentFloor = 1;
        currentTrip = null;
    }
    
    public int getElevatorId() {
        return elevatorId;
    }
    
    public int getCurrentFloor() {
        return currentFloor;
    }

    public ElevatorCall getCurrentTrip() {return currentTrip; }
    
    
    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    @Override
    public void run() {
        // This is temporary, replace while(true) with flag
        while (true) {
            currentTrip = scheduler.getNextTrip(this);
            while (currentTrip.getNextFloor() != null) {
                if (currentFloor < currentTrip.getNextFloor()) {
                    move(1);
                } else if (currentFloor > currentTrip.getNextFloor()) {
                    move(-1);
                } else {
                    toggleDoors();
                    currentTrip.arrivedAtFloor();
                }
            }
        }
    }

    public void toggleDoors() {
        try {
            Thread.sleep(2 * DOOR_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void move(int direction) {
        currentFloor += direction;
        try {
            Thread.sleep(MOVE_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        scheduler.notifySchedulerElevatorDetected(elevatorId, currentFloor);
    }



    /** Notify the elevator car that was detected with the floor it got detected at.
     * 
     * @param elevatorCarDetected: The elevator car detected
     * @param floorNumber: The floor number where the elevator car was detected
     */
    private void notifyElevatorWithFloorDetected(int floorNumber, ElevatorSubsystem elevatorCarDetected){
        elevatorCarDetected.notifyWithTheDetectedPosition(floorNumber);
}
}
