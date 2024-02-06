//import org.junit.Test;
//
//import static org.junit.Assert.*;
//
//public class ElevatorCallTest {
//
//    @Test
//    public void testElevatorCallConstruction() {
//        // Black Box Testing: Test if the ElevatorCall object is created with the correct parameters
//    }
//
//    @Test
//    public void testGetNextTargetFloor() {
//        // White Box Testing: Test the logic to get the next target floor
//    }
//
//    @Test
//    public void testArrivedAtFloor() {
//        // White Box Testing: Test removing a floor from the target list when arrived
//    }
//    @Test
//    public void testMergeRequest() {
//        // Black Box Testing: Test merging compatible requests
//        ElevatorTrip upRequest1 = new ElevatorTrip(null, 1, 10, "Up");
//        ElevatorTrip upRequest2 = new ElevatorTrip(null, 3, 5, "Up");
//
//        assertFalse(upRequest1.mergeRequest(upRequest2));
//
//        Elevator elevator = new Elevator(Scheduler.getScheduler(null));
//        upRequest1.setOwner(elevator);
//
//        ElevatorTrip downRequest1 = new ElevatorTrip(null, 10, 1, "Down");
//
//        assertFalse(upRequest1.mergeRequest(downRequest1));
//        assertTrue(upRequest1.mergeRequest(upRequest2));
//        upRequest2.setOwner(elevator);
//        assertFalse(upRequest1.mergeRequest(upRequest2));
//
//        // White Box Testing: Test the internal conditions that allow two requests to merge
//
//    }
//
//    @Test
//    public void testFromString() {
//        // Black Box Testing: Test creating an ElevatorCall from a string representation
//    }
//}