import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Dashboard extends JFrame {

    private static final String DATABASE_URL = "jdbc:mysql://127.0.0.1:3306/SSCM";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "_cryponymous_";

    private JTextField usernameField;
    private JTextField candidateIdField;
    private JButton loginButton, signUpButton;

    public Dashboard() {
        setTitle("Dashboard");
        setSize(900, 600); // Set initial size of the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Load the logo image and resize it
        ImageIcon logoIcon = createResizedImageIcon("/Users/ds/Desktop/StudyMaterials/NTCC_4thYear/logo.jpg", 90, 80); // Adjust dimensions as needed
        JLabel logoLabel = new JLabel(logoIcon);

        // Letterhead panel with company details
        JPanel letterheadPanel = new JPanel();
        letterheadPanel.setBackground(Color.decode("#6495ED")); // Light blue color
        letterheadPanel.setPreferredSize(new Dimension(getWidth(), 150)); // Increased height to accommodate content
        letterheadPanel.setLayout(new BorderLayout());
        letterheadPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add logo to the left of the organization name in a square box
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoPanel.setPreferredSize(new Dimension(90, 80)); // Adjusted size for the logo panel
        logoPanel.setBackground(Color.WHITE); // White background for logo square
        logoPanel.add(logoLabel);
        letterheadPanel.add(logoPanel, BorderLayout.WEST);

        // Company name centered at the top
        JLabel companyNameLabel = new JLabel("Self Study Centre Management System");
        companyNameLabel.setFont(new Font("Arial", Font.BOLD, 35));
        companyNameLabel.setForeground(Color.WHITE); // White text color
        companyNameLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center align the text
        letterheadPanel.add(companyNameLabel, BorderLayout.CENTER);

        // Contact details including phone number and email
        JPanel contactDetailsPanel = new JPanel();
        contactDetailsPanel.setBackground(Color.decode("#6495ED")); // Light blue color
        contactDetailsPanel.setLayout(new BoxLayout(contactDetailsPanel, BoxLayout.Y_AXIS));
        contactDetailsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel phoneLabel = new JLabel("Phone no: +91 9155789755");
        phoneLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        phoneLabel.setForeground(Color.WHITE); // White text color
        phoneLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align horizontally
        contactDetailsPanel.add(phoneLabel);

        JLabel emailLabel = new JLabel("Mail id: helpdesk121@selfstudy.com");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        emailLabel.setForeground(Color.WHITE); // White text color
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align horizontally
        contactDetailsPanel.add(emailLabel);

        letterheadPanel.add(contactDetailsPanel, BorderLayout.SOUTH);
        add(letterheadPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        // Username input field and candidate ID field in a single panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Login"));
        loginPanel.setPreferredSize(new Dimension(300, 150));

        usernameField = new JTextField(20);
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);

        candidateIdField = new JTextField(20);
        loginPanel.add(new JLabel("Candidate ID:"));
        loginPanel.add(candidateIdField);

        // Login button centered within the login panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton = new JButton("Login");
        loginButton.addActionListener(new LoginAction());
        buttonPanel.add(loginButton);

        loginPanel.add(buttonPanel); // Add button panel to login panel
        mainPanel.add(loginPanel);

        // Sign up button
        signUpButton = new JButton("Sign Up");
        signUpButton.addActionListener(e -> new EnrolmentForm());
        mainPanel.add(signUpButton);

        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // Method to create a resized ImageIcon from a file path
    private ImageIcon createResizedImageIcon(String imagePath, int width, int height) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
        return new ImageIcon(resizedImg);
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String candidateIdStr = candidateIdField.getText();

            if (username.isEmpty() || candidateIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(Dashboard.this, "Please enter both Username and Candidate ID.");
                return;
            }

            try {
                int candidateId = Integer.parseInt(candidateIdStr);
                if (username.equalsIgnoreCase("admin") && candidateId == 1) {
                    // Admin login handling
                    new CandidateDetailsWithFilter();
                } else {
                    checkCandidateLoginByNameAndId(username, candidateId);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(Dashboard.this, "Invalid Candidate ID format.");
            }
        }

        private void checkCandidateLoginByNameAndId(String name, int id) {
            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM candidate WHERE c_name = ? AND c_id = ?")) {
                statement.setString(1, name);
                statement.setInt(2, id);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    showCandidateDetails(resultSet);
                } else {
                    JOptionPane.showMessageDialog(Dashboard.this, "Invalid Username (Name) or Candidate ID");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(Dashboard.this, "Error: " + ex.getMessage());
            }
        }

        private void showCandidateDetails(ResultSet resultSet) throws SQLException {
            JFrame candidateFrame = new JFrame("Candidate Details");
            candidateFrame.setSize(400, 300);
            candidateFrame.setLayout(new GridLayout(0, 2));
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                candidateFrame.add(new JLabel(resultSet.getMetaData().getColumnName(i)));
                candidateFrame.add(new JLabel(resultSet.getString(i)));
            }
            candidateFrame.setVisible(true);
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            new Dashboard();
        });
    }
}
