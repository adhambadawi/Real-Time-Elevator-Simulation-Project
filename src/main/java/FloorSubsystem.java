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
 * Class responsible for simulating arrival of passengers to the elevator
 * and performing elevator calls
 *
 * @author Jaden Sutton
 * @author Dana El Sherif
 * @author Adham Badawi
 * @author Sameh Gawish
 * @author Amr Abdelazeem
 *
 * @version 3.00
 */
public class FloorSubsystem implements Runnable {
    private DatagramSocket sendReceiveSocket;
    private String inputFilepath;
    private Map<Integer, Integer> elevatorCarDisplay;
    private DatagramSocket ReceiveSocket;
    private DatagramPacket receivePacket;

    public FloorSubsystem(String inputFilepath) {
        this.inputFilepath = inputFilepath;
        elevatorCarDisplay = new HashMap<>();
        try {
            this.sendReceiveSocket = new DatagramSocket();

        } catch (SocketException e) {
            e.printStackTrace();
        }
        listenToSchedulerForDisplay();
    }

    @Override
    public void run() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFilepath));
            String line = reader.readLine();
            Date firstTimestamp = null;
            Date executionStart = new Date();

            while ((line = reader.readLine()) != null) {
                String[] elevatorCallInfo = ElevatorCall.fromString(line);
//                System.out.println("[FLOOR SUBSYSTEM] Processing new elevator call: " + elevatorCall);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SS");
                Date timestamp;
                try {
                   timestamp = dateFormat.parse(elevatorCallInfo[0]);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                if (firstTimestamp == null) {
                    firstTimestamp = timestamp;
                } else {

                    long expectedTimeDifference = timestamp.getTime() - firstTimestamp.getTime();
                    long actualTimeDifference = (new Date()).getTime() - executionStart.getTime();
                    if (expectedTimeDifference > actualTimeDifference) {
                        // Use the calculated difference to simulate time between requests
                        Thread.sleep(expectedTimeDifference - actualTimeDifference);
                    }
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
     *  @param elevatorCallInfo ElevatorCall from text file with requests
     */
    private void sendElevatorCall(String[] elevatorCallInfo) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SS");
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
            byteBuffer.putInt(elevatorCallInfo[3].equals("up") ? 1 : 2); //Direction

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

    public void listenToSchedulerForDisplay() {
        Thread SchedulerListenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ReceiveDisplayInfo();
            }
        });
        SchedulerListenerThread.start();



    }

    public void ReceiveDisplayInfo(){
        try {
            ReceiveSocket = new DatagramSocket(80);
            ReceiveSocket.setSoTimeout(10000);
            byte[] receiveData = new byte[Integer.BYTES * 2];

            while (true) {
                try {

                    receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    ReceiveSocket.receive(receivePacket);
                    ByteBuffer byteBuffer = ByteBuffer.wrap(receivePacket.getData());

                    int elevatorId = byteBuffer.getInt();
                    int currentFloor = byteBuffer.getInt();

                    // commented the displaying part for this iteration
                    // updateElevatorCarDisplay(elevatorId, currentFloor);
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Ensure the socket is closed to release resources
            if (ReceiveSocket != null && !ReceiveSocket.isClosed()) {
                ReceiveSocket.close();
            }
        }
    }


}
