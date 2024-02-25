# SYSC3303Project
# Elevator System Simulation
This documentation provides an overview of the Elevator System Simulation project. The project simulates the operation of an elevator system within a building, handling elevator calls from different floors and directing the elevator to service these calls efficiently.

# Overview
The system comprises several key components:

ElevatorTrip: Represents a request for elevator service, including details such as the time of the call, the starting floor, the target floor(s), and the direction of travel.

Elevator: Manages the movement of the elevator, including opening and closing doors, moving between floors, and servicing ElevatorTrip requests.

FloorSubsystem: Simulates the arrival of passengers at various floors and generates ElevatorTrip requests based on input data.

Scheduler: Coordinates between FloorSubsystem and Elevator, ensuring that elevator calls are serviced in an efficient manner. It operates on a singleton design pattern to ensure only one instance manages the elevator system.

# Features
Elevator Call Handling: Processes elevator calls with details such as timestamp, starting floor, and target floor, and direction.

Elevator Movement Simulation: Simulates the elevator's movement between floors, including door opening and closing mechanisms.

Call Merging: Attempts to merge incoming elevator calls with existing ones to improve system efficiency.

Floor Subsystem Simulation: Reads elevator call requests from an input file and simulates the time delay between calls.

Scheduler Coordination: Manages the queue of pending elevator calls and assigns them to the elevator, ensuring efficient service.

# Usage
To use the simulation, compile and run the Scheduler class. The system will start processing elevator calls based on the predefined input file for floor calls and manage the elevator's movements accordingly.

# Dependencies
Java Development Kit (JDK)
An input file containing elevator call requests (for FloorSubsystem)


# Input File Format for Elevator Calls
The input file used by the FloorSubsystem to simulate the arrival of passengers and generate elevator calls follows a specific format. Each line in the file represents a single elevator call request and contains the following information:

1. Timestamp: The time at which the elevator call is made, formatted as HH:mm:ss.
2. Starting Floor: The floor number from which the call is made.
3. Direction: Indicates whether the call is to go "Up" or "Down".
4. Target Floor: The destination floor number.

# Example Input
Below is an example of the input file content that the FloorSubsystem will parse:
14:05:15 1 Up 10
14:05:20 5 Up 8
14:05:25 6 Down 4
14:05:30 2 Up 7
14:05:35 1 Up 2
14:05:40 10 Down 1
14:05:45 1 Up 6

Input File Line Breakdown:
14:05:15 1 Up 10: At 14:05:15, a call is made from floor 1 to go up to floor 10.
14:05:20 5 Up 8: At 14:05:20, a call is made from floor 5 to go up to floor 8.
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
1. Code Design and brainstorming: All members contributed to the code design.
2. Floor Subsystem Code Implementation: Jaden Sutton
3. Elevator Subsystem Code Implementation: Adham Badawi, Jaden Sutton, Amr Abdelazeem, Yasmina Younes
4. Scheduler Code Implementation: Amr Abdelazeem, Adham Badawi, Jaden Sutton, Dana El Sherif, Sameh Gawish
5. Sequence Diagram: Dana El Sherif
6. UML Class Diagram: Sameh Gawish
7. Readme: Yasmina Younes

# State Machine Diagram
![State-Machine-Diagram (1) drawio](https://github.com/adhambadawi/SYSC3303Project/assets/89320833/4b064fdd-bb26-408b-a114-4615df293267)

# Sequence Diagram
<img width="1918" alt="Sequence Diagram - Template (Community)" src="https://github.com/adhambadawi/SYSC3303Project/assets/89320833/1c1a9a38-6f0d-43da-8aa3-79037ce0ee61">

# Version History
1.00: Initial release


