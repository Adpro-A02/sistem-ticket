package id.ac.ui.cs.advprog.sistemticket.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://ep-ancient-cell-a1csuhot-pooler.ap-southeast-1.aws.neon.tech/EventSphere?sslmode=require");
        dataSource.setUsername("EventSphere_owner");
        dataSource.setPassword("npg_2QiwmupIb7UX");
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setMaximumPoolSize(5);
        dataSource.setConnectionTimeout(20000);
        
        System.out.println("Creating datasource with hardcoded credentials");
        return dataSource;
    }
}
