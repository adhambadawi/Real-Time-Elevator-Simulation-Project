import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
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
                ElevatorCall elevatorCall = ElevatorCall.fromString(line);
                System.out.println("[FLOOR SUBSYSTEM] Processing new elevator call: " + elevatorCall);

                if (firstTimestamp == null) {
                    firstTimestamp = elevatorCall.getTimestamp();
                } else {

                    long timeDifference = elevatorCall.getTimestamp().getTime() - firstTimestamp.getTime();
                    // Use the calculated difference to simulate time between requests
                    Thread.sleep(timeDifference);
                }

                sendElevatorCall(elevatorCall);
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
    private void sendElevatorCall(ElevatorCall call) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String[] timeParts = sdf.format(call.getTimestamp()).split(":");
            byteBuffer.putInt(Integer.parseInt(timeParts[0]));
            byteBuffer.putInt(Integer.parseInt(timeParts[1]));
            byteBuffer.putInt(Integer.parseInt(timeParts[2]));
            byteBuffer.putInt(call.getStartingFloor());
            byteBuffer.putInt(call.getDirection().equals("Up") ? 1 : 2);
            byteBuffer.putInt(call.getTargetFloors().size());
            for (Integer floor : call.getTargetFloors()) {
                byteBuffer.putInt(floor);
            }

            byte[] sendData = byteBuffer.array();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 23);

            System.out.println("[FLOOR SUBSYSTEM] Sending elevator call to Scheduler: " + call);
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
