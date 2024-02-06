public class Main {
    public static void main(String[] args) {
        Scheduler scheduler = Scheduler.getScheduler(null);

        //Elevator and floor threads
        Thread elevatorThread, floorThread;
        Elevator elevatorSubsystem = new Elevator(scheduler);
        FloorSubsystem floorSubSystem = new FloorSubsystem(scheduler, "ElevatorCalls");
        elevatorThread = new Thread(elevatorSubsystem);
        floorThread = new Thread(floorSubSystem);

        //register the elevator and floor systems to listen the scheduler
        scheduler.registerElevatorSubsystemNode(elevatorSubsystem);
        scheduler.registerSubFloorSubsystemNode(floorSubSystem);

        //start the programs execution
        floorThread.start();
        elevatorThread.start();
    }
}