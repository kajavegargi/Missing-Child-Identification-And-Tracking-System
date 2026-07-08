package Dbms_proj;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ViewOpenCasesUI extends JFrame {

    private JTable caseTable;
    private JButton refreshButton, closeButton;

    public ViewOpenCasesUI() {
        setTitle(" View All Open Cases");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(" Open Missing Child Cases", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(heading, BorderLayout.NORTH);

        // Table setup
        caseTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(caseTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with buttons
        JPanel buttonPanel = new JPanel();
        refreshButton = new JButton(" Refresh");
        closeButton = new JButton(" Close");
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadOpenCases());
        closeButton.addActionListener(e -> dispose());

        // Initial load
        loadOpenCases();
    }

    private void loadOpenCases() {
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[]{
                "Case ID", "Child Name", "Date Reported", "Case Status", "Description"
        });

        String query = """
                SELECT lc.CaseID, CONCAT(p.FirstName, ' ', p.LastName) AS ChildName,
                       lc.DateReported, lc.CaseStatus, lc.Description
                FROM lost_case lc
                JOIN child c ON lc.ChildID = c.ChildID
                JOIN person p ON c.PersonID = p.PersonID
                WHERE lc.CaseStatus != 'Closed'
                ORDER BY lc.DateReported DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            boolean found = false;

            while (rs.next()) {
                found = true;
                model.addRow(new Object[]{
                        rs.getInt("CaseID"),
                        rs.getString("ChildName"),
                        rs.getDate("DateReported"),
                        rs.getString("CaseStatus"),
                        rs.getString("Description")
                });
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "No open cases found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }

            caseTable.setModel(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading open cases:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ViewOpenCasesUI().setVisible(true));
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
