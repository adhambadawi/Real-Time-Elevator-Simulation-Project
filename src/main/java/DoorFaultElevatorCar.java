public class DoorFaultElevatorCar extends ElevatorCar {

    public DoorFaultElevatorCar(ElevatorSubsystem elevatorSubsystem) {
        super(elevatorSubsystem);
    }

    @Override
    // Directly manipulates door operation times for fault simulation
    public void toggleDoors() {
        System.out.println(String.format("[Elevator Car %d] toggleDoors called, temporarilyDisabled: %s, permanentlyDisabled: %s", getElevatorCarID(), isTemporarilyDisabled, isPermanentlyDisabled));
        if (isPermanentlyDisabled) {
            System.out.println(String.format("[Elevator Car %d] is permanently disabled and cannot operate doors", getElevatorCarID()));
            return;
        }

        if (!isTemporarilyDisabled) {
            System.out.println(String.format("[Elevator Car %d] initiating fault simulation for doors", getElevatorCarID()));
            startDoorOperationTimer();
        }

        doorOperationCompleted = false;
        long startTime = System.currentTimeMillis();

        // Simulate an extended door operation time to force a fault condition
        try {
            System.out.println(String.format("[Elevator Car %d] Door opening (fault simulated)", getElevatorCarID()));
            Thread.sleep(DOOR_OPEN_TIME * 10);  // Intentionally long sleep to simulate a fault

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            if (duration > 12000) {  // This condition should always be true in fault simulation
                System.out.println(String.format("[Elevator Car %d] Door operation took too long (%d ms), simulating a fault.", getElevatorCarID(), duration));
                if (isTemporarilyDisabled) {
                    handleRetryAfterFault();
                }
            } else {
                doorOperationCompleted = true; // This line should theoretically never be reached in this subclass
                doorOperationFuture.cancel(false);
                doorOperationRetryCount = 0;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during simulated fault operation", e);
        }
    }
}
