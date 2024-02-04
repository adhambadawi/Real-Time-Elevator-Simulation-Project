/**
 * Class represents the elevator car that is moving inside the shaft
 * 
 * @author Jaden Sutton
 * @author Adham Badawi
 * @version 1.00
 */

public class ElevatorSubsystem implements Runnable{
    private static final int MOVE_TIME = 8006;
    private static final int DOOR_OPEN_TIME = 3238;

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
        boolean availableTrips = true;
        while (availableTrips) {
            currentTrip = scheduler.getNextTrip(this);
            if (currentTrip == null){
                availableTrips = false;
                break;
            }
            while (currentTrip.getNextTargetFloor() != null) {
                if (currentFloor < currentTrip.getNextTargetFloor()) {
                    move(1);
                } else if (currentFloor > currentTrip.getNextTargetFloor()) {
                    move(-1);
                } else {
                    toggleDoors();
                    currentTrip.arrivedAtFloor();
                }
            }
        }
    }

    public void toggleDoors() {
        System.out.println(String.format("[ELEVATOR] Opening and closing doors at floor %d", currentFloor));
        try {
            Thread.sleep(DOOR_OPEN_TIME);
            System.out.println("[ELEVATOR] Doors open");
            Thread.sleep(DOOR_OPEN_TIME);
            System.out.println("[ELEVATOR] Doors closed");
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
