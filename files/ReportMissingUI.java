package Dbms_proj;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class ReportMissingUI extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/Dbms_proj";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "riya";

    private JTextField fnameField, lnameField, dobField, markField, dnaField;
    private JButton reportBtn, cancelBtn;

    public ReportMissingUI() {
        setTitle("Report Missing Child");
        setSize(520, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel(" Report Missing Child", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        formPanel.add(new JLabel("Child First Name:"));
        fnameField = new JTextField();
        formPanel.add(fnameField);

        formPanel.add(new JLabel("Child Last Name:"));
        lnameField = new JTextField();
        formPanel.add(lnameField);

        formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"));
        dobField = new JTextField();
        formPanel.add(dobField);

        formPanel.add(new JLabel("Distinguishing Mark:"));
        markField = new JTextField();
        formPanel.add(markField);

        formPanel.add(new JLabel("DNA Sequence (optional):"));
        dnaField = new JTextField();
        formPanel.add(dnaField);

        reportBtn = new JButton("Report Missing");
        cancelBtn = new JButton("Cancel");
        formPanel.add(reportBtn);
        formPanel.add(cancelBtn);

        add(formPanel, BorderLayout.CENTER);

        reportBtn.addActionListener(e -> reportMissingChild());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void reportMissingChild() {
        String fname = fnameField.getText().trim();
        String lname = lnameField.getText().trim();
        String dob = dobField.getText().trim();
        String mark = markField.getText().trim();
        String dnaSeq = dnaField.getText().trim();

        if (fname.isEmpty() || lname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the child's first and last name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            con.setAutoCommit(false);

            int childId = 0;
            boolean childExists = false;

            // 1️ Check if child already exists
            String checkQuery;
            PreparedStatement psCheck;
            if (dob.isEmpty()) {
                checkQuery = "SELECT c.ChildID FROM child c " +
                        "JOIN person p ON c.PersonID = p.PersonID " +
                        "WHERE p.FirstName = ? AND p.LastName = ?";
                psCheck = con.prepareStatement(checkQuery);
                psCheck.setString(1, fname);
                psCheck.setString(2, lname);
            } else {
                checkQuery = "SELECT c.ChildID FROM child c " +
                        "JOIN person p ON c.PersonID = p.PersonID " +
                        "WHERE p.FirstName = ? AND p.LastName = ? AND c.DOB = ?";
                psCheck = con.prepareStatement(checkQuery);
                psCheck.setString(1, fname);
                psCheck.setString(2, lname);
                psCheck.setString(3, dob);
            }

            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next()) {
                childExists = true;
                childId = rsCheck.getInt("ChildID");
            }

            // 2️ If exists → update status
            if (childExists) {
                String updateChild = "UPDATE child SET Status = 'Missing' WHERE ChildID = ?";
                PreparedStatement psUpdate = con.prepareStatement(updateChild);
                psUpdate.setInt(1, childId);
                psUpdate.executeUpdate();
            } else {
                // 3️ Else insert new person + child
                String insertPerson = "INSERT INTO person (FirstName, LastName, Role) VALUES (?, ?, 'child')";
                PreparedStatement psPerson = con.prepareStatement(insertPerson, Statement.RETURN_GENERATED_KEYS);
                psPerson.setString(1, fname);
                psPerson.setString(2, lname);
                psPerson.executeUpdate();

                ResultSet rsPerson = psPerson.getGeneratedKeys();
                int personId = 0;
                if (rsPerson.next()) personId = rsPerson.getInt(1);

                String insertChild = "INSERT INTO child (PersonID, DOB, DistinguishingMark, Status) VALUES (?, ?, ?, 'Missing')";
                PreparedStatement psChild = con.prepareStatement(insertChild, Statement.RETURN_GENERATED_KEYS);
                psChild.setInt(1, personId);
                psChild.setString(2, dob.isEmpty() ? null : dob);
                psChild.setString(3, mark);
                psChild.executeUpdate();

                ResultSet rsChild = psChild.getGeneratedKeys();
                if (rsChild.next()) childId = rsChild.getInt(1);

                if (!dnaSeq.isEmpty()) {
                    String insertDNA = "INSERT INTO DNA_Profile (ChildID, SampleData, DNASequence) VALUES (?, ?, ?)";
                    PreparedStatement psDNA = con.prepareStatement(insertDNA);
                    psDNA.setInt(1, childId);
                    psDNA.setString(2, fname + "_" + lname + "_Sample");
                    psDNA.setString(3, dnaSeq);
                    psDNA.executeUpdate();
                }
            }

            // 4️ Always insert new case in lost_case
            String insertCase = "INSERT INTO lost_case (CaseStatus, Description, DateReported, ChildID) VALUES (?, ?, ?, ?)";
            PreparedStatement psCase = con.prepareStatement(insertCase);
            psCase.setString(1, "Open");
            psCase.setString(2, "Child reported missing.");
            psCase.setDate(3, Date.valueOf(LocalDate.now()));
            psCase.setInt(4, childId);
            psCase.executeUpdate();

            con.commit();

            JOptionPane.showMessageDialog(this,
                    childExists ? "Existing child marked as missing and new case created!" :
                            "New missing child reported successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            clearFields();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        fnameField.setText("");
        lnameField.setText("");
        dobField.setText("");
        markField.setText("");
        dnaField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReportMissingUI().setVisible(true));
    }
}
