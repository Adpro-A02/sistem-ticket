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
            });
        } catch (Exception e) {
            // Log error but continue startup
            System.err.println("Warning: Failed to load environment variables: " + e.getMessage());
        }

        SpringApplication.run(SistemTicketApplication.class, args);
    }

}
