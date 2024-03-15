import java.net.*;
import java.util.*;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * Class represents the elevator subsystem that manages the elevator cars registered under it
 * 
 * @author Jaden Sutton
 * @author Adham Badawi
 * @author Sameh Gawish
 * @author Amr Abdelazeem
 * @version 3.00
 */

public class ElevatorSubsystem{
    public enum Action { UP, DOWN, TOGGLE_DOORS, QUIT }
    private DatagramPacket sendPacket, receivePacket; // packets to be sent and received using UDP
    private DatagramSocket sendReceiveSocket; // the socket used to send and receive the packets
    private Map<Integer, ElevatorCar> elevatorCars; //list of register elevator cars
    /**
     * Constructor for ElevatorSubsystem thread
     * Does not need instantiation of anScheduler object, instead send data
     * throw UDP packets 
     */
    public ElevatorSubsystem() {
        //Synchronized to enure thread safety in case two elevator cars were getting registered at the same time
        this.elevatorCars = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * registers a given elevatorCar to the ElevatorSubsystem
     * Throws an exception if an elevatorCar with the same ID is already registered
     * @param elevatorCar the elevator car to be registered
     * 
     */
    public void registerElevatorCar(ElevatorCar elevatorCar) throws IllegalArgumentException {
        synchronized(elevatorCar) {
            int carId = elevatorCar.getElevatorCarID();
            if (this.elevatorCars.containsKey(carId)) {
                throw new IllegalArgumentException("An ElevatorCar with ID " + carId + " is already registered.");
            }
            //else register the car
            this.elevatorCars.put(carId, elevatorCar);
        }
    }

    /**
     * Sends a packet to the Scheduler requesting an action to be assigned to the
     * elevator car with the given ID
     * @param elevatorCarID the elevatorCarID for the elevator car
     * @return the action given by the scheduler
     */

    //Synchronized method to be called by the elevator cars to get an action
    public synchronized Action getAction(int elevatorCarID){
        int elevatorCarLocation = this.elevatorCars.get(elevatorCarID).getCurrentFloor();
        Action action = this.SendReceiveSchedulerPacket(elevatorCarID, elevatorCarLocation);
        return action;
    }

    /**
     * creates the sending and receiving socket and packets, decodes them and send the needed data to the scheduler class then receive
     * the needed data in a new packet to the same port
     *
     *  @param elevatorCarID the elevator car id
     *  @param currentFloor the current floor for the elevator car with the given id
     *
     */
    public Action SendReceiveSchedulerPacket(int elevatorCarID, int currentFloor) {

        try {
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();
        }

        byte[] sendData; // data to be sent
        byte[] receiveData; // data to be received

        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES * 2);
            byteBuffer.putInt(elevatorCarID);
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