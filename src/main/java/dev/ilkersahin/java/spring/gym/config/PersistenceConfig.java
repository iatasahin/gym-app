package dev.ilkersahin.java.spring.gym.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.Setter;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class PersistenceConfig {

    @Setter(onMethod_ = @Autowired)
    private Environment env;

    @Bean
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setDriverClassName(env.getProperty("db.driver"));
        hikariConfig.setJdbcUrl(env.getProperty("db.url"));
        hikariConfig.setUsername(env.getProperty("db.username"));
        hikariConfig.setPassword(env.getProperty("db.password"));

        hikariConfig.setMinimumIdle(env.getProperty("db.pool.minIdle", Integer.class, 5));
        hikariConfig.setMaximumPoolSize(env.getProperty("db.pool.maxPoolSize", Integer.class, 20));
        hikariConfig.setConnectionTimeout(env.getProperty("db.pool.connectionTimeout", Long.class, 30000L));
        hikariConfig.setIdleTimeout(env.getProperty("db.pool.idleTimeout", Long.class, 600000L));
        hikariConfig.setMaxLifetime(env.getProperty("db.pool.maxLifetime", Long.class, 1800000L));

        hikariConfig.setPoolName("GymAppHikariPool");
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setAutoCommit(false);

        return new HikariDataSource(hikariConfig);
    }

    @Bean
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();

        emf.setDataSource(dataSource);
        emf.setPackagesToScan("dev.ilkersahin.java.spring.gym.model");
        emf.setPersistenceProviderClass(HibernatePersistenceProvider.class);

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(env.getProperty("hibernate.show_sql", Boolean.class, true));
        vendorAdapter.setGenerateDdl(false);
        emf.setJpaVendorAdapter(vendorAdapter);

        emf.setJpaProperties(hibernateProperties());

        return emf;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();

        properties.setProperty("hibernate.dialect",
                env.getProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect"));
        properties.setProperty("hibernate.show_sql",
                env.getProperty("hibernate.show_sql", "true"));
        properties.setProperty("hibernate.format_sql",
                env.getProperty("hibernate.format_sql", "true"));
        properties.setProperty("hibernate.use_sql_comments",
                env.getProperty("hibernate.use_sql_comments", "true"));
        properties.setProperty("hibernate.hbm2ddl.auto",
                env.getProperty("hibernate.hbm2ddl.auto", "validate"));

        properties.setProperty("hibernate.type.preferred_uuid_jdbc_type",
                env.getProperty("hibernate.type.preferred_uuid_jdbc_type", "BINARY"));

        properties.setProperty("hibernate.jdbc.batch_size",
                env.getProperty("hibernate.jdbc.batch_size", "20"));
        properties.setProperty("hibernate.order_inserts",
                env.getProperty("hibernate.order_inserts", "true"));
        properties.setProperty("hibernate.order_updates",
                env.getProperty("hibernate.order_updates", "true"));
        properties.setProperty("hibernate.jdbc.fetch_size",
                env.getProperty("hibernate.jdbc.fetch_size", "50"));

        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");

        properties.setProperty("hibernate.generate_statistics", "false");

        return properties;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}
