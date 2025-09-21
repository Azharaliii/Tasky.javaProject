import javax.swing.*;
import java.awt.*;
import java.sql.*;

class Taskistan {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/taskistan_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    public static void main(String[] args) {
        // Load MySQL JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found!");
            System.exit(1);
        }

        SwingUtilities.invokeLater(Taskistan::showFrontPage);
    }

    private static void showFrontPage() {
        JFrame frame = new JFrame("Tasky – Local Job Portal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 350);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        JLabel title = new JLabel("Tasky");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(33, 37, 41));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Local Job Portal");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 17));  // Bold and slightly larger
        subtitle.setForeground(new Color(70, 70, 70));  // Darker gray for better contrast
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));  // Add some space below

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JButton loginBtn = createStyledButton("Login");
        JButton registerBtn = createStyledButton("Register");

        loginBtn.addActionListener(e -> showLoginOptions());
        registerBtn.addActionListener(e -> showRegisterOptions());

        panel.add(loginBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(registerBtn);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setBackground(new Color(0, 123, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private static void showLoginOptions() {
        String[] options = {"Job Seeker", "Job Creator"};
        int choice = JOptionPane.showOptionDialog(null, "Login as:", "Login",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) loginJobSeeker();
        else if (choice == 1) loginJobCreator();
    }

    private static void showRegisterOptions() {
        String[] options = {"Job Seeker", "Job Creator"};
        int choice = JOptionPane.showOptionDialog(null, "Register as:", "Register",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) registerJobSeeker();
        else if (choice == 1) registerJobCreator();
    }

    private static void loginJobSeeker() {
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] fields = {"Email:", emailField, "Password:", passwordField};

        int result = JOptionPane.showConfirmDialog(null, fields, "Job Seeker Login", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String query = "SELECT * FROM job_seekers WHERE email=? AND password=?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, emailField.getText());
                stmt.setString(2, new String(passwordField.getPassword()));

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int seekerId = rs.getInt("id");
                    String name = rs.getString("name");
                    JOptionPane.showMessageDialog(null, "Welcome, " + name);
                    viewJobs(seekerId);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials!");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
            }
        }
    }

    private static void loginJobCreator() {
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] fields = {"Email:", emailField, "Password:", passwordField};

        int result = JOptionPane.showConfirmDialog(null, fields, "Job Creator Login", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String query = "SELECT * FROM job_creators WHERE email=? AND password=?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, emailField.getText());
                stmt.setString(2, new String(passwordField.getPassword()));

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int creatorId = rs.getInt("id");
                    String companyName = rs.getString("company_name");
                    JOptionPane.showMessageDialog(null, "Welcome, " + companyName);
                    showCreatorDashboard(creatorId);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials!");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
            }
        }
    }

    private static void registerJobSeeker() {
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JPasswordField password = new JPasswordField();
        JTextField resume = new JTextField();

        Object[] fields = {"Name:", name, "Email:", email, "Password:", password, "Resume Summary:", resume};

        int result = JOptionPane.showConfirmDialog(null, fields, "Register as Job Seeker", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Check if email already exists
                String checkQuery = "SELECT id FROM job_seekers WHERE email=?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, email.getText());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(null, "Email already registered!");
                    return;
                }

                String query = "INSERT INTO job_seekers (name, email, password, resume) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, name.getText());
                stmt.setString(2, email.getText());
                stmt.setString(3, new String(password.getPassword()));
                stmt.setString(4, resume.getText());
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(null, "Registration successful!");
                // Removed the viewJobs() call after registration
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
            }
        }
    }

    private static void registerJobCreator() {
        JTextField company = new JTextField();
        JTextField email = new JTextField();
        JPasswordField password = new JPasswordField();

        Object[] fields = {"Company Name:", company, "Email:", email, "Password:", password};

        int result = JOptionPane.showConfirmDialog(null, fields, "Register as Job Creator", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Check if email already exists
                String checkQuery = "SELECT id FROM job_creators WHERE email=?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, email.getText());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(null, "Email already registered!");
                    return;
                }

                String query = "INSERT INTO job_creators (company_name, email, password) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, company.getText());
                stmt.setString(2, email.getText());
                stmt.setString(3, new String(password.getPassword()));
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(null, "Registration successful!");
                // Removed the showCreatorDashboard() call after registration
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
            }
        }
    }

    private static void viewJobs(int seekerId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT j.* FROM jobs j LEFT JOIN applied_jobs a ON j.id = a.job_id AND a.seeker_id = ? WHERE a.job_id IS NULL";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, seekerId);
            ResultSet rs = stmt.executeQuery();

            JFrame jobFrame = new JFrame("Available Jobs");
            jobFrame.setSize(750, 550);
            jobFrame.setLocationRelativeTo(null);
            jobFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel jobPanel = new JPanel();
            jobPanel.setLayout(new GridLayout(0, 2, 20, 20));
            jobPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            jobPanel.setBackground(new Color(245, 245, 245)); // light background

            boolean jobsAvailable = false;

            while (rs.next()) {
                jobsAvailable = true;
                int jobId = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String location = rs.getString("location");
                double salary = rs.getDouble("salary");
                String company = rs.getString("company_name");

                JPanel card = new JPanel();
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                card.setBackground(Color.WHITE);
                card.setPreferredSize(new Dimension(320, 180));

                card.add(new JLabel(" Title: " + title));
                card.add(new JLabel(" Company: " + company));
                card.add(new JLabel(" Location: " + location));
                card.add(new JLabel(" Salary: Rs " + salary));
                card.add(new JLabel("<html><body style='width:280px;'> Description: " + description + "</body></html>"));

                JButton applyBtn = new JButton("Apply");
                applyBtn.setBackground(new Color(0, 123, 255));
                applyBtn.setForeground(Color.WHITE);
                applyBtn.setFocusPainted(false);

                applyBtn.addActionListener(e -> applyForJob(seekerId, jobId));
                card.add(Box.createRigidArea(new Dimension(0, 10)));
                card.add(applyBtn);

                jobPanel.add(card);
            }

            if (!jobsAvailable) {
                JOptionPane.showMessageDialog(null, "No available jobs at the moment!");
                return;
            }

            JScrollPane scrollPane = new JScrollPane(jobPanel);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            jobFrame.add(scrollPane);
            jobFrame.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
    }

    // Apply method inside same class
    private static void applyForJob(int seekerId, int jobId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String applyQuery = "INSERT INTO applied_jobs (seeker_id, job_id) VALUES (?, ?)";
            PreparedStatement applyStmt = conn.prepareStatement(applyQuery);
            applyStmt.setInt(1, seekerId);
            applyStmt.setInt(2, jobId);
            applyStmt.executeUpdate();
            JOptionPane.showMessageDialog(null, " Successfully applied for the job!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, " Error applying for job: " + e.getMessage());
        }
    }


    private static void showCreatorDashboard(int creatorId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String[] options = {"Post New Job", "View Posted Jobs", "View Applicants"};
            int choice = JOptionPane.showOptionDialog(null, "What would you like to do?", "Creator Dashboard",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                postNewJob(creatorId);
            } else if (choice == 1) {
                viewPostedJobs(creatorId);
            } else if (choice == 2) {
                viewApplicants(creatorId);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
    }

    private static void postNewJob(int creatorId) {
        JTextField title = new JTextField();
        JTextField description = new JTextField();
        JTextField location = new JTextField();
        JTextField salary = new JTextField();

        Object[] fields = {
                "Job Title:", title,
                "Description:", description,
                "Location:", location,
                "Salary (Rs):", salary
        };

        int result = JOptionPane.showConfirmDialog(null, fields, "Post New Job", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Get company name for the job posting
                String companyQuery = "SELECT company_name FROM job_creators WHERE id=?";
                PreparedStatement companyStmt = conn.prepareStatement(companyQuery);
                companyStmt.setInt(1, creatorId);
                ResultSet rs = companyStmt.executeQuery();

                if (rs.next()) {
                    String companyName = rs.getString("company_name");

                    String query = "INSERT INTO jobs (title, description, location, salary, creator_id, company_name) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, title.getText());
                    stmt.setString(2, description.getText());
                    stmt.setString(3, location.getText());
                    stmt.setDouble(4, Double.parseDouble(salary.getText()));
                    stmt.setInt(5, creatorId);
                    stmt.setString(6, companyName);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Job posted successfully!");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid salary amount!");
            }
        }
    }

    private static void viewPostedJobs(int creatorId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM jobs WHERE creator_id=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, creatorId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder jobList = new StringBuilder("Your Posted Jobs:\n\n");
            while (rs.next()) {
                jobList.append("ID: ").append(rs.getInt("id"))
                        .append(" | Title: ").append(rs.getString("title"))
                        .append("\nDescription: ").append(rs.getString("description"))
                        .append("\nLocation: ").append(rs.getString("location"))
                        .append("\nSalary: Rs ").append(rs.getDouble("salary"))
                        .append("\n---------------------------\n");
            }

            if (jobList.toString().equals("Your Posted Jobs:\n\n")) {
                JOptionPane.showMessageDialog(null, "You haven't posted any jobs yet!");
                return;
            }

            JTextArea textArea = new JTextArea(jobList.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(null, scrollPane, "Your Jobs", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
    }

    private static void viewApplicants(int creatorId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT j.title, s.name, s.email, s.resume " +
                    "FROM applied_jobs a " +
                    "JOIN jobs j ON a.job_id = j.id " +
                    "JOIN job_seekers s ON a.seeker_id = s.id " +
                    "WHERE j.creator_id=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, creatorId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder applicantsList = new StringBuilder("Job Applicants:\n\n");
            while (rs.next()) {
                applicantsList.append("Job: ").append(rs.getString("title"))
                        .append("\nApplicant: ").append(rs.getString("name"))
                        .append("\nEmail: ").append(rs.getString("email"))
                        .append("\nResume: ").append(rs.getString("resume"))
                        .append("\n---------------------------\n");
            }

            if (applicantsList.toString().equals("Job Applicants:\n\n")) {
                JOptionPane.showMessageDialog(null, "No applicants yet!");
                return;
            }

            JTextArea textArea = new JTextArea(applicantsList.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(null, scrollPane, "Applicants", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
    }
}