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



    /** Notify the elevator car that was detected with the floor it got detected at.
     * 
     * @param elevatorCarDetected: The elevator car detected
     * @param floorNumber: The floor number where the elevator car was detected
     */
    private void notifyElevatorWithFloorDetected(int floorNumber, ElevatorSubsystem elevatorCarDetected){
        elevatorCarDetected.notifyWithTheDetectedPosition(floorNumber);
}
}
