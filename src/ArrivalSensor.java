
/**
 * Class representing the ArrivalSensor, that is used to detect the elevator
 * car at a specific floor.
 * 
 * 
 * @author Amr Abdelazeem
 * @version 1.00
 */

public class ArrivalSensor {

    private int elevatorSubsystemCarID; //represents the ID for the elevator car the Arrival sensor is monitoring, must be unique for each car.
    private Integer arrivalSensorMonitoringFloor= null; //equivalent floor where the Sensor is fixed in the Elevator shaft.
    private boolean elevatorCarDetected = false; //Assign to true when the car gets detected or if it standing at this floor, false otherwise.
    private Scheduler scheduler; //The elevator system scheduler

    public ArrivalSensor(int elevatorSubsystemCarID, Integer arrivalSensorMonitoringFloor, Scheduler scheduler){

        //Only the if statement should always be instantiated but just for extra cautious 
        if (this.arrivalSensorMonitoringFloor == null){
            this.arrivalSensorMonitoringFloor = arrivalSensorMonitoringFloor;
        }
        else{
            System.out.println("Arrival sensor is already fixed in floor: " + this.arrivalSensorMonitoringFloor +
            " and cannot be reassigned at a different floor!");
        }
        this.elevatorSubsystemCarID = elevatorSubsystemCarID;
        this.scheduler = scheduler;
    }

    private void notifySchedulerElevatorCarDetected(){
        setElevatorCarDetected(true);
        //Notify the Scheduler that the elevator car was detected
        scheduler.notifySchedulerElevatorDetected(arrivalSensorMonitoringFloor, elevatorSubsystemCarID);
    }

    public void setElevatorCarDetected(boolean elevatorCarDetected) {
        this.elevatorCarDetected = elevatorCarDetected;
    }  
}
