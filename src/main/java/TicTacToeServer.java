import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TicTacToeServer extends JFrame {
    private final String[] board = new String[9];
    private final JTextArea outputArea;
    private final Player[] players;
    private ServerSocket server;
    private int currentPlayer;
    private final static int PLAYER_X = 0;
    private final static int PLAYER_O = 1;
    private final static String[] MARKS = {"X", "O"};
    private final ExecutorService runGame;
    private final Lock gameLock;
    private final Condition otherPlayerConnected;
    private final Condition otherPlayerTurn;

    public TicTacToeServer() {
        super("Tic-Tac-Toe Server");

        runGame = Executors.newFixedThreadPool(2);
        gameLock = new ReentrantLock();
        otherPlayerConnected = gameLock.newCondition();
        otherPlayerTurn = gameLock.newCondition();

        for (int i = 0; i < 9; i++) {
            board[i] = "";
        }
        players = new Player[2];
        currentPlayer = PLAYER_X;

        outputArea = new JTextArea();
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        setSize(500, 500);
        setVisible(true);
    }

    public void execute() {
        try {
            server = new ServerSocket(12345, 2);
            outputArea.append("Server waiting for connections\n");

            for (int i = 0; i < players.length; i++) {
                try {
                    players[i] = new Player(server.accept(), i);
                    runGame.execute(players[i]);
                } catch (IOException e) {
                    e.getCause();
                    System.exit(1);
                }
            }

            gameLock.lock();
            try {
                players[PLAYER_X].setSuspended(false);
                otherPlayerConnected.signal();
            } finally {
                gameLock.unlock();
            }

        } catch (IOException e) {
            e.getCause();
            System.exit(1);
        }
    }

    public boolean isGameWon() {

        for (int i = 0; i < 9; i += 3) {
            if (!board[i].isEmpty() && board[i].equals(board[i + 1]) && board[i].equals(board[i + 2])) return true;
        }
        for (int i = 0; i < 3; ++i) {
            if (!board[i].isEmpty() && board[i].equals(board[i + 3]) && board[i].equals(board[i + 6])) return true;
        }
        if (!board[0].isEmpty() && board[0].equals(board[4]) && board[0].equals(board[8])) return true;
        return !board[2].isEmpty() && board[2].equals(board[4]) && board[2].equals(board[6]);
    }

    public boolean isBoardFull() {
        for (String s : board) {
            if (s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean validateAndMove(int location, int player) {
        if (player != currentPlayer) {
            return false;
        }

        if (board[location].isEmpty()) {
            board[location] = MARKS[currentPlayer];
            currentPlayer = (currentPlayer + 1) % 2;

            players[currentPlayer].otherPlayerMoved(location);

            gameLock.lock();
            try {
                otherPlayerTurn.signal();
            } finally {
                gameLock.unlock();
            }

            return true;
        }
        return false;
    }

    private class Player implements Runnable {
        private final Socket connection;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private final int playerNumber;
        private final String mark;
        private boolean suspended = true;

        public Player(Socket socket, int number) {
            this.connection = socket;
            this.playerNumber = number;
            this.mark = MARKS[playerNumber];

            try {
                output = new ObjectOutputStream(connection.getOutputStream());
                output.flush();
                input = new ObjectInputStream(connection.getInputStream());
                outputArea.append("Player " + mark + " connected\n");
            } catch (IOException e) {
                e.getCause();
            }
        }

        public void otherPlayerMoved(int location) {
            try {
                output.writeObject("Opponent moved");
                output.writeObject(location);
                output.flush();
            } catch (IOException e) {
                e.getCause();
            }
        }

        @Override
        public void run() {
            try {
                output.writeObject(mark); // Envia a marca do jogador (X ou O)
                output.flush();
                output.writeObject("Player " + (playerNumber == PLAYER_X ? "X" : "O") + " connected");
                output.flush();

                if (playerNumber == PLAYER_X) {
                    output.writeObject("Waiting for another player");
                    output.flush();
                    gameLock.lock();
                    try {
                        while (suspended) {
                            otherPlayerConnected.await();
                        }
                    } catch (InterruptedException e) {
                        e.getCause();
                    } finally {
                        gameLock.unlock();
                    }
                    output.writeObject("Other player connected. Your move.");
                    output.flush();
                } else {
                    output.writeObject("Waiting for player X's move");
                    output.flush();
                }

                while (true) {
                    int location = (int) input.readObject();
                    if (validateAndMove(location, this.playerNumber)) {
                        output.writeObject("Valid move.");
                        output.flush();

                        if (isGameWon()) {
                            output.writeObject("VICTORY");
                            output.flush();
                            players[currentPlayer].output.writeObject("DEFEAT"); // Notifica o outro
                            players[currentPlayer].output.flush();
                            break;
                        } else if (isBoardFull()) {
                            output.writeObject("DRAW");
                            output.flush();
                            players[currentPlayer].output.writeObject("DRAW");
                            players[currentPlayer].output.flush();
                            break;
                        }
                    } else {
                        output.writeObject("Invalid move, try again");
                        output.flush();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.getCause();
            } finally {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.getCause();
                }
            }
        }

        public void setSuspended(boolean status) {
            this.suspended = status;
        }
    }

    public static void main(String[] args) {
        TicTacToeServer application = new TicTacToeServer();
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.execute();
    }
}