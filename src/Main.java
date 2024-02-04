public class Main {
    public static void main(String[] args) {
        Scheduler scheduler = Scheduler.getScheduler(null);

        //Elevator and floor threads
        Thread elevatorThread, floorThread;
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(scheduler);
        FloorSubsystem floorSubSystem = new FloorSubsystem(scheduler, "ElevatorCalls");


        elevatorThread = new Thread(elevatorSubsystem);
        floorThread = new Thread(floorSubSystem);

        floorThread.start();
        elevatorThread.start();
    }
}