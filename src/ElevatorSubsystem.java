/**
 * @author Adham Badawi
 */

public class ElevatorSubsystem implements Runnable{

    private Scheduler scheduler;
    private int elevatorId;
    private int currentFloor;
    
    
    
    
    /**
     * Constructor for ElevatorSubsystem thread
     * @param scheduler The elevator scheduler
     */
    public void ElevatorSubsystemystem(Scheduler scheduler) {
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

    @Override
    public void run() {

    }
}
