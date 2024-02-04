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
    public ElevatorSubsystem(Scheduler scheduler) {
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
        System.out.println("[ELEVATOR] Opening and closing doors");
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
        System.out.println(String.format("[ELEVATOR] Moved to floor %d", currentFloor));
        //scheduler.notifySchedulerElevatorDetected(elevatorId, currentFloor);
    }
}
