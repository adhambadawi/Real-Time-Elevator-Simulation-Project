import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    
    private List<Request> requests;

    public Main(){
        requests = new ArrayList<>();
        Request request1 = new Request(LocalTime.of(8, 0), 3, "UP", 5);
        Request request2 = new Request(LocalTime.of(8, 1), 3, "UP", 5);
        Request request3 = new Request(LocalTime.of(8, 5), 1, "DOWN", 2);
        Request request4 = new Request(LocalTime.of(8, 2), 2, "UP", 6);
        Request request5 = new Request(LocalTime.of(8,30), 2, "UP", 4);
        Request request6 = new Request(LocalTime.of(8, 30), 4, "DOWN", 1);
        requests.add(request1);
        requests.add(request2);
        requests.add(request3);
        requests.add(request4);
        requests.add(request5);
        requests.add(request6);
        analyzeRequests();
    }
    public static void main(String[] args) {
        Main main = new Main();
    }
}