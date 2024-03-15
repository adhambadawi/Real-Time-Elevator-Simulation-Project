import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * Class reposnisple for simulating arrival of passengers to the elevator
 * and performing elevator calls
 *
 * @author Jaden Sutton
 * @author Dana El Sherif
 * @version 2.00
 */
public class FloorSubsystem implements Runnable {
    private DatagramSocket sendReceiveSocket;
    private String inputFilepath;
    private Map<Integer, Integer> elevatorCarDisplay;

    public FloorSubsystem(String inputFilepath) {
        this.inputFilepath = inputFilepath;
        elevatorCarDisplay = new HashMap<>();
        try {
            this.sendReceiveSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFilepath));
            String line;
            Date firstTimestamp = null;

            while ((line = reader.readLine()) != null) {
                String[] elevatorCallInfo = ElevatorCall.fromString(line);
//                System.out.println("[FLOOR SUBSYSTEM] Processing new elevator call: " + elevatorCall);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                Date timestamp;
                try {
                   timestamp = dateFormat.parse(elevatorCallInfo[0]);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                if (firstTimestamp == null) {
                    firstTimestamp = timestamp;
                } else {

                    long timeDifference = timestamp.getTime() - firstTimestamp.getTime();
                    // Use the calculated difference to simulate time between requests
                    Thread.sleep(timeDifference);
                }
                sendElevatorCall(elevatorCallInfo);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (sendReceiveSocket != null && !sendReceiveSocket.isClosed()) {
                sendReceiveSocket.close();
            }
        }
    }

    /**
     * creates the message based on elevator calls and sends the packet to port 23
     *  @param call ElevatorCall from text file with requests
     */
    private void sendElevatorCall(String[] elevatorCallInfo) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date timestamp;
            try {
                timestamp = sdf.parse(elevatorCallInfo[0]);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timestamp);
            // Extract hours, minutes, and seconds from the timestamp
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            int seconds = calendar.get(Calendar.SECOND);

            //timestamp parts
            byteBuffer.putInt(hours);
            byteBuffer.putInt(minutes);
            byteBuffer.putInt(seconds);
            byteBuffer.putInt(Integer.parseInt(elevatorCallInfo[1])); //starting floor
            byteBuffer.putInt(Integer.parseInt(elevatorCallInfo[2])); // Target floor
            byteBuffer.putInt(elevatorCallInfo[3].equals("Up") ? 1 : 2); //Direction

            byte[] sendData = byteBuffer.array();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 23);
            sendReceiveSocket.send(sendPacket);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host exception: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the elevator car display with the current location of an elevator car
     * @param elevatorId elevator car id
     * @param currentFloor current floor the elevator is on
     */
    public void updateElevatorCarDisplay(int elevatorId, int currentFloor) {
        System.out.println(String.format("[FLOORSubSystem] Updated the floors display to reflect elevator car %s at floor %s", elevatorId, currentFloor));
        elevatorCarDisplay.put(elevatorId, currentFloor);
    }
}
