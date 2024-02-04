import org.junit.Test;

import static org.junit.Assert.*;

public class ElevatorCallTest {
    @Test
    public void testMergeRequest() {
        ElevatorCall upRequest1 = new ElevatorCall(null, 1, 10, "Up");
        ElevatorCall upRequest2 = new ElevatorCall(null, 3, 5, "Up");

        assertFalse(upRequest1.mergeRequest(upRequest2));

        ElevatorSubsystem elevator = new ElevatorSubsystem();
        upRequest1.setOwner(elevator);

        ElevatorCall downRequest1 = new ElevatorCall(null, 10, 1, "Down");

        assertFalse(upRequest1.mergeRequest(downRequest1));
        assertTrue(upRequest1.mergeRequest(upRequest2));
        upRequest2.setOwner(elevator);
        assertFalse(upRequest1.mergeRequest(upRequest2));
    }
}