/**Class represents an elevator car under the elevator subsystem
 * 
 * @author Amr Abdelazeem
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

    public ElevatorCar(ElevatorSubsystem elevatorSubsystem){
        //this is considered the owner elevatorSubsystem under which an instantiated
        // elevator car object is registered.
        this.elevatorSubsystem = elevatorSubsystem;
        this.elevatorCarID = elevatorCarIDCounter++; //elevator (shaft) ID
        //register elevatorCar to the elevatorSubsystem
        this.elevatorSubsystem.registerElevatorCar(this);
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
            }
        }
    }

    public void toggleDoors() {
        System.out.println(String.format("[Elevator Car %d] Opening and closing doors at floor %d", elevatorCarID, currentFloor));
        System.out.println(String.format("[Elevator Car %d] Turn off floor %d light", elevatorCarID, currentFloor));
        try {
            Thread.sleep(DOOR_OPEN_TIME);
            System.out.println(String.format("[Elevator Car %d] Doors open", elevatorCarID));
            Thread.sleep(DOOR_OPEN_TIME);
            System.out.println(String.format("[Elevator Car %d] Doors closed", elevatorCarID));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
}