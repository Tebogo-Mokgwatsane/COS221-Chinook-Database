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
        createTracksTab();
        createReportTab();

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
    // ====================== 4.3 TRACKS TAB - ADD NEW TRACK ======================
    private void createTracksTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton btnAdd = new JButton("Add New Track");
        String[] cols = {"TrackId", "Name", "Album", "Genre", "MediaType", "Composer", "Price"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        panel.add(btnAdd, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        loadTracks(model);

        btnAdd.addActionListener(e -> showAddTrackDialog(model));

        tabbedPane.addTab("Tracks", panel);
    }

    private void loadTracks(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = """
            SELECT t.TrackId, t.Name, a.Title AS Album, g.Name AS Genre,
                   m.Name AS MediaType, t.Composer, t.UnitPrice
            FROM Track t
            LEFT JOIN Album a ON t.AlbumId = a.AlbumId
            LEFT JOIN Genre g ON t.GenreId = g.GenreId
            LEFT JOIN MediaType m ON t.MediaTypeId = m.MediaTypeId
            LIMIT 200
            """;

        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("TrackId"));
                row.add(rs.getString("Name"));
                row.add(rs.getString("Album"));
                row.add(rs.getString("Genre"));
                row.add(rs.getString("MediaType"));
                row.add(rs.getString("Composer"));
                row.add(rs.getDouble("UnitPrice"));
                model.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ====================== ADD NEW TRACK ======================
    private void showAddTrackDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Add New Track", true);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));
        dialog.setSize(500, 400);

        JTextField txtName = new JTextField();
        JComboBox<String> cbAlbum = new JComboBox<>();
        JComboBox<String> cbGenre = new JComboBox<>();
        JComboBox<String> cbMedia = new JComboBox<>();
        JTextField txtComposer = new JTextField();
        JTextField txtMs = new JTextField("300000");
        JTextField txtPrice = new JTextField("0.99");

        // Populate dropdowns
        populateCombo(cbAlbum, "SELECT Title FROM Album ORDER BY Title", "Title");
        populateCombo(cbGenre, "SELECT Name FROM Genre ORDER BY Name", "Name");
        populateCombo(cbMedia, "SELECT Name FROM MediaType ORDER BY Name", "Name");

        dialog.add(new JLabel("Track Name:")); dialog.add(txtName);
        dialog.add(new JLabel("Album:"));      dialog.add(cbAlbum);
        dialog.add(new JLabel("Genre:"));      dialog.add(cbGenre);
        dialog.add(new JLabel("Media Type:")); dialog.add(cbMedia);
        dialog.add(new JLabel("Composer:"));   dialog.add(txtComposer);
        dialog.add(new JLabel("Milliseconds:")); dialog.add(txtMs);
        dialog.add(new JLabel("Unit Price:")); dialog.add(txtPrice);

        JButton btnSave = new JButton("Save Track");
        dialog.add(btnSave);

        btnSave.addActionListener(e -> {
            try (Connection conn = getConnection()) {
                String sql ="""
                            INSERT INTO Track (Name, AlbumId, MediaTypeId, GenreId, Composer, Milliseconds, UnitPrice) 
                            VALUES (?, (SELECT AlbumId FROM Album WHERE Title=? LIMIT 1), 
                            (SELECT MediaTypeId FROM MediaType WHERE Name=? LIMIT 1),
                            (SELECT GenreId FROM Genre WHERE Name=? LIMIT 1), ?, ?, ?)
                            """;

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtName.getText());
                ps.setString(2, (String) cbAlbum.getSelectedItem());
                ps.setString(3, (String) cbMedia.getSelectedItem());
                ps.setString(4, (String) cbGenre.getSelectedItem());
                ps.setString(5, txtComposer.getText());
                ps.setInt(6, Integer.parseInt(txtMs.getText()));
                ps.setDouble(7, Double.parseDouble(txtPrice.getText()));

                ps.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Track added successfully!");
                dialog.dispose();
                loadTracks(model);   // refresh table
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void populateCombo(JComboBox<String> cb, String sql, String column) {
        cb.removeAllItems();
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                cb.addItem(rs.getString(column));
            }
        } catch (Exception ignored) {}
    }
    
    // ====================== 4.4 REPORT TAB - GENRE REVENUE ======================
    private void createReportTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton btnRefresh = new JButton("Refresh Revenue Report");
        String[] cols = {"Genre", "Total Revenue"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        panel.add(btnRefresh, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadGenreRevenue(model));

        // Auto load when tab selected
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == panel) loadGenreRevenue(model);
        });

        tabbedPane.addTab("Genre Revenue Report", panel);
    }

    private void loadGenreRevenue(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = """
            SELECT g.Name AS Genre, ROUND(SUM(il.Quantity * il.UnitPrice), 2) AS TotalRevenue
            FROM Genre g
            JOIN Track t ON g.GenreId = t.GenreId
            JOIN InvoiceLine il ON t.TrackId = il.TrackId
            GROUP BY g.GenreId, g.Name
            ORDER BY TotalRevenue DESC
            """;

        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("Genre"), rs.getDouble("TotalRevenue")});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
