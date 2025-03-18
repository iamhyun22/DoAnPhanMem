package chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ChatForm extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton, backButton, clearHistoryButton;
    private String loggedInUser, selectedUser;

    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=ChatDB;encrypt=false;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123123";

    public ChatForm(String loggedInUser, String selectedUser) {
        this.loggedInUser = loggedInUser;
        this.selectedUser = selectedUser;

        setTitle("💬 Chat với " + selectedUser);
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Khu vực hiển thị tin nhắn
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setBackground(new Color(240, 240, 240));
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        messageField.setPreferredSize(new Dimension(280, 35)); 

        sendButton = new JButton("Gửi");
        clearHistoryButton = new JButton("🗑 Xóa lịch sử");
        backButton = new JButton("⬅ Quay lại");

        sendButton.setBackground(new Color(50, 150, 250));
        sendButton.setForeground(Color.WHITE);

        clearHistoryButton.setBackground(new Color(220, 50, 50));
        clearHistoryButton.setForeground(Color.WHITE);

        backButton.setBackground(new Color(100, 180, 100));
        backButton.setForeground(Color.WHITE);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.add(backButton);
        buttonPanel.add(clearHistoryButton);

        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        inputPanel.add(buttonPanel, BorderLayout.NORTH);
        inputPanel.add(messagePanel, BorderLayout.SOUTH);
        
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
        sendButton.addActionListener(e -> sendMessage());
        clearHistoryButton.addActionListener(e -> clearChatHistory());
        backButton.addActionListener(e -> goBack());
        messageField.addActionListener(e -> sendMessage());

        loadMessages();
        new Thread(this::receiveMessages).start();

        setVisible(true);
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
  private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO Messages (sender_id, receiver_id, content) " +
                         "VALUES ((SELECT user_id FROM Users WHERE username = ?), " +
                         "(SELECT user_id FROM Users WHERE username = ?), ?)")) {
                stmt.setString(1, loggedInUser);
                stmt.setString(2, selectedUser);
                stmt.setString(3, message);
                stmt.executeUpdate();

                chatArea.append("Me: " + message + "\n");
                messageField.setText("");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMessages() {
        chatArea.setText("");
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT u.username, m.content FROM Messages m " +
                     "JOIN Users u ON m.sender_id = u.user_id " +
                     "WHERE (m.sender_id = (SELECT user_id FROM Users WHERE username = ?) " +
                     "AND m.receiver_id = (SELECT user_id FROM Users WHERE username = ?)) " +
                     "OR (m.sender_id = (SELECT user_id FROM Users WHERE username = ?) " +
                     "AND m.receiver_id = (SELECT user_id FROM Users WHERE username = ?)) " +
                     "ORDER BY m.timestamp")) {
            stmt.setString(1, loggedInUser);
            stmt.setString(2, selectedUser);
            stmt.setString(3, selectedUser);
            stmt.setString(4, loggedInUser);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                chatArea.append(rs.getString("username") + ": " + rs.getString("content") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        while (true) {
            try {
                Thread.sleep(2000);
                loadMessages();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void clearChatHistory() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa lịch sử trò chuyện?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM Messages WHERE (sender_id = (SELECT user_id FROM Users WHERE username = ?) " +
                         "AND receiver_id = (SELECT user_id FROM Users WHERE username = ?)) " +
                         "OR (sender_id = (SELECT user_id FROM Users WHERE username = ?) " +
                         "AND receiver_id = (SELECT user_id FROM Users WHERE username = ?))")) {

                stmt.setString(1, loggedInUser);
                stmt.setString(2, selectedUser);
                stmt.setString(3, selectedUser);
                stmt.setString(4, loggedInUser);
                stmt.executeUpdate();

                chatArea.setText("");
                JOptionPane.showMessageDialog(this, "Lịch sử trò chuyện đã được xóa.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void goBack() {
        dispose();
        new UserListForm(loggedInUser);
    }
}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

