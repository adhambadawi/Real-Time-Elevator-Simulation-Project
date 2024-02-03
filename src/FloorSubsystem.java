import java.io.*;
import java.util.Date;

/**
 * Class reposnisple for simulating arrival of passengers to the elevator
 * and performing elevator calls
 *
 * @author Jaden Sutton
 */
public class FloorSubsystem implements Runnable {
    private Scheduler scheduler;
    private String inputFilepath;

    public FloorSubsystem(Scheduler scheduler, String inputFilepath) {
        this.scheduler = scheduler;
        this.inputFilepath = inputFilepath;
    }
    public void run() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFilepath));
            String line = reader.readLine();
            Date firstTimestamp = null;
            Date initialTime = new Date();
            while (line != null) {
                ElevatorCall elevatorCall = ElevatorCall.fromString(line);

                if (firstTimestamp == null) {
                    firstTimestamp = elevatorCall.getTimestamp();
                } else {
                    Date currTimestamp = new Date();
                    long timeDifference = (elevatorCall.getTimestamp().getTime() - firstTimestamp.getTime()) - (currTimestamp.getTime() - initialTime.getTime());
                    if (timeDifference > 0) {
                        try {
                            Thread.sleep(timeDifference);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                scheduler.addRequest(elevatorCall);
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
