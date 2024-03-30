public class FloorArrivalFaultElevatorCar extends ElevatorCar {
    private static final int MOVES_UNTIL_FAIL = 3;
    private int numMoves = 0;
    public FloorArrivalFaultElevatorCar(ElevatorSubsystem elevatorSubsystem) {
        super(elevatorSubsystem);
    }

    @Override
    public void move(int direction) {
        if (numMoves == MOVES_UNTIL_FAIL) {
            running = false; // Stop the elevator car after three moves
        } else {
            super.move(direction);
            numMoves += 1;
        }
    }
}
