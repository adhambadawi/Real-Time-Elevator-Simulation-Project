import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom data structure used to represent an elevator call
 *
 * @author Jaden Sutton
 * @author Adham Badawi
 *
 * @version 3.00
 */
public class ElevatorCall {
    private static final String REGEX_PATTERN = "([0-9]{2}:[0-9]{2}:[0-9]{2} \\d+ \\w+ \\d+)";

    private final Date timestamp;
    private final int startingFloor;
    private List<Integer> targetFloors;

    private final String direction;
    private Integer currentFloor;

    private int passengersTotalWeight;
    private static final int ELEVATOR_WEIGHT_LIMIT = 500;

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
        this.direction = direction;
        this.passengersTotalWeight = new Passenger().getPassengerWeight();

        currentFloor = null;

        targetFloors = new ArrayList<>();
        targetFloors.add(startingFloor);
        targetFloors.add(targetFloor);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getStartingFloor() {
        return startingFloor;
    }

    public Integer getNextTargetFloor() {
        if (targetFloors.size() == 0) {
            return null;
        }
        return targetFloors.get(0);
    }

    public String getDirection() {
        return direction;
    }

    public Integer getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(Integer currentFloor) {
        this.currentFloor = currentFloor;
        if (currentFloor == getNextTargetFloor()) {
            targetFloors.remove(0);
        }
    }

    public int getPassengersTotalWeight() {
        return passengersTotalWeight;
    }

    /**
     * Attempt to merge an incoming request with this request
     * @param request the incoming request
     * @return true if request was merged, false otherwise
     */
    public boolean mergeRequest(ElevatorCall request) {
        if (!canMerge(request)) {
            return false;
        }

        insertTargetFloor(request.getStartingFloor());
        request.setCurrentFloor(request.getStartingFloor());
        insertTargetFloor(request.getNextTargetFloor());

        this.passengersTotalWeight += request.getPassengersTotalWeight();

        return true;
    }

    /**
     * Check if an incoming request can be merged with this request
     * @param request incoming request
     * @return true if the requests can be merged, false otherwise
     */
    private boolean canMerge(ElevatorCall request) {
        if (currentFloor == null) {
            // Cannot merge with this request unless it is currently being serviced as we don't know where the physical elevator is
            return false;
        } else if (request.getCurrentFloor() != null) {
            // Cannot merge a request already being serviced
            return false;
        } else if (!direction.equals(request.getDirection())) {
            // Requests cannot be merged if directions are opposite
            return false;
        } else if (direction.equals("Up") && currentFloor >= request.getStartingFloor() && (getNextTargetFloor() == null || getNextTargetFloor() > request.getStartingFloor())) {
            // Requests cannot be merged if this request has already passed the starting floor of the incoming request
            return false;
        } else if (direction.equals("Down") && currentFloor <= request.getStartingFloor() && (getNextTargetFloor() == null || getNextTargetFloor() < request.getStartingFloor())) {
            return false;
        } else if (passengersTotalWeight + request.getPassengersTotalWeight() > ELEVATOR_WEIGHT_LIMIT ){
            System.out.println("Elevator Car capacity reached");
            return  false;
        }

        return true;
    }

    /**
     * Insert a target floor into the target floor list while maintaining sorted order
     * @param targetFloor floor to insert
     */
    private void insertTargetFloor(int targetFloor) {
        int insertionIndex = 0;
        while (insertionIndex < targetFloors.size() && (direction.equals("Up") && targetFloors.get(insertionIndex) < targetFloor) || (direction.equals("Down") && targetFloors.get(insertionIndex) > targetFloor)) {
            insertionIndex += 1;
        }
        if (insertionIndex >= targetFloors.size()) {
            // Append new target floor to end of target floors if insertion index is out of bounds
            targetFloors.add(targetFloor);
        } else if (targetFloors.get(insertionIndex) != targetFloor) {
            // Only add requests target floor to target floors list if not already included
            targetFloors.add(insertionIndex, targetFloor);
        }
    }

    /**
     * Construct a new ElevatorCall object using a string representation
     * @param repr
     */
    /**
     * Construct a new ElevatorCall object using a string representation
     * @param repr
     */
    public static String[] fromString(String repr) {
        Pattern pattern = Pattern.compile(REGEX_PATTERN);
        Matcher matcher = pattern.matcher(repr);

        String timestamp;
        String startingFloor;
        String targetFloor;
        String direction;

        // Verify that the input line matches the defined regex expression
        if (matcher.matches()) {
            // Parse timestamp, starting floor, direction, and target floor
            timestamp = matcher.group(1).split(" ")[0];
            startingFloor = matcher.group(1).split(" ")[1];
            direction = matcher.group(1).split(" ")[2];
            targetFloor = matcher.group(1).split(" ")[3];

        } else {
            throw new RuntimeException("ElevatorCall string representation does not align with required format.");
        }
        String[] parsedElevatorCallInfo = {timestamp, startingFloor, targetFloor, direction};
        return parsedElevatorCallInfo;
    }


    @Override
    public String toString() {
        return String.format("ElevatorCall{RequestTime=%s, StartingFloor=%d, TargetFloor=%s, direction='%s'}",
                timestamp, startingFloor, targetFloors, direction);
    }

    public List<Integer> getTargetFloors() {
        return targetFloors;
    }
}