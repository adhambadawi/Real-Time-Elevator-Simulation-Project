import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom data structure used to represent an elevator call
 *
 * @author Jaden Sutton
 */
public class ElevatorCall {
    private static final String REGEX_PATTERN = "([0-9]{2}:[0-9]{2}:[0-9]{2} \\d+ \\w+ \\d+)";
    private Date timestamp;
    private int startingFloor;
    private int targetFloor;
    private String direction;

    /**
     * Construct a new ElevatorCall object
     *
     * @param timestamp The time the elevator call occurred
     * @param startingFloor The starting floor
     * @param targetFloor The target floor
     * @param direction The direction
     */
    public ElevatorCall(Date timestamp, int startingFloor, int targetFloor, String direction) {
        this.timestamp = timestamp;
        this.startingFloor = startingFloor;
        this.targetFloor = targetFloor;
        this.direction = direction;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getStartingFloor() {
        return startingFloor;
    }

    public int getTargetFloor() {
        return targetFloor;
    }

    public String getDirection() {
        return direction;
    }

    /**
     * Construct a new ElevatorCall object using a string representation
     * @param repr
     */
    public static ElevatorCall fromString(String repr) {
        Pattern pattern = Pattern.compile(REGEX_PATTERN);
        Matcher matcher = pattern.matcher(repr);

        Date timestamp;
        int startingFloor;
        int targetFloor;
        String direction;

        // Verify that the input line matches the defined regex expression
        if (matcher.matches()) {
            // Parse timestamp, starting floor, direction, and target floor
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            try {
                timestamp = dateFormat.parse(matcher.group(1).split(" ")[0]);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            startingFloor = Integer.valueOf(matcher.group(1).split(" ")[1]);
            direction = matcher.group(1).split(" ")[2];
            targetFloor = Integer.valueOf(matcher.group(1).split(" ")[3]);

        } else {
            throw new RuntimeException("ElevatorCall string representation does not align with required format.");
        }

        return new ElevatorCall(timestamp, startingFloor, targetFloor, direction);
    }
}
