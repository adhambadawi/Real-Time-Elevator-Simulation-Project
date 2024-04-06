import java.util.Random;

/**
 * Class to represent the Passenger Object
 *
 * @author Adham Badawi
 *
 * @version 1.00
 */

public class Passenger {
    private int passengerWeight;

    public Passenger() {
        // being constant instead of random since the elevator is bounded by a number of passengers (5)
        this.passengerWeight = 100;
    }

    public int getPassengerWeight() {
        return passengerWeight;
    }
}
