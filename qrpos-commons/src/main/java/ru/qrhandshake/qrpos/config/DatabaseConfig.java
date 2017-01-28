package ru.qrhandshake.qrpos.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Created by lameroot on 18.05.16.
 */
@Configuration
@Profile(value = "prod")
public class DatabaseConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private Environment environment;

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

//    @Profile(value = "test")
//    @Bean
//    public DataSource datasource() {
//        EmbeddedDatabaseFactoryBean bean = new EmbeddedDatabaseFactoryBean();
////		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
////		databasePopulator.addScript(new ClassPathResource("hibernate/config/schema.sql"));
////		bean.setDatabasePopulator(databasePopulator);
//        bean.setDatabaseType(EmbeddedDatabaseType.H2);
//        bean.afterPropertiesSet();
//        return bean.getObject();
//    }


}
