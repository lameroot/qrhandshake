package ru.qrhandshake.qrpos.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactoryBean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by lameroot on 18.05.16.
 */
@Configuration
public class DatabaseConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private Environment environment;

    @Profile(value = "prod")
    @Bean(destroyMethod="close",name="dataSource")
    public DataSource dataSource() {
        logger.debug("Start development datasource");
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName(environment.getRequiredProperty("database.driverClassName"));
        dataSource.setUrl(environment.getRequiredProperty("database.url"));
        dataSource.setUsername(environment.getRequiredProperty("database.username"));
        dataSource.setPassword(environment.getRequiredProperty("database.password"));

        dataSource.setDefaultAutoCommit(false);
        dataSource.setDefaultTransactionIsolation(2);

        dataSource.setInitialSize(0);
        dataSource.setMinIdle(0);
        dataSource.setMaxWait(5000);

        dataSource.setValidationQuery(environment.getRequiredProperty("database.validationQuery"));
        dataSource.setTimeBetweenEvictionRunsMillis(600000);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setTestWhileIdle(true);

        dataSource.setPoolPreparedStatements(true);

        return dataSource;
    }

    @Profile(value = "test")
    @Bean
    public DataSource datasource() {
        EmbeddedDatabaseFactoryBean bean = new EmbeddedDatabaseFactoryBean();
//		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
//		databasePopulator.addScript(new ClassPathResource("hibernate/config/schema.sql"));
//		bean.setDatabasePopulator(databasePopulator);
        bean.setDatabaseType(EmbeddedDatabaseType.H2);
        bean.afterPropertiesSet();
        return bean.getObject();
    }




}
