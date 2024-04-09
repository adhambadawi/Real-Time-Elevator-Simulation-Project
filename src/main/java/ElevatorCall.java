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
    private static final String REGEX_PATTERN = "(\\d{2}):(\\d{2}):(\\d{2}):(\\d{2}) (\\d+) (up|down) (\\d+)";

    private final Date timestamp;
    private final int startingFloor;
    private List<Integer> targetFloors;

    private final String direction;
    private Integer currentFloor;

    private int passengersTotalWeight;
    private static final int ELEVATOR_WEIGHT_LIMIT = 550;
    private boolean tripStarted;

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
        tripStarted = false;

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
        if (tripStarted && currentFloor == getNextTargetFloor()) {
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
        if (request == null) {
            System.out.println("[ERROR] Null request passed to mergeRequest.");
            return false;
        }
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
        if (request == null) {
            System.out.println("[ERROR] Null request passed to canMerge.");
            return false;
        }
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
     * Insert a target floor into the target floor list while maintaining sorted order. Ensures that the floors are visited efficiently without unnecessary reversals in direction
     * @param targetFloor floor to insert
     */
    private void insertTargetFloor(int targetFloor) {
        try{
            int insertionIndex = 0;
            while (insertionIndex < targetFloors.size() && ((direction.equals("Up") && targetFloors.get(insertionIndex) < targetFloor) || (direction.equals("Down") && targetFloors.get(insertionIndex) > targetFloor))) {
                // increment until:
                //Condition 1. The floor at insertionIndex is either not less than targetFloor when moving up, or not greater than targetFloor when moving down, indicating the correct position for insertion has been found.
                //Condition 2. insertionIndex reaches the size of the targetFloors list, meaning the targetFloor should be added at the end of the list because it is higher (when moving up) or lower (when moving down) than all currently listed floors.
                insertionIndex += 1;
            }

            // Check if insertionIndex is within the bounds of the list and the floor isn't already included
            if (insertionIndex < targetFloors.size() && targetFloors.get(insertionIndex) != targetFloor) {
                targetFloors.add(insertionIndex, targetFloor); //cond 1
            } else if (insertionIndex == targetFloors.size()) {
                // If the index equals the size of the list, add to the end.
                targetFloors.add(targetFloor); //cond 2
            }
        } catch (Exception e) {
            System.err.println("An error occurred while processing: " + e.getMessage());
        }
    }

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
            timestamp = matcher.group(1) + ":" + matcher.group(2) + ":" + matcher.group(3) + ":" + matcher.group(4);
            startingFloor = matcher.group(5);
            direction = matcher.group(6);
            targetFloor = matcher.group(7);

        } else {
            System.out.println(repr);
            throw new RuntimeException("ElevatorCall string representation does not align with required format." + repr);
        }
        String[] parsedElevatorCallInfo = {timestamp, startingFloor, targetFloor, direction};
        return parsedElevatorCallInfo;
    }

    public void setTripStarted() {
        tripStarted = true;
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