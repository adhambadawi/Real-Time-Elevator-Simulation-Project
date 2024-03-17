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
        // Assign a random weight between 50 to 100 kgs
        this.passengerWeight = new Random().nextInt(51) + 50;
    }

    public int getPassengerWeight() {
        return passengerWeight;
    }
}
