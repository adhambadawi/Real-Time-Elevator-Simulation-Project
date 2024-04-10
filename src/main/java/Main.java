public class Main {
    private static final int NUM_ELEVATOR_CARS = 4;
    private static final int NUM_FLOOR_ARRIVAL_FAULT_CARS = 1;
    private static final int NUM_DOOR_FAULT_CARS = 1;
    private static final int TOTAL_ELEVATOR_CARS = NUM_ELEVATOR_CARS + NUM_FLOOR_ARRIVAL_FAULT_CARS + NUM_DOOR_FAULT_CARS;
    public static void main(String[] args) {
        Scheduler scheduler = Scheduler.getScheduler();
        //Elevator and floor threads
        Thread floorThread;
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem();
        FloorSubsystem floorSubSystem = new FloorSubsystem("ElevatorCalls");
        floorThread = new Thread(floorSubSystem);

        ElevatorSubsystemGui gui = new ElevatorSubsystemGui(TOTAL_ELEVATOR_CARS, 22);

        //Create elevator car threads
        Thread[] elevatorCarThreads = new Thread[TOTAL_ELEVATOR_CARS];
        for (int i = 0; i < NUM_ELEVATOR_CARS; i++) {
            ElevatorCar elevatorCar = new ElevatorCar(elevatorSubsystem);
            elevatorCar.addView(gui);
            elevatorCarThreads[i] = new Thread(elevatorCar);
        }

        for (int i = 0; i < NUM_FLOOR_ARRIVAL_FAULT_CARS; i++) {
            ElevatorCar elevatorCar = new FloorArrivalFaultElevatorCar(elevatorSubsystem);
            elevatorCar.addView(gui);
            elevatorCarThreads[i + NUM_ELEVATOR_CARS] = new Thread(elevatorCar);
        }

        for (int i = 0; i < NUM_DOOR_FAULT_CARS; i++) {
            ElevatorCar elevatorCar = new DoorFaultElevatorCar(elevatorSubsystem);
            elevatorCar.addView(gui);
            elevatorCarThreads[i + NUM_ELEVATOR_CARS + NUM_FLOOR_ARRIVAL_FAULT_CARS] = new Thread(elevatorCar);
        }

        //start the programs execution
        floorThread.start();

        //Start all elevator cars
        for (int i = 0; i < elevatorCarThreads.length; i++) {
            elevatorCarThreads[i].start();
        }
    }
}