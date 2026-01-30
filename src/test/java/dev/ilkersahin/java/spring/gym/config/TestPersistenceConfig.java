package dev.ilkersahin.java.spring.gym.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.Setter;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
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
@PropertySource("classpath:application-test.properties")
@ComponentScan(basePackages = "dev.ilkersahin.java.spring.gym.repository")
public class TestPersistenceConfig {

    @Setter(onMethod_ = @Autowired)
    private Environment env;

    @Bean
    public DataSource dataSource() {
        var hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(env.getProperty("db.driver"));
        hikariConfig.setJdbcUrl(env.getProperty("db.url"));
        hikariConfig.setUsername(env.getProperty("db.username"));
        hikariConfig.setPassword(env.getProperty("db.password"));
        hikariConfig.setAutoCommit(false);
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("dev.ilkersahin.java.spring.gym.model");
        emf.setPersistenceProviderClass(HibernatePersistenceProvider.class);

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        vendorAdapter.setGenerateDdl(true);
        emf.setJpaVendorAdapter(vendorAdapter);

        emf.setJpaProperties(hibernateProperties());
        return emf;
    }

    private Properties hibernateProperties() {
        var props = new Properties();
        props.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
        props.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        props.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql", "true"));
        props.setProperty("hibernate.format_sql", env.getProperty("hibernate.format_sql", "true"));
        return props;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        var txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }
}
