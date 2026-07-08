package Dbms_proj;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SearchLostChildUI extends JFrame {

    private JTextField childNameField;
    private JButton searchBtn, resetBtn;
    private JTable resultTable;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/Dbms_proj";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "riya";

    public SearchLostChildUI() {
        setTitle("Search Lost Child & Verify Parent");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(" Search Lost Child & Top Parent Match", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(heading, BorderLayout.NORTH);

        // Search Panel
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Child Name:"));
        childNameField = new JTextField(20);
        searchPanel.add(childNameField);

        searchBtn = new JButton("Search");
        resetBtn = new JButton("Reset");
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        add(searchPanel, BorderLayout.NORTH);

        // Result Table
        resultTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(resultTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Actions
        searchBtn.addActionListener(e -> searchLostChild());
        resetBtn.addActionListener(e -> {
            childNameField.setText("");
            ((DefaultTableModel) resultTable.getModel()).setRowCount(0);
        });

        setVisible(true);
    }

    private void searchLostChild() {
        String childName = childNameField.getText().trim();
        if (childName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a child name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            // 1️ Fetch all children matching the name
            String childQuery = "SELECT c.ChildID, p.FirstName, p.LastName, c.DOB, c.DistinguishingMark, c.Status, d.DNASequence " +
                    "FROM child c " +
                    "JOIN person p ON c.PersonID = p.PersonID " +
                    "JOIN DNA_Profile d ON d.ChildID = c.ChildID " +
                    "WHERE p.FirstName LIKE ? OR p.LastName LIKE ?";
            PreparedStatement psChild = conn.prepareStatement(childQuery);
            psChild.setString(1, "%" + childName + "%");
            psChild.setString(2, "%" + childName + "%");
            ResultSet rsChild = psChild.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.setColumnIdentifiers(new Object[]{
                    "ChildID", "Child Name", "DOB", "Distinguishing Mark", "Status",
                    "Top Parent", "Match Accuracy (%)", "Parent Email"
            });

            boolean found = false;

            while (rsChild.next()) {
                found = true;
                int childId = rsChild.getInt("ChildID");
                String fullName = rsChild.getString("FirstName") + " " + rsChild.getString("LastName");
                Date dob = rsChild.getDate("DOB");
                String mark = rsChild.getString("DistinguishingMark");
                String status = rsChild.getString("Status");
                String childDNA = rsChild.getString("DNASequence");

                // 2️ Fetch all parents with DNA and email
                String parentQuery = "SELECT pr.ParentID, p.FirstName, p.LastName, d.DNASequence, pr.Email " +
                        "FROM parent pr " +
                        "JOIN person p ON pr.PersonID = p.PersonID " +
                        "JOIN DNA_Profile d ON d.ParentID = pr.ParentID";
                PreparedStatement psParent = conn.prepareStatement(parentQuery);
                ResultSet rsParent = psParent.executeQuery();

                String topParent = "N/A";
                double topAccuracy = 0.0;
                String parentEmail = "N/A";

                while (rsParent.next()) {
                    String parentName = rsParent.getString("FirstName") + " " + rsParent.getString("LastName");
                    String parentDNA = rsParent.getString("DNASequence");
                    double accuracy = calculateMatchAccuracy(childDNA, parentDNA);

                    if (accuracy > topAccuracy) {
                        topAccuracy = accuracy;
                        topParent = parentName;
                        parentEmail = rsParent.getString("Email");
                    }
                }

                model.addRow(new Object[]{
                        childId, fullName, dob, mark, status,
                        topParent, String.format("%.2f", topAccuracy), parentEmail
                });
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "No matching child records found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }

            resultTable.setModel(model);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error:\n" + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Simple DNA match accuracy calculation
    private double calculateMatchAccuracy(String childDNA, String parentDNA) {
        if (childDNA == null || parentDNA == null) return 0.0;
        int minLength = Math.min(childDNA.length(), parentDNA.length());
        int matches = 0;
        for (int i = 0; i < minLength; i++) {
            if (childDNA.charAt(i) == parentDNA.charAt(i)) matches++;
        }
        return ((double) matches / minLength) * 100;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SearchLostChildUI::new);
    }
}
