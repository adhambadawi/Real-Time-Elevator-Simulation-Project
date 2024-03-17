import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Date;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Class responsible for testing ElevatorSubsystem class, both white box and black box testing
 *
 * @author Adham Badawi
 * @version 2.0
 *
 */

public class ElevatorSubsystem_Test {

    @Mock
    private Scheduler scheduler;

    private ElevatorSubsystem elevatorSubsystem;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        elevatorSubsystem = new ElevatorSubsystem();
    }

    /*
    @Test
    public void testElevatorUpMovement() {
        // Set up the scheduler to return UP actions a certain number of times before returning QUIT
        when(scheduler.getNextAction(anyInt(), anyInt()))
                .thenReturn(ElevatorSubsystem.Action.UP)   // Move up from floor 1 to 2
                .thenReturn(ElevatorSubsystem.Action.UP)   // Move up from floor 2 to 3
                .thenReturn(ElevatorSubsystem.Action.QUIT); // End the loop

        // Run the elevator subsystem (this would normally be in a separate thread)
        elevatorSubsystem.run();

        // Assert that the elevator has moved up to the expected floor
        // The actual floor will depend on how many times 'UP' was returned before 'QUIT'
        assertEquals(3, elevatorSubsystem.getCurrentFloor());
    }

    @Test
    public void testElevatorDownMovement() {
        elevatorSubsystem.setCurrentFloor(3);

        when(scheduler.getNextAction(anyInt(), eq(3)))
                .thenReturn(ElevatorSubsystem.Action.DOWN); // Move down from floor 3 to 2
        when(scheduler.getNextAction(anyInt(), eq(2)))
                .thenReturn(ElevatorSubsystem.Action.DOWN); // Move down from floor 2 to 1
        when(scheduler.getNextAction(anyInt(), eq(1)))
                .thenReturn(ElevatorSubsystem.Action.QUIT); // End the loop

        elevatorSubsystem.run();

        assertEquals(1, elevatorSubsystem.getCurrentFloor());
    }

    @Test
    public void testToggleDoors() throws InterruptedException {
        // Set up the scheduler to return TOGGLE_DOORS action and then QUIT
        when(scheduler.getNextAction(anyInt(), anyInt()))
                .thenReturn(ElevatorSubsystem.Action.TOGGLE_DOORS)
                .thenReturn(ElevatorSubsystem.Action.QUIT);

        long startTime = System.currentTimeMillis();

        elevatorSubsystem.run();

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        // Since toggleDoors involves sleeping for DOOR_OPEN_TIME twice,
        // we check if the elapsed time is at least as long as 2 * DOOR_OPEN_TIME.
        // We add a small buffer (e.g., 100ms) to account for execution time outside of sleep.
        assertTrue("The elapsed time should be at least as long as 2 * DOOR_OPEN_TIME",
                elapsedTime >= (2 * ElevatorSubsystem.DOOR_OPEN_TIME - 100));

        // Additional checks can be added here if there are more observable effects from toggling doors
    }

    @Test
    public void testElevatorQuit() {
        when(scheduler.getNextAction(anyInt(), anyInt())).thenReturn(ElevatorSubsystem.Action.QUIT);

        elevatorSubsystem.run();

        verify(scheduler).getNextAction(anyInt(), anyInt());
        // You can assert that the elevator didn't move if it started at floor 1 and QUIT was the first action
        assertEquals(1, elevatorSubsystem.getCurrentFloor());
    }

    @Test
    public void testElevatorHandlesMultipleStops() {
        // Assuming the elevator starts at floor 1
        // Set up the scheduler to simulate stops at floors 2 and 3
        when(scheduler.getNextAction(anyInt(), eq(1)))
                .thenReturn(ElevatorSubsystem.Action.UP); // Move up to floor 2
        when(scheduler.getNextAction(anyInt(), eq(2)))
                .thenReturn(ElevatorSubsystem.Action.TOGGLE_DOORS) // Stop at floor 2
                .thenReturn(ElevatorSubsystem.Action.UP); // Move up to floor 3
        when(scheduler.getNextAction(anyInt(), eq(3)))
                .thenReturn(ElevatorSubsystem.Action.TOGGLE_DOORS) // Stop at floor 3
                .thenReturn(ElevatorSubsystem.Action.QUIT); // End the loop

        elevatorSubsystem.run();

        // Verify the elevator has moved to the expected floor (3) and stopped at intermediate floors
        assertEquals("Elevator should be at floor 3", 3, elevatorSubsystem.getCurrentFloor());

        // Verify scheduler was asked for next action at each floor
        verify(scheduler, times(1)).getNextAction(elevatorSubsystem.getElevatorId(), 1);
        verify(scheduler, times(2)).getNextAction(elevatorSubsystem.getElevatorId(), 2); // Once before stopping at floor 2 and once after
        verify(scheduler, times(2)).getNextAction(elevatorSubsystem.getElevatorId(), 3); // Once before stopping at floor 3 and once to end the loop
    }

    @Test
    public void testElevatorAlreadyAtTargetFloor() {
        // Assume the elevator starts at floor 1, which is also the target floor
        int targetFloor = 1;
        elevatorSubsystem.setCurrentFloor(targetFloor); // Ensure the elevator is set to the target floor

        // Set up the scheduler to return TOGGLE_DOORS action to simulate the elevator needing to open its doors at the current floor
        when(scheduler.getNextAction(elevatorSubsystem.getElevatorId(), targetFloor))
                .thenReturn(ElevatorSubsystem.Action.TOGGLE_DOORS) // Simulate door toggling at the current/target floor
                .thenReturn(ElevatorSubsystem.Action.QUIT); // End the loop

        elevatorSubsystem.run();

        // Verify the elevator has not moved from the target floor
        assertEquals("Elevator should remain at the target floor", targetFloor, elevatorSubsystem.getCurrentFloor());

        // Verify that the toggleDoors action was called, implying the doors would open at the target floor
        verify(scheduler, atLeastOnce()).getNextAction(elevatorSubsystem.getElevatorId(), targetFloor);
    }

    @Test
    public void testElevatorExceptionHandling() {
        // Prepare the test to expect a RuntimeException
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Scheduler error");

        // Simulate the Scheduler throwing a RuntimeException when getNextAction is called
        when(scheduler.getNextAction(anyInt(), anyInt())).thenThrow(new RuntimeException("Scheduler error"));

        // Execute the run method, which should encounter the simulated exception
        elevatorSubsystem.run();
    }

    @Test
    public void testElevatorSignalsFloorArrival() {
        // Set up the scheduler to simulate an elevator moving up to the next floor
        when(scheduler.getNextAction(elevatorSubsystem.getElevatorId(), 1))
                .thenReturn(ElevatorSubsystem.Action.UP);// Move from floor 1 to 2
        when(scheduler.getNextAction(elevatorSubsystem.getElevatorId(), 2))
                .thenReturn(ElevatorSubsystem.Action.TOGGLE_DOORS);// Simulate stopping at floor 2
        when(scheduler.getNextAction(elevatorSubsystem.getElevatorId(), 2))
                .thenReturn(ElevatorSubsystem.Action.QUIT);// End the loop

        // Run the elevator subsystem
        elevatorSubsystem.run();

        // Verify that getNextAction was called with the updated floor number,
        // which indicates that the elevator signaled its arrival at the new floor
        verify(scheduler, atLeastOnce()).getNextAction(elevatorSubsystem.getElevatorId(), 2);
    }*/
}
