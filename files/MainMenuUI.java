package Dbms_proj;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle(" Child Rescue Management System");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        // ---- Title ----
        JLabel title = new JLabel(" Missing Child Tracking & DNA Matching System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // ---- Buttons Panel ----
        JPanel btnPanel = new JPanel(new GridLayout(6, 1, 20, 15));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));

        JButton addPersonBtn = new JButton(" Add Person (Child / Parent)");
        JButton reportMissingBtn = new JButton(" Report Missing Child");
        JButton searchMatchBtn = new JButton(" Search Lost Child / Parent Match");
        JButton updateCaseBtn = new JButton(" Update Case Status");
        JButton viewOpenCasesBtn = new JButton(" View All Open Cases");
        JButton exitBtn = new JButton(" Exit Application");

        Font btnFont = new Font("Segoe UI", Font.BOLD, 16);
        for (JButton b : new JButton[]{addPersonBtn, reportMissingBtn, searchMatchBtn, updateCaseBtn, viewOpenCasesBtn, exitBtn}) {
            b.setFont(btnFont);
            b.setFocusPainted(false);
            b.setBackground(new Color(230, 240, 255));
            b.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 200), 2, true));
            btnPanel.add(b);
        }

        add(btnPanel, BorderLayout.CENTER);

        // ---- Footer ----
        JLabel footer = new JLabel("Developed by Your Team | DBMS Project 2025", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footer.setForeground(Color.DARK_GRAY);
        add(footer, BorderLayout.SOUTH);

        // ---- Button Actions ----
        addPersonBtn.addActionListener(e -> new AddPersonForm().setVisible(true));
        reportMissingBtn.addActionListener(e -> new ReportMissingUI().setVisible(true));
        searchMatchBtn.addActionListener(e -> new SearchLostChildUI().setVisible(true));
        updateCaseBtn.addActionListener(e -> new UpdateCaseStatusUI().setVisible(true));
        viewOpenCasesBtn.addActionListener(e -> new ViewOpenCasesUI().setVisible(true));

        exitBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
                    "Exit Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}
