public class Main {
    private static final int NUM_ELEVATOR_CARS = 3;
    public static void main(String[] args) {
        Scheduler scheduler = Scheduler.getScheduler();
        //Elevator and floor threads
        Thread elevatorCarThread1, elevatorCarThread2, elevatorCarThread3, floorThread;
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem();
        FloorSubsystem floorSubSystem = new FloorSubsystem("ElevatorCalls");
        floorThread = new Thread(floorSubSystem);

        //Create elevator car threads
        Thread[] elevatorCarThreads = new Thread[NUM_ELEVATOR_CARS];
        for (int i = 0; i < NUM_ELEVATOR_CARS; i++) {
            elevatorCarThreads[i] = new Thread(new ElevatorCar(elevatorSubsystem));
        }

        //start the programs execution
        floorThread.start();

        //Start all elevator cars
        for (int i = 0; i < NUM_ELEVATOR_CARS; i++) {
            elevatorCarThreads[i].start();
        }
    }
}