import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Class represents the elevator car that is moving inside the shaft
 * 
 * @author Jaden Sutton
 * @author Adham Badawi
 * @version 2.00
 */

public class ElevatorSubsystem implements Runnable {
    public enum Action { UP, DOWN, TOGGLE_DOORS, QUIT }
    private static final int MOVE_TIME = 8006;
    protected static final int DOOR_OPEN_TIME = 3238;
    private static final int STARTING_FLOOR = 1;
    private Scheduler scheduler;
    private static int elevatorIdCounter = 0;
    private int elevatorId;
    private int currentFloor = STARTING_FLOOR;
    private DatagramPacket sendPacket, receivePacket; // packets to be sent and received using UDP
    private DatagramSocket sendReceiveSocket; // the socket used to send and receive the packets

    /**
     * Constructor for ElevatorSubsystem thread
     * @param scheduler The elevator scheduler
     */
    public ElevatorSubsystem(Scheduler scheduler) {
        elevatorId = elevatorIdCounter++;
        this.scheduler = scheduler;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) { //only for testing purposes
        this.currentFloor = currentFloor;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    @Override
    public void run() {
        Action action = SendReceiveSchedulerPacket(elevatorId, currentFloor);
        while (action != Action.QUIT) {
            switch (action) {
                case UP:
                    move(1);
                    break;
                case DOWN:
                    move(-1);
                    break;
                case TOGGLE_DOORS:
                    toggleDoors();
                    break;
            }

            action = scheduler.getNextAction(elevatorId, currentFloor);
        }
    }

    public void toggleDoors() {
        System.out.println(String.format("[ELEVATOR] Opening and closing doors at floor %d", currentFloor));
        System.out.println("Turn off floor light: " + currentFloor);
        try {
            Thread.sleep(DOOR_OPEN_TIME);
            System.out.println("[ELEVATOR] Doors open");
            Thread.sleep(DOOR_OPEN_TIME);
            System.out.println("[ELEVATOR] Doors closed");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void move(int direction) {
        try {
            Thread.sleep(MOVE_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        currentFloor += direction;
        System.out.println(String.format("[ELEVATOR] Moved to floor %d", currentFloor));
    }

    /**
     * creates the sending and receiving socket and packets, decodes them and send the needed data to the scheduler class then receive
     * the needed data in a new packet to the same port
     *
     *  @param elevatorId the elevator car id
     *  @param currentFloor the current floor for the elevator car with the given id
     *
     */
    public Action SendReceiveSchedulerPacket(int elevatorId, int currentFloor) {

        try {
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();
        }

        byte[] sendData; // data to be sent
        byte[] receiveData; // data to be received

        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES * 2);
            byteBuffer.putInt(elevatorId);
            byteBuffer.putInt(currentFloor);
            sendData = byteBuffer.array();

            sendPacket = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getLocalHost(), 69);

            sendReceiveSocket.send(sendPacket);

            receiveData = new byte[1024];
             receivePacket = new DatagramPacket(receiveData, receiveData.length);

            sendReceiveSocket.receive(receivePacket);

            // get the data and Convert string back to enum
            String receivedText = new String(receivePacket.getData(), 0, receivePacket.getLength());
            ElevatorSubsystem.Action action = ElevatorSubsystem.Action.valueOf(receivedText.trim());

            return action;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Ensure the socket is closed to release resources
            if (sendReceiveSocket != null && !sendReceiveSocket.isClosed()) {
                sendReceiveSocket.close();
            }
        }
        // return default action in case of any error not in catch
        return ElevatorSubsystem.Action.QUIT;

    }
}