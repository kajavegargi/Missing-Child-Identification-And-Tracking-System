package Dbms_proj;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddPersonForm extends JFrame {

    private static final String URL = "jdbc:mysql://localhost:3306/Dbms_proj";
    private static final String USER = "root";
    private static final String PASSWORD = "riya";

    private JTextField fnameField, lnameField, dobField, markField, statusField, dnaField, emailField;
    private JComboBox<String> personTypeBox;

    public AddPersonForm() {
        setTitle("Add Person Record");
        setSize(500, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Add Person Record", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Fields
        formPanel.add(new JLabel("First Name:"));
        fnameField = new JTextField();
        formPanel.add(fnameField);

        formPanel.add(new JLabel("Last Name:"));
        lnameField = new JTextField();
        formPanel.add(lnameField);

        formPanel.add(new JLabel("Person Type:"));
        personTypeBox = new JComboBox<>(new String[]{"Child", "Parent"});
        formPanel.add(personTypeBox);

        formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"));
        dobField = new JTextField();
        formPanel.add(dobField);

        formPanel.add(new JLabel("Distinguishing Mark (Child only):"));
        markField = new JTextField();
        formPanel.add(markField);

        formPanel.add(new JLabel("Status (Child only):"));
        statusField = new JTextField("Missing");
        formPanel.add(statusField);

        formPanel.add(new JLabel("DNA Sequence:"));
        dnaField = new JTextField();
        formPanel.add(dnaField);

        formPanel.add(new JLabel("Email (Parent only):"));
        emailField = new JTextField();
        formPanel.add(emailField);

        JButton saveBtn = new JButton("Save Record");
        JButton cancelBtn = new JButton("Cancel");
        formPanel.add(saveBtn);
        formPanel.add(cancelBtn);

        add(formPanel, BorderLayout.CENTER);

        // Button actions
        saveBtn.addActionListener(e -> addPersonToDB());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void addPersonToDB() {
        String fname = fnameField.getText().trim();
        String lname = lnameField.getText().trim();
        String personType = ((String) personTypeBox.getSelectedItem()).toLowerCase();
        String dob = dobField.getText().trim();
        String mark = markField.getText().trim();
        String status = statusField.getText().trim();
        String dnaSeq = dnaField.getText().trim();
        String email = emailField.getText().trim();

        if (fname.isEmpty() || lname.isEmpty() || dnaSeq.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in required fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            // 1️ Insert into person table
            String insertPerson = "INSERT INTO person (FirstName, LastName, Role) VALUES (?, ?, ?)";
            PreparedStatement psPerson = con.prepareStatement(insertPerson, Statement.RETURN_GENERATED_KEYS);
            psPerson.setString(1, fname);
            psPerson.setString(2, lname);
            psPerson.setString(3, personType);
            psPerson.executeUpdate();

            ResultSet rsPerson = psPerson.getGeneratedKeys();
            int personId = 0;
            if (rsPerson.next()) personId = rsPerson.getInt(1);

            // 2️ Fetch trigger-generated ID from child/parent table
            int entityId = 0;
            if (personType.equals("child")) {
                String fetchChild = "SELECT ChildID FROM child WHERE PersonID = ?";
                PreparedStatement psFetch = con.prepareStatement(fetchChild);
                psFetch.setInt(1, personId);
                ResultSet rs = psFetch.executeQuery();
                if (rs.next()) entityId = rs.getInt("ChildID");
            } else {
                String fetchParent = "SELECT ParentID FROM parent WHERE PersonID = ?";
                PreparedStatement psFetch = con.prepareStatement(fetchParent);
                psFetch.setInt(1, personId);
                ResultSet rs = psFetch.executeQuery();
                if (rs.next()) entityId = rs.getInt("ParentID");

                // Update Email for parent
                String updateParent = "UPDATE parent SET Email = ? WHERE ParentID = ?";
                PreparedStatement psUpdate = con.prepareStatement(updateParent);
                psUpdate.setString(1, email);
                psUpdate.setInt(2, entityId);
                psUpdate.executeUpdate();
            }

            // 3️ Insert into DNA_Profile
            String insertDNA;
            if (personType.equals("child")) {
                insertDNA = "INSERT INTO DNA_Profile (ChildID, SampleData, DNASequence) VALUES (?, ?, ?)";
            } else {
                insertDNA = "INSERT INTO DNA_Profile (ParentID, SampleData, DNASequence) VALUES (?, ?, ?)";
            }
            PreparedStatement psDNA = con.prepareStatement(insertDNA);
            psDNA.setInt(1, entityId);
            psDNA.setString(2, fname + "_" + lname + "_DNA");
            psDNA.setString(3, dnaSeq);
            psDNA.executeUpdate();

            JOptionPane.showMessageDialog(this, "Record and DNA saved successfully!");
            clearFields();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        fnameField.setText("");
        lnameField.setText("");
        dobField.setText("");
        markField.setText("");
        statusField.setText("Missing");
        dnaField.setText("");
        emailField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AddPersonForm().setVisible(true));
    }
}
