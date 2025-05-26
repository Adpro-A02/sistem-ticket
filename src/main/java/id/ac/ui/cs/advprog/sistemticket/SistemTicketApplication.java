package id.ac.ui.cs.advprog.sistemticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class SistemTicketApplication {

    public static void main(String[] args) {
        try {
            // Load environment variables from .env file
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
                
                // Explicitly set Spring properties from environment variables
                if (entry.getKey().equals("JWT_SECRET")) {
                    System.setProperty("jwt.secret", entry.getValue());
                }
                if (entry.getKey().equals("CORS_ALLOWED_ORIGIN")) {
                    System.setProperty("cors.allowed.origin", entry.getValue());
                }
                if (entry.getKey().equals("DB_USERNAME")) {
                    System.setProperty("spring.datasource.username", entry.getValue());
                }
                if (entry.getKey().equals("DB_PASSWORD")) {
                    System.setProperty("spring.datasource.password", entry.getValue());
                }
            });
            
            // Print environment variables for debugging
            System.out.println("Database username: " + System.getProperty("DB_USERNAME", "not set from env"));
            System.out.println("Database password is set: " + (System.getProperty("DB_PASSWORD") != null));
            System.out.println("JWT_SECRET is set: " + (System.getProperty("JWT_SECRET") != null));
            System.out.println("CORS_ALLOWED_ORIGIN: " + System.getProperty("CORS_ALLOWED_ORIGIN", "not set from env"));
            
        } catch (Exception e) {
            // Log error but continue startup
            System.err.println("Warning: Failed to load environment variables: " + e.getMessage());
        }

        // Force set critical properties for database connection
        System.setProperty("spring.datasource.username", "EventSphere_owner");
        System.setProperty("spring.datasource.password", "npg_2QiwmupIb7UX");
        System.out.println("Forced spring.datasource.username = EventSphere_owner");
        System.out.println("Forced spring.datasource.password is set = true");

        SpringApplication.run(SistemTicketApplication.class, args);
    }

}
