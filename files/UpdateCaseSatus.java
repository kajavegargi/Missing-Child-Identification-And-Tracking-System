package Dbms_proj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UpdateCaseStatusUI extends JFrame {

    private JComboBox<Integer> caseIdComboBox;
    private JComboBox<String> statusComboBox;
    private JTextArea descriptionArea;
    private JLabel childInfoLabel;
    private JButton updateButton;

    public UpdateCaseStatusUI() {
        setTitle("Update Case Status");
        setSize(500, 450);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ---- North panel (heading) ----
        JLabel heading = new JLabel("Update Missing Case Status", JLabel.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(heading, BorderLayout.NORTH);

        // ---- Center panel ----
        JPanel centerPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        centerPanel.add(new JLabel("Select Case ID:"));
        caseIdComboBox = new JComboBox<>();
        populateCaseIDs();
        centerPanel.add(caseIdComboBox);

        centerPanel.add(new JLabel("Child Info:"));
        childInfoLabel = new JLabel("-");
        centerPanel.add(childInfoLabel);

        centerPanel.add(new JLabel("New Case Status:"));
        statusComboBox = new JComboBox<>(new String[]{"Open", "Under Investigation", "Closed"});
        centerPanel.add(statusComboBox);

        centerPanel.add(new JLabel("Case Description:"));
        descriptionArea = new JTextArea(3, 20);
        centerPanel.add(new JScrollPane(descriptionArea));

        add(centerPanel, BorderLayout.CENTER);

        // ---- South panel (button) ----
        updateButton = new JButton("Update Status");
        updateButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(updateButton, BorderLayout.SOUTH);

        // Event listeners
        updateButton.addActionListener(e -> updateCaseStatus());
        caseIdComboBox.addActionListener(e -> displayChildInfo());
    }

    // Populate case IDs from DB
    private void populateCaseIDs() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT CaseID FROM lost_case");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                caseIdComboBox.addItem(rs.getInt("CaseID"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching case IDs: " + e.getMessage());
        }
    }

    // Display associated child's name and status
    private void displayChildInfo() {
        Integer caseId = (Integer) caseIdComboBox.getSelectedItem();
        if (caseId == null) return;

        String query = """
                SELECT p.FirstName, p.LastName, c.Status
                FROM lost_case lc
                JOIN child c ON lc.ChildID = c.ChildID
                JOIN person p ON c.PersonID = p.PersonID
                WHERE lc.CaseID = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                    String status = rs.getString("Status");
                    childInfoLabel.setText(name + " (" + status + ")");
                } else {
                    childInfoLabel.setText("-");
                }
            }

        } catch (SQLException e) {
            childInfoLabel.setText("Error fetching child info");
        }
    }

    // Update case status
    private void updateCaseStatus() {
        Integer caseId = (Integer) caseIdComboBox.getSelectedItem();
        String newStatus = (String) statusComboBox.getSelectedItem();
        String description = descriptionArea.getText();

        if (caseId == null) {
            JOptionPane.showMessageDialog(this, "Please select a case ID.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE lost_case SET CaseStatus = ?, Description = ? WHERE CaseID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setString(2, description);
            ps.setInt(3, caseId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, " Case status updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "No case found with that ID.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UpdateCaseStatusUI().setVisible(true));
    }
}

class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Dbms_proj";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "riya";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
