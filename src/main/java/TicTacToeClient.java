import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicTacToeClient extends JFrame implements Runnable {
    private final JTextField idField;
    private final JTextArea displayArea;
    private final JPanel boardPanel;
    private final Square[] board;
    private Socket connection;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String myMark;
    private boolean myTurn;

    public TicTacToeClient(String host) {
        super("Tic-Tac-Toe Client");

        Container container = getContentPane();

        displayArea = new JTextArea(4, 30);
        displayArea.setEditable(false);
        container.add(new JScrollPane(displayArea), BorderLayout.SOUTH);

        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3, 0, 0));
        board = new Square[9];

        for (int i = 0; i < 9; i++) {
            board[i] = new Square(i);
            boardPanel.add(board[i]);
        }

        idField = new JTextField();
        idField.setEditable(false);
        container.add(idField, BorderLayout.NORTH);
        container.add(boardPanel, BorderLayout.CENTER);

        setSize(500, 550);
        setVisible(true);

        startClient(host);
    }

    public void startClient(String host) {
        try {
            connection = new Socket(InetAddress.getByName(host), 12345);
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.getCause();
        }
    }

    @Override
    public void run() {
        try {
            myMark = (String) input.readObject();
            idField.setText("You are player \"" + myMark + "\"");
            myTurn = myMark.equals("X");

            while (true) {
                String message = (String) input.readObject();
                processMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.getCause();
        }
    }

    private void processMessage(String message) {
        SwingUtilities.invokeLater(() -> displayArea.append(message + "\n"));

        switch (message) {
            case "Valid move.", "VICTORY", "DEFEAT", "DRAW":
                myTurn = false;
                break;
            case "Invalid move, try again":
            case "Opponent moved":
                try {
                    int location = (int) input.readObject();
                    board[location].setMark(myMark.equals("X") ? "O" : "X");
                    myTurn = true;
                } catch (IOException | ClassNotFoundException e) {
                    e.getCause();
                }
                break;
            default:
                myTurn = true;
                break;
        }
    }

    private void sendClickedSquare(int location) {
        if(myTurn) {
            try {
                output.writeObject(location);
                output.flush();
                myTurn = false; // Aguarda confirmação do servidor
            } catch (IOException e) {
                displayArea.append("Error writing object\n");
            }
        }
    }

    private class Square extends JPanel {
        private String mark = "";
        private final int location;

        public Square(int squareLocation) {
            this.location = squareLocation;
            this.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    sendClickedSquare(location);
                }
            });
        }

        public void setMark(String newMark) {
            this.mark = newMark;
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawRect(0, 0, getWidth(), getHeight());
            g.drawString(mark, 11, 20);
        }
    }

    public static void main(String[] args) {
        TicTacToeClient application = new TicTacToeClient("127.0.0.1");
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(application);
    }
}