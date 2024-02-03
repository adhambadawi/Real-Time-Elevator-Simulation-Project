import java.time.LocalTime;

public class Request {

    LocalTime time;
    int floor;
    String direction;
    int destinationFloor;

    public Request(LocalTime time, int floor, String direction, int destinationFloor){
        this.time = time;
        this.floor = floor;
        this.direction = direction;
        this.destinationFloor = destinationFloor;
    }

    public LocalTime getTime() {
        return time;
    }

    public int getFloor() {
        return floor;
    }

    public String getDirection() {
        return direction;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    @Override
    public String toString() {
        return "Request{" +
                "time=" + time +
                ", floor=" + floor +
                ", direction='" + direction + '\'' +
                ", destinationFloor=" + destinationFloor +
                '}';
    }
}
