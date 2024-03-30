public class Main {
    private static final int NUM_ELEVATOR_CARS = 1;
    private static final int NUM_FLOOR_ARRIVAL_FAULT_CARS = 1;
    private static final int NUM_DOOR_FAULT_CARS = 1;
    public static void main(String[] args) {
        Scheduler scheduler = Scheduler.getScheduler();
        //Elevator and floor threads
        Thread floorThread;
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem();
        FloorSubsystem floorSubSystem = new FloorSubsystem("ElevatorCalls");
        floorThread = new Thread(floorSubSystem);

        //Create elevator car threads
        Thread[] elevatorCarThreads = new Thread[NUM_ELEVATOR_CARS + NUM_FLOOR_ARRIVAL_FAULT_CARS + NUM_DOOR_FAULT_CARS];
        for (int i = 0; i < NUM_ELEVATOR_CARS; i++) {
            elevatorCarThreads[i] = new Thread(new ElevatorCar(elevatorSubsystem));
        }

        for (int i = 0; i < NUM_FLOOR_ARRIVAL_FAULT_CARS; i++) {
            elevatorCarThreads[i + NUM_ELEVATOR_CARS] = new Thread(new FloorArrivalFaultElevatorCar(elevatorSubsystem));
        }

        for (int i = 0; i < NUM_DOOR_FAULT_CARS; i++) {
            elevatorCarThreads[i + NUM_ELEVATOR_CARS + NUM_FLOOR_ARRIVAL_FAULT_CARS] = new Thread(new DoorFaultElevatorCar(elevatorSubsystem));
        }

        //start the programs execution
        floorThread.start();

        //Start all elevator cars
        for (int i = 0; i < elevatorCarThreads.length; i++) {
            elevatorCarThreads[i].start();
        }
    }
}