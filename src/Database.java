import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/gestion_etudiants";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion reussie !");
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("erreur de connexion !");
            return null;
        }
    }
}
