package chat;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;

public class Server {
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=ChatDB;encrypt=false;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123123";

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chat Server");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(45, 45, 45));
        
        JLabel titleLabel = new JLabel("Chat Server", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(30, 30, 30));
        chatArea.setForeground(Color.GREEN);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        frame.add(panel);
        frame.setVisible(true);

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            chatArea.append("Server is running on port 12345...\n");

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, chatArea).start();
            }
        } catch (IOException e) {
            chatArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private JTextArea chatArea;

        public ClientHandler(Socket socket, JTextArea chatArea) {
            this.socket = socket;
            this.chatArea = chatArea;
        }

        public void run() {
            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                out = writer;
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = reader.readLine()) != null) {
                    String formattedMessage = "Client: " + message;
                    SwingUtilities.invokeLater(() -> chatArea.append(formattedMessage + "\n"));
                    saveMessageToDatabase("Client", message);

                    synchronized (clientWriters) {
                        for (PrintWriter writerOut : clientWriters) {
                            writerOut.println(formattedMessage);
                        }
                    }
                }
            } catch (IOException e) {
                chatArea.append("Connection error: " + e.getMessage() + "\n");
            } finally {
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }
    }

    private static void saveMessageToDatabase(String sender, String message) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO Messages (sender, content, timestamp) VALUES (?, ?, CURRENT_TIMESTAMP)")) {
            stmt.setString(1, sender);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}