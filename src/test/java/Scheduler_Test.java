import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class Scheduler_Test {

    private Scheduler scheduler;

    @Before
    public void setUp() {
        scheduler = Scheduler.getScheduler();
    }

    @Test
    public void testAddRequest() {
        ElevatorCall call = new ElevatorCall(new Date(), 1, 5, "Up");
        scheduler.addRequest(call);

        assertFalse("Request queue should not be empty after adding a request", scheduler.getRequestQueue().isEmpty());
        assertTrue("Request queue should contain the added request", scheduler.getRequestQueue().contains(call));
    }

    @Test
    public void testInitialState() {
        ElevatorCall call = new ElevatorCall(new Date(), 2, 6, "Up");

        // Capture the initial state, which should be WaitingForRequest
        assertTrue("Initial state should be WaitingForRequest", scheduler.getCurrentState().toString().contains("WaitingForRequest"));
    }

    @Test
    public void testAssignTrip() {
        ElevatorCall call = new ElevatorCall(new Date(), 3, 7, "Up");
        scheduler.addRequest(call);

        boolean result = scheduler.assignTrip(1); // Assuming elevatorId 1 is valid
        assertTrue("assignTrip should succeed when there are pending requests", result);
        assertNotNull("Active trips should contain an entry for the elevator after assigning a trip", scheduler.getActiveTrips().get(1));
    }

    /**
    @Test
    public void testGetNextAction() {
        ElevatorCall call = new ElevatorCall(new Date(), 4, 8, "Up");
        Scheduler schedulerTestNextAction = Scheduler.getScheduler();
        schedulerTestNextAction.addRequest(call);
        schedulerTestNextAction.assignTrip(2);

        ElevatorSubsystem.Action action = schedulerTestNextAction.getNextAction(2, 3);
        assertEquals("The first action for an 'Up' request should be UP", ElevatorSubsystem.Action.UP, action);

        action = schedulerTestNextAction.getNextAction(2, 4);
        assertEquals("The action when the elevator reaches the target floor should be TOGGLE_DOORS", ElevatorSubsystem.Action.TOGGLE_DOORS, action);

        action = schedulerTestNextAction.getNextAction(2, 5);
        assertEquals("The action when the elevator reaches the target floor should be TOGGLE_DOORS", ElevatorSubsystem.Action.UP, action);

        action = schedulerTestNextAction.getNextAction(2, 8);
        assertEquals("The action when the elevator reaches the target floor should be TOGGLE_DOORS", ElevatorSubsystem.Action.TOGGLE_DOORS, action);
    }*/

    @Test
    public void testAddRequestAndStateTransition() {
        Scheduler scheduler = Scheduler.getScheduler();
        ElevatorCall call = new ElevatorCall(new Date(), 1, 5, "Up");

        scheduler.addRequest(call);

        // Verify the request is added to the queue
        assertFalse("Request queue should not be empty after adding a request", scheduler.getRequestQueue().isEmpty());

        // Verify the state transition to AddingRequest and then back to WaitingForRequest
        assertEquals("Scheduler state should be WaitingForRequest after adding a request", "WaitingForRequest", scheduler.getCurrentState().getClass().getSimpleName());
    }

    /**
    @Test
    public void testHandlingMultipleElevators() {
        Scheduler scheduler = Scheduler.getScheduler();
        ElevatorCall call1 = new ElevatorCall(new Date(), 1, 3, "Up");
        ElevatorCall call2 = new ElevatorCall(new Date(), 2, 5, "Up");

        scheduler.addRequest(call1);
        scheduler.addRequest(call2);

        // Assuming elevator IDs 1 and 2
        scheduler.registerElevatorSubsystemNode(() -> {}); // Mock elevator 1
        scheduler.registerElevatorSubsystemNode(() -> {}); // Mock elevator 2

        // Simulate assigning actions for both elevators
        ElevatorSubsystem.Action action1 = scheduler.getNextAction(1, 1);
        ElevatorSubsystem.Action action2 = scheduler.getNextAction(2, 2);

        // Verify actions for both elevators
        assertNotNull("Action for elevator 1 should not be null", action1);
        assertNotNull("Action for elevator 2 should not be null", action2);
    }*/


    /**
    @Test
    public void testRequestCompletionAndCleanup() {
        Scheduler scheduler = Scheduler.getScheduler();
        ElevatorCall call = new ElevatorCall(new Date(), 1, 3, "Up");

        scheduler.addRequest(call);
        // Assuming elevator ID 1
        scheduler.registerElevatorSubsystemNode(() -> {}); // Mock elevator

        // Simulate completing the request
        scheduler.getNextAction(1, 1); // Move up from floor 1
        scheduler.getNextAction(1, 3); // Arrive at floor 3

        // Assuming some mechanism to mark the request as completed, e.g., after TOGGLE_DOORS action
        scheduler.getActiveTrips().remove(1); // Manually removing for this test

        // Verify that active trips are empty after request completion
        assertTrue("Active trips should be empty after request completion", scheduler.getActiveTrips().isEmpty());
    }*/

}
