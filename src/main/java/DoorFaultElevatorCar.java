public class DoorFaultElevatorCar extends ElevatorCar {

    public DoorFaultElevatorCar(ElevatorSubsystem elevatorSubsystem) {
        super(elevatorSubsystem);
    }

    @Override
    public void toggleDoors() {
        System.out.println(String.format("[Faulty Elevator Car %d] Simulating stuck door at floor %d", getElevatorCarID(), getCurrentFloor()));

        running = false;
        notifySchedulerOfFault();

    }

    protected void notifySchedulerOfFault() {
        super.notifySchedulerOfFault();

        System.out.println(String.format("[Faulty Elevator Car %d] Door fault notification sent to Scheduler", getElevatorCarID()));
    }
}
