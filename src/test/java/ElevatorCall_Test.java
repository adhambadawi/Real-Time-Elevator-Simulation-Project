import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Class responsible for testing ElevatorCall class, both white box and black box testing
 *
 * @author Adham Badawi
 * @version 2.0
 *
 */
public class ElevatorCall_Test {

    private ElevatorCall elevatorCall;
    private ElevatorCall elevatorCallUp;
    private ElevatorCall elevatorCallDown;
    private Date timestamp;
    private final int startingFloor = 1;
    private final int firstTargetFloor = 5;
    private final int secondTargetFloor = 10;
    private final int targetFloor = 10;
    private final String direction = "Up";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Before
    public void setUp() throws ParseException {
        timestamp = dateFormat.parse("14:05:15");
        // Initializing with two target floors to test the removal of floors upon arrival
        elevatorCall = new ElevatorCall(timestamp, startingFloor, secondTargetFloor, direction);
        elevatorCall.getTargetFloors().add(1, firstTargetFloor); // Adding an intermediate stop

        // Create an ElevatorCall for the Up direction
        elevatorCallUp = new ElevatorCall(timestamp, 1, 5, "Up");
        // Create an ElevatorCall for the Down direction
        elevatorCallDown = new ElevatorCall(timestamp, 10, 5, "Down");
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
        // Initially, the next target floor should be the starting floor
        assertEquals("Initial next target floor should be the starting floor", Integer.valueOf(startingFloor), elevatorCall.getNextTargetFloor());

        // Simulate arrival at the starting floor
        elevatorCall.setCurrentFloor(startingFloor);
        assertFalse("Starting floor should be removed from target floors", elevatorCall.getTargetFloors().contains(startingFloor));

        // The next target floor should now be the first target floor
        assertEquals("Next target floor should update to the first target floor", Integer.valueOf(firstTargetFloor), elevatorCall.getNextTargetFloor());

        // Simulate arrival at the first target floor
        elevatorCall.setCurrentFloor(firstTargetFloor);
        assertFalse("First target floor should be removed from target floors", elevatorCall.getTargetFloors().contains(firstTargetFloor));

        // The next target floor should now be the second target floor
        assertEquals("Next target floor should update to the second target floor", Integer.valueOf(secondTargetFloor), elevatorCall.getNextTargetFloor());

        // Simulate arrival at the second target floor
        elevatorCall.setCurrentFloor(secondTargetFloor);
        assertFalse("Second target floor should be removed from target floors", elevatorCall.getTargetFloors().contains(secondTargetFloor));

        // After arriving at the last target floor, there should be no next target floor
        assertNull("There should be no next target floor after arriving at the last target floor", elevatorCall.getNextTargetFloor());
    }

    @Test
    public void testMergeRequestBeyondCurrentDirection() throws ParseException {
        // Test trying to merge a request that is beyond the current direction of the elevator
        ElevatorCall beyondDirectionCall = new ElevatorCall(dateFormat.parse("14:06:00"), 11, 12, direction);
        assertFalse(elevatorCall.mergeRequest(beyondDirectionCall));
    }


    @Test
    public void testMergeRequestIncompatibleDirection() throws ParseException {
        // Test merging requests with incompatible directions
        ElevatorCall incompatibleDirectionCall = new ElevatorCall(dateFormat.parse("14:06:00"), 2, 3, "Down");
        assertFalse(elevatorCall.mergeRequest(incompatibleDirectionCall));
    }


    @Test
    public void testFromString() {
        String input = "14:05:15 1 Up 10";
        String[] expected = {"14:05:15", "1", "10", "Up"};
        String[] result = ElevatorCall.fromString(input);
        assertArrayEquals("The parsed array should match the expected output", expected, result);
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


    // Existing test cases remain valid and should be kept as is.
    // Additional or modified test cases are as follows:

    @Test
    public void testSetCurrentFloor() {
        assertNull("Initially, currentFloor should be null", elevatorCall.getCurrentFloor());

        elevatorCall.setCurrentFloor(startingFloor);
        assertEquals("Current floor should be updated to the starting floor", Integer.valueOf(startingFloor), elevatorCall.getCurrentFloor());
    }

    @Test
    public void testUpdateCurrentFloorRemovesTarget() {
        elevatorCall.setCurrentFloor(startingFloor); // Simulate the elevator arriving at the starting floor
        assertEquals("Next target floor should be updated to the target floor after arriving at the starting floor", Integer.valueOf(firstTargetFloor), elevatorCall.getNextTargetFloor());

        elevatorCall.setCurrentFloor(firstTargetFloor); // Simulate the elevator arriving at the first target floor
        assertEquals("Next target floor should be updated to the target floor after arriving at the starting floor", Integer.valueOf(secondTargetFloor), elevatorCall.getNextTargetFloor());

        elevatorCall.setCurrentFloor(secondTargetFloor); // Simulate the elevator arriving at the second target floor
        assertNull("There should be no next target floor after arriving at the target floor", elevatorCall.getNextTargetFloor());
    }

    @Test
    public void testMergeRequestWithCurrentFloorSet() throws ParseException {
        elevatorCall.setCurrentFloor(startingFloor); // Elevator is servicing this call and is at the starting floor

        ElevatorCall anotherCall = new ElevatorCall(dateFormat.parse("14:06:00"), 2, 5, direction);
        assertTrue("Should be able to merge a request if currentFloor is set", elevatorCall.mergeRequest(anotherCall));
    }

    @Test
    public void testCannotMergeAlreadyServicedRequest() throws ParseException {
        ElevatorCall servicedCall = new ElevatorCall(dateFormat.parse("14:07:00"), 3, 4, direction);
        servicedCall.setCurrentFloor(4); // This call has been serviced and is at its target floor

        assertFalse("Should not be able to merge a request that has already been serviced", elevatorCall.mergeRequest(servicedCall));
    }

    @Test
    public void testMergeCompatibleRequests() throws ParseException {
        ElevatorCall baseCall = new ElevatorCall(timestamp, 2, 6, "Up");
        baseCall.setCurrentFloor(1); // means that the elevator is being serviced

        // A compatible call that should be merged
        ElevatorCall compatibleCall = new ElevatorCall(timestamp, 3, 5, "Up");

        assertTrue("Compatible requests should be merged", baseCall.mergeRequest(compatibleCall));

        // Verify that the target floors of the base call include the floors from the merged call
        assertTrue("Base call should now include target floor from merged call", baseCall.getTargetFloors().contains(compatibleCall.getStartingFloor()));
        assertTrue("Base call should now include target floor from merged call", baseCall.getTargetFloors().contains(compatibleCall.getNextTargetFloor()));
    }

    @Test
    public void testMergeIncompatibleDirectionRequests() throws ParseException {
        ElevatorCall baseCall = new ElevatorCall(timestamp, 4, 7, "Up");

        // An incompatible call due to direction
        ElevatorCall incompatibleDirectionCall = new ElevatorCall(timestamp, 5, 8, "Down");

        assertFalse("Requests with different directions should not be merged", baseCall.mergeRequest(incompatibleDirectionCall));
    }

    @Test
    public void testMergeNonOverlappingRequests() throws ParseException {
        ElevatorCall baseCall = new ElevatorCall(timestamp, 1, 3, "Up");

        // A non-overlapping call that should not be merged
        ElevatorCall nonOverlappingCall = new ElevatorCall(timestamp, 5, 8, "Up");

        assertFalse("Non-overlapping requests should not be merged", baseCall.mergeRequest(nonOverlappingCall));
    }

    @Test
    public void testMergeRequestsWithCurrentFloorSet() throws ParseException {
        ElevatorCall baseCall = new ElevatorCall(timestamp, 1, 4, "Up");
        baseCall.setCurrentFloor(2); // Simulate the elevator having moved to floor 2

        // A compatible call that arrives after the elevator has started its journey
        ElevatorCall lateCall = new ElevatorCall(timestamp, 3, 5, "Up");

        assertTrue("Requests should be merged even if the base call's current floor is set", baseCall.mergeRequest(lateCall));

        // Verify the merged call includes the late call's floors
        assertTrue("Merged call should include floors from late call", baseCall.getTargetFloors().contains(lateCall.getStartingFloor()));
        assertTrue("Merged call should include floors from late call", baseCall.getTargetFloors().contains(lateCall.getNextTargetFloor()));
    }

    @Test
    public void testMergeRequestAlreadyServiced() throws ParseException {
        ElevatorCall servicedCall = new ElevatorCall(dateFormat.parse("14:06:00"), 7, 10, "Up");
        servicedCall.setCurrentFloor(7);

        // New call that arrives after the initial call has been serviced
        ElevatorCall newCall = new ElevatorCall(dateFormat.parse("14:06:00"), 6, 8, "Up");

        // Attempt to merge the new call with the already serviced call
        boolean mergeResult = servicedCall.mergeRequest(newCall);

        // Verify that the merge operation was unsuccessful
        assertFalse("Should not be able to merge a new request into an already serviced call", mergeResult);

        // Additionally, verify that the serviced call's target floors list remains unchanged
        assertEquals("The serviced call's target floors list should remain unchanged", 1, servicedCall.getTargetFloors().size());
        assertTrue("The serviced call's target floors list should still contain the original target floor", servicedCall.getTargetFloors().contains(10));
    }

    @Test
    public void testNoMoreTargetFloors() {
        // Initially, the next target floor should be the starting floor
        assertEquals("Initial next target floor should be the starting floor", Integer.valueOf(startingFloor), elevatorCall.getNextTargetFloor());

        // Simulate arrival at the starting floor
        elevatorCall.setCurrentFloor(startingFloor);
        elevatorCall.setCurrentFloor(firstTargetFloor);

        // The next target floor should now be the final target floor
        assertEquals("Next target floor should be the final target floor", Integer.valueOf(secondTargetFloor), elevatorCall.getNextTargetFloor());

        // Simulate arrival at the final target floor
        elevatorCall.setCurrentFloor(targetFloor);

        // After arriving at the last target floor, there should be no more target floors
        assertNull("There should be no next target floor after all targets have been serviced", elevatorCall.getNextTargetFloor());
    }

    @Test
    public void testInsertTargetFloorMaintainsOrder() {
        // Merge requests for the "Up" direction to check order is maintained
        elevatorCallUp.mergeRequest(new ElevatorCall(timestamp, 2, 3, "Up"));
        elevatorCallUp.mergeRequest(new ElevatorCall(timestamp, 4, 6, "Up"));

        // Check order for "Up" direction
        List<Integer> targetFloorsUp = elevatorCallUp.getTargetFloors();
        for (int i = 0; i < targetFloorsUp.size() - 1; i++) {
            assertTrue("Target floors should be in ascending order for 'Up' direction",
                    targetFloorsUp.get(i) < targetFloorsUp.get(i + 1));
        }

        // Merge requests for the "Down" direction to check order is maintained
        elevatorCallDown.mergeRequest(new ElevatorCall(timestamp, 9, 7, "Down"));
        elevatorCallDown.mergeRequest(new ElevatorCall(timestamp, 6, 4, "Down"));

        // Check order for "Down" direction
        List<Integer> targetFloorsDown = elevatorCallDown.getTargetFloors();
        for (int i = 0; i < targetFloorsDown.size() - 1; i++) {
            assertTrue("Target floors should be in descending order for 'Down' direction",
                    targetFloorsDown.get(i) > targetFloorsDown.get(i + 1));
        }
    }

}
