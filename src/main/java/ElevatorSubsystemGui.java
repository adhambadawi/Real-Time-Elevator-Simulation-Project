import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class ElevatorSubsystemGui extends JFrame {
    JButton[][] cells;
    private int runningElevators;
    private Date executionStart;
    private int elevatorMoves;

    public ElevatorSubsystemGui(int numElevators, int numFloors) {
        super("Elevator Subsystem");
        this.setLayout(new GridLayout(numFloors + 1, numElevators));

        cells = new JButton[numFloors + 1][numElevators];
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500,500);
        for (int floor = 0; floor < numFloors + 1; floor++){
            for (int elevatorCarID = 0; elevatorCarID < numElevators; elevatorCarID++){
                JButton button = new JButton();
                if (floor == 0) {
                    button.setForeground(Color.RED);
                    button.setText("1");
                    button.setBackground(Color.BLACK);
                } else {
                    button.setBackground(Color.GRAY);
                }
                this.cells[floor][elevatorCarID] = button;
                this.add(button);
            }
        }

        runningElevators = numElevators;
        executionStart = new Date();

        this.setVisible(true);
    }

    public void handleElevatorPositionUpdate(int elevatorCarID, int currentFloor) {
        cells[0][elevatorCarID].setText(Integer.toString(currentFloor));
        elevatorMoves++;
    }

    public void handleElevatorDoorOpen(int elevatorCarID, int currentFloor) {
        for (int i = 1; i < cells.length; i++) {
            if (cells.length - i == currentFloor) {
                cells[i][elevatorCarID].setText("Open");
                cells[i][elevatorCarID].setBackground(Color.WHITE);
                cells[i][elevatorCarID].setForeground(Color.BLACK);
            }
        }
    }

    public void handleElevatorDoorClose(int elevatorCarID, int currentFloor) {
        for (int i = 1; i < cells.length; i++) {
            if (cells.length - i == currentFloor) {
                cells[i][elevatorCarID].setText("");
                cells[i][elevatorCarID].setBackground(Color.GRAY);
            }
        }
    }

    public void handleElevatorRemoval(int elevatorCarID) {
        cells[0][elevatorCarID].setText("Out of service");
        runningElevators -= 1;
        if (runningElevators == 0) {
            Date now = new Date();
            double executionTime = (now.getTime() - executionStart.getTime()) / 1000;
            JOptionPane.showMessageDialog(this, "All elevators are out of service. GUI will now be closed. Total execution time = " + executionTime + " seconds. Total elevator moves = " + elevatorMoves);
            this.dispose();
        }
    }
}
