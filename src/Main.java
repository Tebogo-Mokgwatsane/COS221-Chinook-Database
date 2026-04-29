import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main{
    public static void main(String[] args) {
        System.setProperty("CHINOOK_DB_PROTO", "jdbc:mariadb");
        System.setProperty("CHINOOK_DB_HOST", "localhost");//"172.21.226.121"
        System.setProperty("CHINOOK_DB_PORT", "3306");
        System.setProperty("CHINOOK_DB_NAME", "u25042239_chinook");
        System.setProperty("CHINOOK_DB_USERNAME", "chinook_user");
        System.setProperty("CHINOOK_DB_PASSWORD", "ChinookPass2026");
    
        /*try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}*/

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
                }
            }
        } catch (Exception e) {}

        SwingUtilities.invokeLater(ChinookApp::new);
    }
}



