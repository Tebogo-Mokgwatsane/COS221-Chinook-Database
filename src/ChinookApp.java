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



        setVisible(true);
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
