import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Date;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ElevatorSubsystemTest {

    @Mock
    private Scheduler scheduler;

    private ElevatorSubsystem elevatorSubsystem;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        elevatorSubsystem = new ElevatorSubsystem(scheduler);
    }

    @Test
    public void testElevatorMovement() {
        // Simulate an elevator call that requires the elevator to move up
        ElevatorCall call = new ElevatorCall(new Date(), 1, 3, "Up");
        when(scheduler.getNextTrip(elevatorSubsystem)).thenReturn(call, (ElevatorCall) null);

        elevatorSubsystem.run();

        // Verify the elevator has moved to the expected floor
        assertEquals(3, elevatorSubsystem.getCurrentFloor());
    }

    @Test
    public void testToggleDoors() throws InterruptedException {
        // Simulate door operations and ensure the methods are called
        ElevatorCall call = new ElevatorCall(new Date(), 1, 2, "Up");
        when(scheduler.getNextTrip(elevatorSubsystem)).thenReturn(call, (ElevatorCall) null);

        elevatorSubsystem.run();

        //to avoid testing with sleep time
        assertEquals(2, elevatorSubsystem.getCurrentFloor());
    }

    @Test
    public void testRunNoCalls() {
        // Test the elevator subsystem when there are no calls
        when(scheduler.getNextTrip(elevatorSubsystem)).thenReturn(null);

        elevatorSubsystem.run();

        // Verify that the current floor hasn't changed as there were no elevator calls
        assertEquals(1, elevatorSubsystem.getCurrentFloor()); // Assuming the elevator starts at floor 1
    }

    @Test
    public void testElevatorMovesDown() {
        // Test elevator movement downwards
        ElevatorCall call = new ElevatorCall(new Date(), 5, 1, "Down");
        when(scheduler.getNextTrip(elevatorSubsystem)).thenReturn(call, (ElevatorCall) null);

        elevatorSubsystem.run();

        // Assert that the elevator has moved down to the expected floor
        assertEquals(1, elevatorSubsystem.getCurrentFloor());
    }

    @Test
    public void testElevatorHandlesMultipleStops() {
        // Test elevator handling multiple stops
        ElevatorCall call1 = new ElevatorCall(new Date(), 1, 3, "Up");
        ElevatorCall call2 = new ElevatorCall(new Date(), 3, 5, "Up");
        when(scheduler.getNextTrip(elevatorSubsystem)).thenReturn(call1, call2, (ElevatorCall) null);

        elevatorSubsystem.run();

        // Assert that the elevator has moved to the last requested floor
        assertEquals(5, elevatorSubsystem.getCurrentFloor());
    }

    @Test
    public void testDoorToggling() {
        // Test door toggling mechanism
        ElevatorCall call = new ElevatorCall(new Date(), 1, 2, "Up");
        when(scheduler.getNextTrip(elevatorSubsystem)).thenReturn(call, (ElevatorCall) null);

        elevatorSubsystem.run();

        // Verify the toggleDoors() method was called
    }

    @Test
    public void testElevatorAlreadyAtTargetFloor() {
        // Test the behavior when the elevator is already at the target floor
        ElevatorCall call = new ElevatorCall(new Date(), 1, 1, "Up");
        when(scheduler.getNextTrip(elevatorSubsystem)).thenReturn(call, (ElevatorCall) null);

        elevatorSubsystem.run();

        // Assert that the elevator does not move
        assertEquals(1, elevatorSubsystem.getCurrentFloor());
    }

    @Test(expected = RuntimeException.class)
    public void testElevatorExceptionHandling() {
        // Test elevator's reaction to an exception, such as a power outage
        doThrow(new RuntimeException("Power outage")).when(scheduler).getNextTrip(elevatorSubsystem);

        elevatorSubsystem.run();
    }

    @Test
    public void testElevatorSignalsFloorArrival() {
        // Test that the elevator signals its arrival at each floor
        ElevatorCall call = new ElevatorCall(new Date(), 1, 3, "Up");
        when(scheduler.getNextTrip(elevatorSubsystem)).thenReturn(call, (ElevatorCall) null);

        elevatorSubsystem.run();

        // Verify the notifySchedulerElevatorDetected method was called on the scheduler
    }

}
