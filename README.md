# SYSC3303Project
# Elevator System Simulation
This documentation provides an overview of the Elevator System Simulation project. The project simulates the operation of an elevator system within a building, handling elevator calls from different floors and directing the elevator to service these calls efficiently.

# Overview
The system is composed of several critical components, now augmented with state machines for streamlined and efficient operation:

- **ElevatorTrip**: Represents a service request by detailing the call time, starting floor, target floor(s), and travel direction.

- **Elevator**: Controls the elevator's movements, including door operations and floor transitions. It follows a state machine model with states such as `UP`, `DOWN`, `TOGGLE_DOORS`, `IDLE`, and `QUIT`, offering a more detailed simulation of elevator behaviors.

- **FloorSubsystem**: Generates `ElevatorTrip` requests by simulating passenger arrivals at different floors, based on input data.

- **Scheduler**: Acts as the intermediary between the `FloorSubsystem` and `Elevator`, optimizing the processing of elevator calls. The Scheduler utilizes a state machine with states like `WAITING_FOR_REQUESTS`, `ASSIGNING_ACTIONS`, and `ADDING_ACTIONS` for dynamic call management and elevator instruction assignment.

# Features
- **Elevator Call Handling**: Manages call details such as timestamp, starting and target floors, and direction.

- **Elevator Movement Simulation**: Employs state machine logic for a realistic and efficient representation of elevator movements.

- **Call Merging**: Combines similar calls to enhance system efficiency.

- **Floor Subsystem Simulation**: Interprets an input file to simulate time-delayed elevator call requests.

- **Scheduler Coordination**: Uses a state machine for sophisticated elevator call queuing and task allocation.

# Usage
To use the simulation, compile and run the Scheduler class. The system will start processing elevator calls based on the predefined input file for floor calls and manage the elevator's movements accordingly.

# Dependencies
- Java Development Kit (JDK) with version 19 or higher 
- An input file with elevator call requests for the FloorSubsystem


# Input File Format for Elevator Calls
The input file used by the FloorSubsystem to simulate the arrival of passengers and generate elevator calls follows a specific format. Each line in the file represents a single elevator call request and contains the following information:

1. **Timestamp**: The call time, formatted as HH:mm:ss.
2. **Starting Floor**: The originating floor number.
3. **Direction**: Specifies the direction (`Up` or `Down`).
4. **Target Floor**: The intended destination floor.

# Example Input
Below is an example of the input file content that the FloorSubsystem will parse:
14:04:50 1 Up 10
14:04:55 5 Up 8
14:05:25 6 Down 4
14:05:30 2 Up 7
14:05:35 1 Up 2
14:05:40 10 Down 1
14:05:45 1 Up 6

Input File Line Breakdown:
14:04:50 1 Up 10: At 14:05:15, a call is made from floor 1 to go up to floor 10.
14:04:55 5 Up 8: At 14:05:20, 8 calls are made from floor 5 to go up to floor 8.
14:05:25 6 Down 4: At 14:05:25, a call is made from floor 6 to go down to floor 4.
14:05:30 2 Up 7: At 14:05:30, a call is made from floor 2 to go up to floor 7.
14:05:35 1 Up 2: At 14:05:35, a call is made from floor 1 to go up to floor 2.
14:05:40 10 Down 1: At 14:05:40, a call is made from floor 10 to go down to floor 1.
14:05:45 1 Up 6: At 14:05:45, a call is made from floor 1 to go up to floor 6.

# Processing Input File
The FloorSubsystem reads each line from the input file, parsing the timestamp, starting floor, direction, and target floor. It simulates the delay between calls based on the timestamps and forwards the elevator call requests to the Scheduler, which then coordinates the servicing of these calls by the Elevator.

# Authors
Jaden Sutton
Adham Badawi
Amr Abdelazeem
Dana El Sherif
Yasmina Younes
Sameh Gawish

# Contributions
1. Scheduler UDP: Sameh Gawish, Jaden Sutton
2. ElevatorSubsystem UDP: Sameh Gawish, Yasmina Younes
3. FloorSubsystem UDP: Dana El Sherif, Amr Abdelazeem
4. Scheduler Multiple Elevators: Jaden Sutton, Amr Abdelazeem
5. ElevatorSybsystem Multiple Elevators: Jaden Sutton, Amr Abdelazeem, Adham Badawi
6. ElevatorCar: Jaden Sutton, Amr Abdelazeem
7. Passenger class: Adham Badawi
8. Assuring the total weight of passengers in an elevator car doesn't exceed 500 KGs: Adham Badawi, Yasmina Younes
9. Testing: Adham Badawi, Yasmina Younes, Dana El Sherif
10. Diagrams: Dana El Sherif, Yasmina Younes
11. Readme:Yasmina Younes

# State Machine Diagram
![State-Machine-Diagram (1) drawio](https://github.com/adhambadawi/SYSC3303Project/assets/89320833/4b064fdd-bb26-408b-a114-4615df293267)

# Sequence Diagram
<img width="1918" alt="Sequence Diagram - Template (Community)" src="https://github.com/adhambadawi/SYSC3303Project/assets/89320833/1c1a9a38-6f0d-43da-8aa3-79037ce0ee61">

# UML Class Diagram
![UML Class Diagram](https://github.com/adhambadawi/SYSC3303Project/assets/89320833/0b00b6aa-6d68-45dc-9db0-5c595e20cd41)

# Version History
- 1.00: Initial release
- 3.00: The new release includes dividing the system into separate programs communicating via UDP and creating multiple elevator cars that operate independently. We have also added a weight limit for each elevator car that we are assuring is not exceeded.