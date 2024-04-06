public class DoorFaultElevatorCar extends ElevatorCar {

    public DoorFaultElevatorCar(ElevatorSubsystem elevatorSubsystem) {
        super(elevatorSubsystem);
    }

    @Override
    public void toggleDoors() {
        // Initiating door operation
        startDoorOperationTimer();
        doorOperationCompleted = false;
        long startTime = System.currentTimeMillis();

        System.out.println(String.format("[Elevator Car %d] reached target floor %d", elevatorCarID, currentFloor));
        System.out.println(String.format("[Elevator Car %d] Turn off floor %d light", elevatorCarID, currentFloor));
        try {
            System.out.println(String.format("[Elevator Car %d] Door opening", elevatorCarID));
            Thread.sleep(DOOR_OPEN_TIME*4);
            System.out.println(String.format("[Elevator Car %d] Door opened, now boarding passenger(s)", elevatorCarID));
            Thread.sleep(BOARDING_TIME);
            System.out.println(String.format("Passenger(s) boarded in [Elevator Car %d], now closing door", elevatorCarID));
            Thread.sleep(DOOR_OPEN_TIME*4);
            System.out.println(String.format("[Elevator Car %d] Doors closed", elevatorCarID));

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            if (duration <= 11000) { //Check if the operation took 11.5 seconds or less
                doorOperationCompleted = true; // Set flag to true as operation completed within expected time
                System.out.println(String.format("[Elevator Car %d] Door operation completed successfully in %d ms.", elevatorCarID, duration));

                // Operation successful - cancel the future and reset retry count
                doorOperationFuture.cancel(false);
                doorOperationRetryCount = 0;

            } else { //Operation took more than 11.5 seconds
                System.out.println(String.format("[Elevator Car %d] Door operation took too long (%d ms), which may indicate a fault.", elevatorCarID, duration));
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void notifySchedulerOfFault() {
        super.notifySchedulerOfFault();

        System.out.println(String.format("[Faulty Elevator Car %d] Door fault notification sent to Scheduler", getElevatorCarID()));
    }
}
