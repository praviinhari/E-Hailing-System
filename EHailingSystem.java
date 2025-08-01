import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class EHailingSystem {
    public static void main(String[] args) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "UCanAccess Driver not found.");
            return;
        }
        SwingUtilities.invokeLater(LoginWindow::new);
    }
}

// ======================== LOGIN =========================
class LoginWindow extends JFrame {
    public LoginWindow() {
        setTitle("Login");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 1));

        JPanel topPanel = new JPanel();
        JTextField tfUserID = new JTextField(10);
        JPasswordField pfPassword = new JPasswordField(10);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Customer", "Driver", "Admin"});

        topPanel.add(new JLabel("User ID:"));
        topPanel.add(tfUserID);
        topPanel.add(new JLabel("Password:"));
        topPanel.add(pfPassword);
        topPanel.add(new JLabel("Role:"));
        topPanel.add(roleBox);

        JPanel btnPanel = new JPanel();
        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Sign Up");
        btnPanel.add(loginBtn);
        btnPanel.add(signupBtn);

        loginBtn.addActionListener(e -> {
            String id = tfUserID.getText().trim();
            String pass = new String(pfPassword.getPassword());
            String role = (String) roleBox.getSelectedItem();

            try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://C:/Users/User/OneDrive/Desktop/Documents/e_hailing.accdb")) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE userID=? AND password=? AND role=?");
                ps.setString(1, id);
                ps.setString(2, pass);
                ps.setString(3, role);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    dispose();
                    switch (role) {
                        case "Customer": new MainCustomerGUI(id); break;
                        case "Driver": new DriverVehicleWindow(id); break;
                        case "Admin": new AdminDashboard(); break;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        signupBtn.addActionListener(e -> {
            dispose();
            new SignupWindow();
        });

        add(topPanel);
        add(new JLabel()); // spacer
        add(btnPanel);
        setVisible(true);
    }
}

// ===================== SIGN UP =======================
class SignupWindow extends JFrame {
    public SignupWindow() {
        setTitle("Sign Up");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2));

        JTextField tfID = new JTextField();
        JPasswordField tfPass = new JPasswordField();
        JTextField tfName = new JTextField();
        JTextField tfPhone = new JTextField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Customer", "Driver", "Admin"});

        JButton btn = new JButton("Register");

        add(new JLabel("User ID:")); add(tfID);
        add(new JLabel("Password:")); add(tfPass);
        add(new JLabel("Name:")); add(tfName);
        add(new JLabel("Phone:")); add(tfPhone);
        add(new JLabel("Role:")); add(roleBox);
        add(new JLabel()); add(btn);

        btn.addActionListener(e -> {
            String id = tfID.getText().trim();
            String pass = new String(tfPass.getPassword());
            String name = tfName.getText().trim();
            String phone = tfPhone.getText().trim();
            String role = (String) roleBox.getSelectedItem();

            try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://C:/Users/User/OneDrive/Desktop/Documents/e_hailing.accdb")) {
                PreparedStatement check = conn.prepareStatement("SELECT * FROM Users WHERE userID=?");
                check.setString(1, id);
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "User ID exists.");
                    return;
                }

                PreparedStatement insert = conn.prepareStatement("INSERT INTO Users (userID, password, name, phone, role) VALUES (?, ?, ?, ?, ?)");
                insert.setString(1, id);
                insert.setString(2, pass);
                insert.setString(3, name);
                insert.setString(4, phone);
                insert.setString(5, role);
                insert.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registered!");
                dispose();
                new LoginWindow();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        setVisible(true);
    }
}

// ===================== CUSTOMER GUI =======================
class MainCustomerGUI extends JFrame {
    private String userID;
    private JComboBox<String> pickupBox, dropoffBox;
    private JList<String> driverList;
    private double currentFare = 0;
    private String selectedDriver = "";
    private static final String[] LOCS = {"KL Sentral", "Mid Valley", "Pavilion", "Sunway", "Bukit Bintang"};

    public MainCustomerGUI(String userID) {
        this.userID = userID;

        setTitle("E-Hailing Booking");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem history = new JMenuItem("Booking History");
        JMenuItem logout = new JMenuItem("Logout");
        file.add(history); file.add(logout);
        bar.add(file);
        setJMenuBar(bar);

        JPanel input = new JPanel(new GridLayout(2, 2));
        pickupBox = new JComboBox<>(LOCS);
        dropoffBox = new JComboBox<>(LOCS);
        input.add(new JLabel("Pickup:")); input.add(pickupBox);
        input.add(new JLabel("Drop-off:")); input.add(dropoffBox);

        add(input, BorderLayout.NORTH);

        driverList = new JList<>();
        loadDrivers();
        add(new JScrollPane(driverList), BorderLayout.CENTER);

        JButton bookBtn = new JButton("Book Now");
        bookBtn.addActionListener(e -> bookRide());
        add(bookBtn, BorderLayout.SOUTH);

        history.addActionListener(e -> new BookingHistoryWindow(userID));
        logout.addActionListener(e -> {
            dispose();
            new LoginWindow();
        });

        setVisible(true);
    }

    private void loadDrivers() {
        DefaultListModel<String> model = new DefaultListModel<>();
        try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://C:/Users/User/OneDrive/Desktop/Documents/e_hailing.accdb")) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Drivers");
            while (rs.next()) {
                model.addElement(rs.getString("name") + " - " + rs.getString("carModel") + " (" + rs.getString("plate") + ")");
            }
            driverList.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void bookRide() {
        String pickup = (String) pickupBox.getSelectedItem();
        String dropoff = (String) dropoffBox.getSelectedItem();
        selectedDriver = driverList.getSelectedValue();

        if (pickup.equals(dropoff) || selectedDriver == null) {
            JOptionPane.showMessageDialog(this, "Invalid input or no driver selected.");
            return;
        }

        currentFare = 5.0 + (Math.abs(pickup.hashCode() - dropoff.hashCode()) % 20 + 1) * 0.5;
        try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://C:/Users/User/OneDrive/Desktop/Documents/e_hailing.accdb")) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Bookings (userID, pickup, dropoff, driver, fare) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, userID);
            ps.setString(2, pickup);
            ps.setString(3, dropoff);
            ps.setString(4, selectedDriver);
            ps.setDouble(5, currentFare);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        new PaymentWindow(userID, currentFare);
    }
}

// ==================== PAYMENT WINDOW ====================
class PaymentWindow extends JFrame {
    public PaymentWindow(String userID, double fare) {
        setTitle("Payment");
        setSize(400, 300);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        JLabel title = new JLabel("Fare: RM" + String.format("%.2f", fare) + " — Select Payment Method", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        JPanel methodPanel = new JPanel(new FlowLayout());
        JButton cardBtn = new JButton("Card");
        JButton walletBtn = new JButton("E-Wallet");
        methodPanel.add(cardBtn);
        methodPanel.add(walletBtn);
        add(methodPanel, BorderLayout.CENTER);

        // Declare fields here so they are accessible in event handlers
        JTextField cardNum = new JTextField();
        JTextField cardHolder = new JTextField();
        JTextField bank = new JTextField();
        JButton payCard = new JButton("Pay Now");

        JPanel cardPanel = new JPanel(new GridLayout(4, 2));
        cardPanel.add(new JLabel("Card Number:")); cardPanel.add(cardNum);
        cardPanel.add(new JLabel("Card Holder:")); cardPanel.add(cardHolder);
        cardPanel.add(new JLabel("Bank:")); cardPanel.add(bank);
        cardPanel.add(new JLabel()); cardPanel.add(payCard);

        JTextField walletID = new JTextField();
        JTextField provider = new JTextField();
        JButton payWallet = new JButton("Pay Now");

        JPanel walletPanel = new JPanel(new GridLayout(3, 2));
        walletPanel.add(new JLabel("Wallet ID:")); walletPanel.add(walletID);
        walletPanel.add(new JLabel("Provider:")); walletPanel.add(provider);
        walletPanel.add(new JLabel()); walletPanel.add(payWallet);

        JPanel container = new JPanel(new CardLayout());
        container.add(cardPanel, "Card");
        container.add(walletPanel, "Wallet");
        add(container, BorderLayout.SOUTH);

        cardBtn.addActionListener(e -> ((CardLayout) container.getLayout()).show(container, "Card"));
        walletBtn.addActionListener(e -> ((CardLayout) container.getLayout()).show(container, "Wallet"));

        payCard.addActionListener(e -> {
            String method = "Card";
            savePayment(userID, method, fare, cardNum.getText(), cardHolder.getText(), bank.getText(), null, null);
            saveReceipt(userID, method, fare);
            showReceiptPopup(userID, method, fare);
            dispose();
            new FeedbackWindow(userID);
        });

        payWallet.addActionListener(e -> {
            String method = "E-Wallet";
            savePayment(userID, method, fare, null, null, null, walletID.getText(), provider.getText());
            saveReceipt(userID, method, fare);
            showReceiptPopup(userID, method, fare);
            dispose();
            new FeedbackWindow(userID);
        });

        setVisible(true);
    }

    private void savePayment(String userID, String method, double fare, String cardNum, String cardHolder, String bank, String walletID, String provider) {
        try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://C:/Users/User/OneDrive/Desktop/Documents/e_hailing.accdb")) {
            String query = "INSERT INTO Payments (userID, paymentMethod, amount, cardNumber, cardHolder, bankName, walletID, provider) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2, method);
            ps.setDouble(3, fare);
            ps.setString(4, cardNum);
            ps.setString(5, cardHolder);
            ps.setString(6, bank);
            ps.setString(7, walletID);
            ps.setString(8, provider);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Payment DB error: " + e.getMessage());
        }
    }

    private void saveReceipt(String user, String method, double fare) {
        try (FileOutputStream fos = new FileOutputStream("receipt.txt", true)) {
            String data = "User: " + user + ", Paid RM" + fare + " via " + method + "\n";
            fos.write(data.getBytes());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Receipt file error: " + e.getMessage());
        }
    }

    private void showReceiptPopup(String userID, String method, double fare) {
        String receipt = "User: " + userID + "\nPaid RM" + fare + " via " + method + "\nThank you!";
        JTextArea area = new JTextArea(receipt);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Receipt", JOptionPane.INFORMATION_MESSAGE);
    }
}


class FeedbackWindow extends JFrame {
    public FeedbackWindow(String userID) {
        setTitle("Booking Feedback");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(4, 1));
        JTextArea reviewArea = new JTextArea("Write your review here...");
        JTextArea supportArea = new JTextArea("Need help? Leave your support message...");

        JButton submitBtn = new JButton("Submit");

        inputPanel.add(new JLabel("Review:"));
        inputPanel.add(new JScrollPane(reviewArea));
        inputPanel.add(new JLabel("Support Message:"));
        inputPanel.add(new JScrollPane(supportArea));

submitBtn.addActionListener(e -> {
    String reviewText = reviewArea.getText().trim();
    String supportText = supportArea.getText().trim();

    try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://C:/Users/User/OneDrive/Desktop/Documents/e_hailing.accdb")) {
        if (!reviewText.isEmpty()) {
            String q1 = "INSERT INTO Reviews (userID, reviewText) VALUES (?, ?)";
            PreparedStatement ps1 = conn.prepareStatement(q1);
            ps1.setString(1, userID);
            ps1.setString(2, reviewText);
            ps1.executeUpdate();
        }

        if (!supportText.isEmpty()) {
            String q2 = "INSERT INTO SupportMessages (userID, message) VALUES (?, ?)";
            PreparedStatement ps2 = conn.prepareStatement(q2);
            ps2.setString(1, userID);
            ps2.setString(2, supportText);
            ps2.executeUpdate();

            // Admin auto-response popup
            JOptionPane.showMessageDialog(this, "Thank you for the support. We will look into the matter quickly.");
        }

        JOptionPane.showMessageDialog(this, "Feedback submitted!");
        dispose();
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
    }
});


        add(inputPanel, BorderLayout.CENTER);
        add(submitBtn, BorderLayout.SOUTH);
        setVisible(true);
    }
}



// ==================== BOOKING HISTORY ====================
class BookingHistoryWindow extends JFrame {
    public BookingHistoryWindow(String userID) {
        setTitle("Booking History");
        setSize(400, 300);
        setLocationRelativeTo(null);

        JTextArea area = new JTextArea();
        area.setEditable(false);

        try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://C:/Users/User/OneDrive/Desktop/Documents/e_hailing.accdb")) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Bookings WHERE userID=?");
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                area.append("From " + rs.getString("pickup") + " to " + rs.getString("dropoff") + "\nDriver: " +
                        rs.getString("driver") + ", Fare: RM" + rs.getDouble("fare") + "\n\n");
            }
        } catch (SQLException e) {
            area.setText("Error loading history: " + e.getMessage());
        }

        add(new JScrollPane(area));
        setVisible(true);
    }
}

// ==================== DRIVER WINDOW ====================
class DriverVehicleWindow extends JFrame {
    public DriverVehicleWindow(String userID) {
        setTitle("Register Vehicle");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2));

        JTextField name = new JTextField();
        JTextField carModel = new JTextField();
        JTextField plate = new JTextField();
        JTextField color = new JTextField();

        JButton submit = new JButton("Submit");

        add(new JLabel("Driver Name:")); add(name);
        add(new JLabel("Car Model:")); add(carModel);
        add(new JLabel("Plate No:")); add(plate);
        add(new JLabel("Color:")); add(color);
        add(new JLabel()); add(submit);

        submit.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://C:/Users/User/OneDrive/Desktop/Documents/e_hailing.accdb")) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO Drivers (userID, name, carModel, plate, color) VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, userID);
                ps.setString(2, name.getText());
                ps.setString(3, carModel.getText());
                ps.setString(4, plate.getText());
                ps.setString(5, color.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Vehicle Registered.");
                dispose();
                new LoginWindow();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}

// ==================== ADMIN DASHBOARD ====================
class AdminDashboard extends JFrame {
    public AdminDashboard() {
        setTitle("Admin Panel");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));

        JTextArea reviewArea = new JTextArea("Reviews:\n");
        JTextArea supportArea = new JTextArea("Support Messages:\n");

        try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://C:/Users/User/OneDrive/Desktop/Documents/e_hailing.accdb")) {
            ResultSet rs1 = conn.createStatement().executeQuery("SELECT * FROM Reviews");
            while (rs1.next()) {
                reviewArea.append(rs1.getString("userID") + ": " + rs1.getString("reviewText") + "\n\n");
            }

            ResultSet rs2 = conn.createStatement().executeQuery("SELECT * FROM SupportMessages");
            while (rs2.next()) {
                supportArea.append(rs2.getString("userID") + ": " + rs2.getString("message") + "\n\n");
            }

        } catch (SQLException e) {
            reviewArea.setText("Error: " + e.getMessage());
        }

        add(new JScrollPane(reviewArea));
        add(new JScrollPane(supportArea));
        setVisible(true);
    }
}

