package dev.ilkersahin.java.spring.gym.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.Setter;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
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

    @Bean("hibernateProperties")
    private static PropertiesFactoryBean hibernateProperties(){
        var bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource("hibernate.properties"));
        return bean;
    }

    @Bean
    public DataSource dataSource() {
        var hikariConfig = new HikariConfig();

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
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, @Qualifier("hibernateProperties") Properties hibernateProperties
    ) {
        var emf = new LocalContainerEntityManagerFactoryBean();

        emf.setDataSource(dataSource);
        emf.setPackagesToScan("dev.ilkersahin.java.spring.gym.model");
        emf.setPersistenceProviderClass(HibernatePersistenceProvider.class);

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(env.getProperty("hibernate.show_sql", Boolean.class, true));
        vendorAdapter.setGenerateDdl(false);
        emf.setJpaVendorAdapter(vendorAdapter);

        emf.setJpaProperties(hibernateProperties);

        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        var transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}
