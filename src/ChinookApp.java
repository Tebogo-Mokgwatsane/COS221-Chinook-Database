/** @author Tebogo Mokgwatsane */

import javax.swing.*;
import javax.swing.border.*;
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

    public Color primaryBlue = new Color(8, 101, 141);      // MySQL like blue
    public Color accentOrange = new Color(255, 140, 0);     // MySQL like orange
    public Color lightBg = new Color(245, 245, 245);    // Light grey background
    public Color white = Color.WHITE;

    public ChinookApp() {

        setTitle("Chinook Music Store - COS 221 Practical 4");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);

        // ====================== COLOR/STYLE THEME ======================

        //Making active tabs more visible
        UIManager.put("TabbedPane.selectedForeground", Color.WHITE);        
        UIManager.put("TabbedPane.selectedBackground", primaryBlue);
        UIManager.put("Button.background", primaryBlue);
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Button.focusPainted", false);

        // Selected tab is styled
        UIManager.put("TabbedPane.selected", primaryBlue);
        UIManager.put("TabbedPane.contentAreaColor", primaryBlue);

        // Initialising tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(true);
        tabbedPane.setBackground(accentOrange);
        tabbedPane.setForeground(Color.BLACK);

        getContentPane().setBackground(primaryBlue);
        // ====================== COLOR/STYLE THEME ======================

        add(tabbedPane);

        // Create all tabs/tables
        createEmployeesTab();
        createTracksTab();
        createReportTab();
        createNotificationsTab();
        createRecommendationsTab();

        setVisible(true);
    }

    // ====================== 4.1 + 4.2 EMPLOYEES TAB ======================
    private void createEmployeesTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(accentOrange);

        JLabel lblFilter = new JLabel("Filter by Name or City:");
        JTextField txtFilter = new JTextField(20);
        JButton btnFilter = new JButton("  Apply Filter  ");
        btnFilter.setBorder(new LineBorder(accentOrange));

        JPanel top = new JPanel();
        top.setBackground(primaryBlue);
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
        panel.setBackground(accentOrange);

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
        panel.setBackground(accentOrange);

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

    // ====================== 4.5 (CRUD) ======================
    private void createNotificationsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane subTabs = new JTabbedPane();

        // Sub-tab 1: Customer CRUD
        JPanel crudPanel = createCustomerCRUDPanel();
        // Sub-tab 2: Inactive Customers
        JPanel inactivePanel = createInactiveCustomersPanel();

        subTabs.addTab("Customer CRUD", crudPanel);
        subTabs.addTab("Inactive Customers", inactivePanel);

        panel.add(subTabs, BorderLayout.CENTER);
        tabbedPane.addTab("Notifications", panel);
    }

    private JPanel createCustomerCRUDPanel() {
        JPanel p = new JPanel(new BorderLayout());
        String[] cols = {"CustomerId", "FirstName", "LastName", "Email", "Phone", "Country"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        JPanel btnPanel = new JPanel();
        JButton btnLoad = new JButton("Load Customers");
        JButton btnAdd = new JButton("Add Customer");
        JButton btnDelete = new JButton("Delete Selected");

        btnPanel.add(btnLoad);
        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);

        p.add(btnPanel, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);

        btnLoad.addActionListener(e -> loadCustomers(model));

        btnAdd.addActionListener(e -> {
            // Dialog for create
            String fname = JOptionPane.showInputDialog("First Name:");
            String lname = JOptionPane.showInputDialog("Last Name:");
            String email = JOptionPane.showInputDialog("Email:");
            String phone = JOptionPane.showInputDialog("Phone:");
            String country = JOptionPane.showInputDialog("Country:");

            if (fname != null && email != null) {
                String sql = "INSERT INTO Customer (FirstName, LastName, Email, Phone, Country) VALUES (?,?,?,?,?)";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, fname);
                    ps.setString(2, lname);
                    ps.setString(3, email);
                    ps.setString(4, phone);
                    ps.setString(5, country);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Customer added!");
                    loadCustomers(model);
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            int id = (Integer) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete customer " + id + "?") == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM Customer WHERE CustomerId=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    loadCustomers(model);
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
            }
        });

        return p;
    }

    private void loadCustomers(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT CustomerId, FirstName, LastName, Email, Phone, Country FROM Customer";
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("CustomerId"), rs.getString("FirstName"), rs.getString("LastName"),
                    rs.getString("Email"), rs.getString("Phone"), rs.getString("Country")
                });
            }
        } catch (Exception ignored) {}
    }

    // ====================== 4.6 NOTIFICATIONS TAB (Inactive) ======================
    private JPanel createInactiveCustomersPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(accentOrange);

        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search");

        String[] cols = {"CustomerId", "FirstName", "LastName", "Email", "Last Invoice"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        JPanel top = new JPanel();
        top.add(new JLabel("Search:")); top.add(txtSearch); top.add(btnSearch);

        p.add(top, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);

        Runnable loadInactive = () -> {
            model.setRowCount(0);
            String sql = """
                SELECT c.CustomerId, c.FirstName, c.LastName, c.Email,
                       MAX(i.InvoiceDate) AS LastInvoice
                FROM Customer c
                LEFT JOIN Invoice i ON c.CustomerId = i.CustomerId
                GROUP BY c.CustomerId, c.FirstName, c.LastName, c.Email
                HAVING MAX(i.InvoiceDate) IS NULL 
                    OR MAX(i.InvoiceDate) < DATE_SUB(CURDATE(), INTERVAL 2 YEAR)
                """;

            try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("CustomerId"), rs.getString("FirstName"), rs.getString("LastName"),
                        rs.getString("Email"), rs.getString("LastInvoice")
                    });
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        };

        loadInactive.run();
        btnSearch.addActionListener(e -> loadInactive.run());

        return p;
    }

    // ====================== 4.7 CUSTOMER RECOMMENDATIONS TAB ======================
    private void createRecommendationsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(accentOrange);

        JPanel top = new JPanel();
        top.add(new JLabel("Select Customer:"));
        JComboBox<String> cbCustomer = new JComboBox<>();
        top.add(cbCustomer);

        panel.add(top, BorderLayout.NORTH);

        JTextArea summaryArea = new JTextArea(6, 50);
        summaryArea.setEditable(false);
        panel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);

        String[] recCols = {"Track Name", "Album", "Genre", "UnitPrice"};
        DefaultTableModel recModel = new DefaultTableModel(recCols, 0);
        JTable recTable = new JTable(recModel);
        panel.add(new JScrollPane(recTable), BorderLayout.SOUTH);

        // Populate customer dropdown
        populateCustomerCombo(cbCustomer);

        cbCustomer.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) cbCustomer.getSelectedItem();
                if (selected != null) {
                    int custId = Integer.parseInt(selected.split(" - ")[0]);
                    updateCustomerInsights(custId, summaryArea, recModel);
                }
            }
        });

        tabbedPane.addTab("Customer Recommendations", panel);
    }

    private void populateCustomerCombo(JComboBox<String> cb) {
        cb.removeAllItems();
        String sql = "SELECT CustomerId, FirstName, LastName FROM Customer ORDER BY LastName";
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                cb.addItem(rs.getInt("CustomerId") + " - " + rs.getString("FirstName") + " " + rs.getString("LastName"));
            }
        } catch (Exception ignored) {}
    }

    private void updateCustomerInsights(int customerId, JTextArea summary, DefaultTableModel recModel) {
        summary.setText("");

        try (Connection conn = getConnection()) {
            // Spending Summary
            String sumSql = """
                SELECT ROUND(SUM(Total),2) AS TotalSpent, COUNT(*) AS Purchases, MAX(InvoiceDate) AS LastPurchase
                FROM Invoice WHERE CustomerId = ?
                """;
            PreparedStatement ps = conn.prepareStatement(sumSql);
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                summary.append("Total Spent: $" + rs.getDouble("TotalSpent") + "\n");
                summary.append("Purchases: " + rs.getInt("Purchases") + "\n");
                summary.append("Last Purchase: " + rs.getDate("LastPurchase") + "\n\n");
            }

            // Favourite Genre
            String favSql = """
                SELECT g.Name, COUNT(*) AS cnt
                FROM Invoice i
                JOIN InvoiceLine il ON i.InvoiceId = il.InvoiceId
                JOIN Track t ON il.TrackId = t.TrackId
                JOIN Genre g ON t.GenreId = g.GenreId
                WHERE i.CustomerId = ?
                GROUP BY g.GenreId, g.Name
                ORDER BY cnt DESC LIMIT 1
                """;
            ps = conn.prepareStatement(favSql);
            ps.setInt(1, customerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                summary.append("Favourite Genre: " + rs.getString("Name") + "\n\n");
            }

            // Tracks favourite genre not yet purchased by customer
            String recSql = """
                SELECT t.Name, a.Title AS Album, g.Name AS Genre, t.UnitPrice
                FROM Track t
                JOIN Album a ON t.AlbumId = a.AlbumId
                JOIN Genre g ON t.GenreId = g.GenreId
                WHERE g.GenreId = (SELECT GenreId FROM (
                    SELECT t2.GenreId, COUNT(*) cnt FROM InvoiceLine il2
                    JOIN Track t2 ON il2.TrackId = t2.TrackId
                    WHERE il2.InvoiceId IN (SELECT InvoiceId FROM Invoice WHERE CustomerId=?)
                    GROUP BY t2.GenreId ORDER BY cnt DESC LIMIT 1
                ) fav)
                AND t.TrackId NOT IN (
                    SELECT il3.TrackId FROM InvoiceLine il3
                    JOIN Invoice i3 ON il3.InvoiceId = i3.InvoiceId
                    WHERE i3.CustomerId = ?
                )
                LIMIT 20
                """;

            ps = conn.prepareStatement(recSql);
            ps.setInt(1, customerId);
            ps.setInt(2, customerId);
            rs = ps.executeQuery();

            recModel.setRowCount(0);
            while (rs.next()) {
                recModel.addRow(new Object[]{
                    rs.getString("Name"),
                    rs.getString("Album"),
                    rs.getString("Genre"),
                    rs.getDouble("UnitPrice")
                });
            }

        } catch (Exception ex) {
            summary.setText("Error: " + ex.getMessage());
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
