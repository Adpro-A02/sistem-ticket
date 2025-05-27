package id.ac.ui.cs.advprog.sistemticket.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder
                .create()
                .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .driverClassName("org.h2.Driver")
                .username("sa")
                .password("")
                .build();
    }
}
