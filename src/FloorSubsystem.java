import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class reposnisple for simulating arrival of passengers to the elevator
 * and performing elevator calls
 *
 * @author Jaden Sutton
 */
public class FloorSubsystem implements Runnable {
    private Scheduler scheduler;
    private String inputFilepath;
    private Map<Integer, Integer> elevatorCarDisplay; // Used to store the current floors of each elevator for future display

    /**
     * Constructor for FloorSubsystem thread
     * @param scheduler The elevator scheduler
     * @param inputFilepath The path of the requests input file
     */
    public FloorSubsystem(Scheduler scheduler, String inputFilepath) {
        this.scheduler = scheduler;
        this.inputFilepath = inputFilepath;
        elevatorCarDisplay = new HashMap<Integer, Integer>();
    }

    /**
     * Parse each request in the input file and forward it to the scheduler
     */
    public void run() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFilepath));
            String line = reader.readLine();
            Date firstTimestamp = null;
            Date initialTime = new Date();

            // Iterate through each line in input file
            while (line != null) {
                ElevatorCall elevatorCall = ElevatorCall.fromString(line);  // Get a new ElevatorCall object from the current input line

                if (firstTimestamp == null) {
                    firstTimestamp = elevatorCall.getTimestamp();
                } else {
                    // Calculate the difference between the desired time between subsequent requests and the elapsed time of program execution
                    Date currTimestamp = new Date();
                    long timeDifference = (elevatorCall.getTimestamp().getTime() - firstTimestamp.getTime()) - (currTimestamp.getTime() - initialTime.getTime());
                    // Use the calculated difference to simulate time between requests
                    if (timeDifference > 0) {
                        try {
                            Thread.sleep(timeDifference);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                scheduler.addRequest(elevatorCall); // Send request to scheduler
                line = reader.readLine();
            }
            scheduler.signalRequestsComplete();

            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update the elevator car display with the current location of an elevator car
     * @param elevatorId elevator car id
     * @param currFloor current floor the elevator is on
     */
    public void updateElevatorCarDisplay(int elevatorId, int currFloor) {
        System.out.println("Updated the floor display to reflect floor : " + currFloor);
        elevatorCarDisplay.put(elevatorId, currFloor);
    }
}
