/**
 * @author Adham Badawi
 */

public class ElevatorSubsystem {

    private Scheduler scheduler;
    private int elevatorId;
    private int currentFloor;
    
    
    
    
    /**
     * Constructor for ElevatorSubsystem thread
     * @param scheduler The elevator scheduler
     */
    public ElevatorSubsystemystem(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    public int getElevatorId() {
        return elevatorId;
    }
    
    public int getCurrentFloor() {
        return currentFloor;
    }
    
    
    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }
}
