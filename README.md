# Network Tic-Tac-Toe with Java (Sockets & Threads)

This project, developed as part of Assignment 07 on Network and Threads API, implements a functional version of the classic Tic-Tac-Toe game for two players over a network.

## 📜 Description

The application uses the Client-Server model, where a central application (`TicTacToeServer`) manages the game state and rules, while two client applications (`TicTacToeClient`) connect to it to participate in the match. Communication is built using Java's Sockets API (TCP), and multi-player management is handled with the Threads API.

During development, the application of advanced design patterns like Model-View-Controller (MVC) and design principles such as SOLID and DRY was evaluated. However, for the scope of this project, whose main requirement is to demonstrate the Java Networking and Threads API, introducing these patterns would have added a significant number of classes and a level of object communication complexity that was deemed unnecessary.

Therefore, a more direct and cohesive approach was chosen, keeping the network, game, and interface logic in centralized classes (`TicTacToeServer` and `TicTacToeClient`). This decision prioritized clarity in the Socket communication flow and Thread management, which are the focus of the assignment.

## ✨ Features

* Multiplayer for 2 people on a local network.
* Simple graphical user interface built with Java Swing.
* Multi-threaded server that handles multiple clients simultaneously.
* Real-time communication between players.
* Win condition check functionality implemented on the server.

## 🛠️ Technologies Used

* **Java**: The primary language for the project.
* **Java Swing**: For building the graphical user interface (GUI).
* **Java Sockets API**: For network communication between the client and server.
* **Java Concurrency API (Threads & ExecutorService)**: For managing multiple clients on the server.

## 🚀 How to Run the Project

To run the game, follow the steps below:

1.  **Compile the Project:** Ensure all Java files (`.java`) have been compiled into (`.class`) files.
2.  **Start the Server:** Run the `TicTacToeServer` class. A log window will appear, waiting for connections.
3.  **Start the First Client:** Run the `TicTacToeClient` class. A game window will open for "Player X".
4.  **Start the Second Client:** Run the `TicTacToeClient` class a second time. A new window will open for "Player O". The match will start automatically.

> **Note:** To run two instances of the client from an IDE like IntelliJ, you need to edit the run configurations (`Run/Debug Configurations`) and check the "Allow multiple instances" option.

## 🏛️ Architecture

The project is divided into two main applications with internal helper classes.

### Class Descriptions

* **`TicTacToeServer.java`**
    * **Network Server:** Uses a `ServerSocket` to wait for and accept connections from two clients.
    * **Concurrency Management:** Employs an `ExecutorService` to dedicate a thread to each connected player.
    * **Game State Control:** Maintains the state of the board, controls turns, and contains the methods that validate moves and check for the end of the game.
    * **Synchronization:** Uses `Lock` and `Condition` objects to ensure a thread-safe access to the game state.
    * **Log Interface:** Presents a simple GUI to display a log of events.

* **`Player` (Inner Class of `TicTacToeServer`)**
    * Represents the communication logic for a single player on the server side.
    * Implements `Runnable` to be executed in a separate thread.
    * Controls the `ObjectInputStream` and `ObjectOutputStream` to receive and send data to the client.

* **`TicTacToeClient.java`**
    * **Network Client:** Uses a `Socket` to initiate a connection with the server.
    * **Graphical User Interface (GUI):** Renders the game window with the 3x3 board and a message area.
    * **Event Handling:** Captures the player's mouse clicks and sends the move to the server.
    * **Background Communication:** Implements `Runnable` so that listening for server messages occurs on a background thread, keeping the GUI responsive.

* **`Square` (Inner Class of `TicTacToeClient`)**
    * A visual component (`JPanel`) representing a single square on the board.
    * Has a `MouseListener` to detect clicks.
    * Is responsible for drawing the "X" or "O" mark.

### UML Diagram

The diagram below illustrates the class structure and their relationships.

<img width="655" height="434" alt="image" src="https://github.com/user-attachments/assets/f5cbc2e4-ec36-4c40-a7b5-99d148ef6715" />

📸 ### Game Screenshots

Below is an image of the application running, showing a completed match.

<img width="637" height="343" alt="image" src="https://github.com/user-attachments/assets/a5c48dac-8701-40db-9462-63ee4d3f5b81" />


