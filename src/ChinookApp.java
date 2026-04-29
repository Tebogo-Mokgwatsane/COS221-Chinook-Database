/** @author Tebogo Mokgwatsane */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ChinookApp extends JFrame {
        
    // ====================== DATABASE CONNECTION (Task 5) ======================
    //Environment variables
    private Connection getConnection() throws SQLException {
        String protocol = System.getProperty("CHINOOK_DB_PROTO");      //jdbc:mariadb
        String host  = System.getProperty("CHINOOK_DB_HOST");       // localhost/"172.21.226.121"
        String port  = System.getProperty("CHINOOK_DB_PORT");       // 3306
        String dbName    = System.getProperty("CHINOOK_DB_NAME");       // u25042239_chinook
        String username  = System.getProperty("CHINOOK_DB_USERNAME");   //chinook_user
        String password  = System.getProperty("CHINOOK_DB_PASSWORD");   //ChinookPass2026
        
        if (protocol == null) protocol = "jdbc:mariadb";
        if (host == null) host = "localhost"; //"172.21.226.121"
        if (port == null) port = "3306";
        if (dbName == null) dbName = "u25042239_chinook";
        if (username == null) username = "chinook_user";
        if (password == null) password = "ChinookPass2026";

        String url = protocol + "://" + host + ":" + (port != null ? port : "3306") + "/" + dbName;
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to Chinook database.");
            return conn;
        } catch (SQLException ex) {
            System.out.println("FAILED: " + ex.getMessage());
            throw ex;
        }
        
        //return DriverManager.getConnection(url, username, password);    //JDBC Driver
    }

    private JTabbedPane tabbedPane;

    public ChinookApp() {
        setTitle("Chinook Music Store - COS 221 Practical 4");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        add(tabbedPane);


        // Create all tabs/tables
        createEmployeesTab();


        setVisible(true);
    }

    // ====================== 4.1 + 4.2 EMPLOYEES TAB ======================
    private void createEmployeesTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel lblFilter = new JLabel("Filter by Name or City:");
        JTextField txtFilter = new JTextField(20);
        JButton btnFilter = new JButton("Apply Filter");

        JPanel top = new JPanel();
        top.add(lblFilter);
        top.add(txtFilter);
        top.add(btnFilter);

        String[] columns = {"First Name", "Last Name", "Title", "City", "Country", "Phone", "Supervisor"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        loadEmployees(model, "");

        // Real-time button filter
        ActionListener filterAction = e -> loadEmployees(model, txtFilter.getText().trim());
        btnFilter.addActionListener(filterAction);
        txtFilter.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) filterAction.actionPerformed(null);
            }
        });

        tabbedPane.addTab("Employees", panel);
    }

    private void loadEmployees(DefaultTableModel model, String filter) {
        model.setRowCount(0);
        String sql = """
            SELECT e1.FirstName, e1.LastName, e1.Title, e1.City, e1.Country, e1.Phone,
                   CONCAT(e2.FirstName, ' ', e2.LastName) AS Supervisor
            FROM Employee e1
            LEFT JOIN Employee e2 ON e1.ReportsTo = e2.EmployeeId
            WHERE CONCAT(e1.FirstName, ' ', e1.LastName, e1.City) LIKE ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + filter + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("FirstName"));
                row.add(rs.getString("LastName"));
                row.add(rs.getString("Title"));
                row.add(rs.getString("City"));
                row.add(rs.getString("Country"));
                row.add(rs.getString("Phone"));
                row.add(rs.getString("Supervisor"));
                model.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading employees: " + ex.getMessage());
        }
    }

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
    }
}
