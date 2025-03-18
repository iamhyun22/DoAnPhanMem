package chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UserListForm extends JFrame {
    private DefaultListModel<String> listModel;
    private JList<String> userList;
    private JTextField searchField;
    private JButton refreshButton;
    private String loggedInUser;
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=ChatDB;encrypt=false;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123123";

    public UserListForm(String loggedInUser) {
        this.loggedInUser = loggedInUser;
        setTitle("Chọn người để chat");
        setSize(350, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        refreshButton = new JButton("🔄 Làm mới");
        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setFont(new Font("Arial", Font.PLAIN, 16));
        userList.setFixedCellHeight(30);

        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(refreshButton, BorderLayout.EAST);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadUsers();
        new Thread(this::refreshUserList).start();
        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) { 
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null && !selectedUser.equals(loggedInUser)) {
                        dispose();
                        new ChatForm(loggedInUser, selectedUser);
                    }
                }
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterUsers();
            }
        });
        refreshButton.addActionListener(e -> loadUsers());

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
private void loadUsers() {
        listModel.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM Users WHERE username <> ?")) {
            stmt.setString(1, loggedInUser);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                listModel.addElement(rs.getString("username"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách người dùng", "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void filterUsers() {
        String searchText = searchField.getText().trim().toLowerCase();
        listModel.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM Users WHERE username <> ?")) {
            stmt.setString(1, loggedInUser);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                if (username.toLowerCase().contains(searchText)) {
                    listModel.addElement(username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshUserList() {
        while (true) {
            try {
                Thread.sleep(5000);
                loadUsers();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

