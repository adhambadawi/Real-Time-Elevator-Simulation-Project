/**
 * Class represents the elevator car that is moving inside the shaft
 * 
 * @author Jaden Sutton
 * @author Adham Badawi
 * @version 1.00
 */

public class ElevatorSubsystem implements Runnable {
    public enum Action { UP, DOWN, TOGGLE_DOORS, QUIT }
    private static final int MOVE_TIME = 8006;
    private static final int DOOR_OPEN_TIME = 3238;
    private static final int STARTING_FLOOR = 1;

    private Scheduler scheduler;
    private static int elevatorIdCounter = 0;
    private int elevatorId;
    private int currentFloor = STARTING_FLOOR;

    /**
     * Constructor for ElevatorSubsystem thread
     * @param scheduler The elevator scheduler
     */
    public ElevatorSubsystem(Scheduler scheduler) {
        elevatorId = elevatorIdCounter++;
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        Action action = scheduler.getNextAction(elevatorId, currentFloor);
        while (action != Action.QUIT) {
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

            action = scheduler.getNextAction(elevatorId, currentFloor);
        }
    }

    public void toggleDoors() {
        System.out.println(String.format("[ELEVATOR] Opening and closing doors at floor %d", currentFloor));
        System.out.println("Turn off floor light: " + currentFloor);
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
        try {
            Thread.sleep(MOVE_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        currentFloor += direction;
        System.out.println(String.format("[ELEVATOR] Moved to floor %d", currentFloor));
    }
}
