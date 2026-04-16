package dev.ilkersahin.java.spring.gym.integrationtests.steps;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseCleanupSteps {

    @Value("${services.gym.db.url:jdbc:mysql://localhost:3306/gymappDatabase}")
    private String gymDbUrl;

    @Value("${services.gym.db.username:root}")
    private String gymDbUsername;

    @Value("${services.gym.db.password:rootPassword}")
    private String gymDbPassword;

    @Value("${services.workload.mongo.uri:mongodb://gymapp_user:gymapp_password@localhost:27017}")
    private String mongoUri;

    @Value("${services.workload.mongo.database:test}")
    private String mongoDatabase;

    @Given("the databases are clean")
    public void cleanDatabases() throws Exception {
        cleanGymDatabase();
        cleanWorkloadDatabase();
    }

    private void cleanGymDatabase() throws Exception {
        try (Connection conn = DriverManager.getConnection(gymDbUrl, gymDbUsername, gymDbPassword);
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE trainings");
            stmt.execute("TRUNCATE TABLE trainees_trainers");
            stmt.execute("TRUNCATE TABLE trainees");
            stmt.execute("TRUNCATE TABLE trainers");
            stmt.execute("TRUNCATE TABLE username_counters");
            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("TRUNCATE TABLE blacklisted_tokens");
            stmt.execute("TRUNCATE TABLE login_attempts");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private void cleanWorkloadDatabase() {
        try (MongoClient client = MongoClients.create(mongoUri)) {
            client.getDatabase(mongoDatabase)
                    .getCollection("trainer_workload")
                    .drop();
        }
    }
}
