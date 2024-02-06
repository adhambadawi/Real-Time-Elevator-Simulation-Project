import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

public class ElevatorCallTest {

    private ElevatorCall elevatorCall;
    private Date timestamp;
    private final int startingFloor = 1;
    private final int targetFloor = 10;
    private final String direction = "Up";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Before
    public void setUp() throws ParseException {
        timestamp = dateFormat.parse("14:05:15");
        elevatorCall = new ElevatorCall(timestamp, startingFloor, targetFloor, direction);
    }

    @Test
    public void testElevatorCallConstruction() {
        assertNotNull(elevatorCall);
        assertEquals(timestamp, elevatorCall.getTimestamp());
        assertEquals(startingFloor, elevatorCall.getStartingFloor());
        assertTrue(elevatorCall.getTargetFloors().contains(targetFloor));
        assertEquals(direction, elevatorCall.getDirection());
    }

    @Test
    public void testGetNextTargetFloor() {
        Integer nextTargetFloor = elevatorCall.getNextTargetFloor();
        assertNotNull(nextTargetFloor);
        assertEquals(Integer.valueOf(startingFloor), nextTargetFloor);
    }

    @Test
    public void testArrivedAtFloor() {
        elevatorCall.arrivedAtFloor(); // Simulate arriving at the starting floor
        Integer nextTargetFloor = elevatorCall.getNextTargetFloor();
        assertEquals(Integer.valueOf(targetFloor), nextTargetFloor); // The next target should now be the target floor
    }

    @Test
    public void testMergeRequest() throws ParseException {
        // Black Box Testing: Test merging compatible requests
        ElevatorCall upRequest1 = new ElevatorCall(null, 1, 10, "Up");
        ElevatorCall upRequest2 = new ElevatorCall(null, 3, 5, "Up");

        assertFalse(upRequest1.mergeRequest(upRequest2));

        ElevatorSubsystem elevator = new ElevatorSubsystem(Scheduler.getScheduler(null));
        upRequest1.setOwner(elevator);

        ElevatorCall downRequest1 = new ElevatorCall(null, 10, 1, "Down");

        assertFalse(upRequest1.mergeRequest(downRequest1));
        assertTrue(upRequest1.mergeRequest(upRequest2));
        upRequest2.setOwner(elevator);
        assertFalse(upRequest1.mergeRequest(upRequest2));

        // White Box Testing: Test the internal conditions that allow two requests to merge
    }

    @Test
    public void testFromString() {
        String callString = "14:05:15 1 Up 10";
        ElevatorCall fromStringCall = ElevatorCall.fromString(callString);

        assertNotNull(fromStringCall);
        assertEquals(timestamp, fromStringCall.getTimestamp());
        assertEquals(1, fromStringCall.getStartingFloor());
        assertTrue(fromStringCall.getTargetFloors().contains(10));
        assertEquals("Up", fromStringCall.getDirection());
    }

    @Test
    public void testMergeRequestIncompatibleDirection() throws ParseException {
        // Test merging requests with incompatible directions
        ElevatorCall incompatibleDirectionCall = new ElevatorCall(dateFormat.parse("14:06:00"), 2, 3, "Down");
        assertFalse(elevatorCall.mergeRequest(incompatibleDirectionCall));
    }

    @Test
    public void testMergeRequestAlreadyServiced() throws ParseException {
        // Test trying to merge a request that has already been serviced
        ElevatorCall servicedCall = new ElevatorCall(dateFormat.parse("14:07:00"), 3, 2, direction);
        elevatorCall.arrivedAtFloor(); // Simulate arriving at the first target floor
        assertFalse(elevatorCall.mergeRequest(servicedCall));
    }

    @Test
    public void testMergeRequestBeyondCurrentDirection() throws ParseException {
        // Test trying to merge a request that is beyond the current direction of the elevator
        ElevatorCall beyondDirectionCall = new ElevatorCall(dateFormat.parse("14:06:00"), 11, 12, direction);
        assertFalse(elevatorCall.mergeRequest(beyondDirectionCall));
    }

    @Test
    public void testInvalidFromStringFormat() {
        // Test parsing from a string with an invalid format
        String invalidString = "Invalid String";
        try {
            ElevatorCall.fromString(invalidString);
            fail("Expected a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("ElevatorCall string representation does not align with required format.", e.getMessage());
        }
    }

    @Test
    public void testNoMoreTargetFloors() {
        // Test getting the next target floor when there are no more floors left
        elevatorCall.arrivedAtFloor(); // Arrive at the starting floor
        elevatorCall.arrivedAtFloor(); // Arrive at the target floor
        assertNull(elevatorCall.getNextTargetFloor());
    }

    /**
    @Test
    public void testInsertTargetFloorMaintainsOrder() throws ParseException {
        // Test that inserting target floors maintains the correct order
        ElevatorCall multiFloorCall = new ElevatorCall(dateFormat.parse("14:08:00"), 5, 8, "Up");
        multiFloorCall.setOwner(new ElevatorSubsystem(new Scheduler()));
        multiFloorCall.mergeRequest(new ElevatorCall(dateFormat.parse("14:08:00"), 6, 7, "Up"));
        assertEquals(Integer.valueOf(5), multiFloorCall.getNextTargetFloor()); // First floor should be 5
        multiFloorCall.arrivedAtFloor(); // Arrive at floor 5
        assertEquals(Integer.valueOf(6), multiFloorCall.getNextTargetFloor()); // Next floor should be 6
        multiFloorCall.arrivedAtFloor(); // Arrive at floor 6
        assertEquals(Integer.valueOf(7), multiFloorCall.getNextTargetFloor()); // Next should be 7
        // , ensuring order is maintained
    }*/
}