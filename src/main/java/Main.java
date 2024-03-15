public class Main {
    public static void main(String[] args) {
        Scheduler scheduler = Scheduler.getScheduler();

        //Elevator and floor threads
        Thread elevatorCarThread1, elevatorCarThread2, elevatorCarThread3, floorThread;
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem();
        FloorSubsystem floorSubSystem = new FloorSubsystem(scheduler, "ElevatorCalls");
        floorThread = new Thread(floorSubSystem);

        //Elevator cars
        ElevatorCar elevatorCar1 = new ElevatorCar(elevatorSubsystem);
        ElevatorCar elevatorCar2 = new ElevatorCar(elevatorSubsystem);
        ElevatorCar elevatorCar3 = new ElevatorCar(elevatorSubsystem);
        //Elevator cars threads
        elevatorCarThread1 = new Thread(elevatorCar1);
        elevatorCarThread2 = new Thread(elevatorCar2);
        elevatorCarThread3 = new Thread(elevatorCar3);



        //register the elevator and floor systems to listen the scheduler
        scheduler.registerSubFloorSubsystem(floorSubSystem);

        //start the programs execution
        floorThread.start();
        elevatorCarThread1.start();
        elevatorCarThread2.start();
        elevatorCarThread3.start();
    }
}