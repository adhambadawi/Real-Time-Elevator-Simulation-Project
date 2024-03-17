import java.util.Random;

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
