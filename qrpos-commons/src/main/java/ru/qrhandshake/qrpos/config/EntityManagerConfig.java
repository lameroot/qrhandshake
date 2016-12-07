package ru.qrhandshake.qrpos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by lameroot on 18.05.16.
 */
@Configuration
//@EnableTransactionManagement(proxyTargetClass = true)
public class EntityManagerConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private Environment environment;
    @Resource
    private DataSource dataSource;


    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setJpaVendorAdapter(vendorAdapter());
        entityManagerFactory.setPackagesToScan(getPackagesToScan());
        entityManagerFactory.setJpaProperties(jpaProperties());
        entityManagerFactory.afterPropertiesSet();
        return entityManagerFactory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory());
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslator exceptionTranslation() {
        return new HibernateExceptionTranslator();
    }

    public String[] getPackagesToScan() {
        return new String[]{"ru.qrhandshake.qrpos.domain"};
    };

    @Bean
    public Properties jpaProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.put("hibernate.jdbc.batch_size", 100);
        if ( environment.containsProperty("hibernate.hbm2ddl.auto") ) {
            properties.put("hibernate.hbm2ddl.auto", environment.getProperty("hibernate.hbm2ddl.auto"));
        }
        if ( environment.containsProperty("hibernate.hbm2ddl.import_files") ) {
            properties.put("hibernate.hbm2ddl.import_files", environment.getProperty("hibernate.hbm2ddl.import_files"));
        }
        properties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory");
        properties.put("hibernate.cache.use_second_level_cache", "true");
        properties.put("hibernate.cache.use_query_cache", "true");
        properties.put("hibernate.connection.CharSet","utf8");
        properties.put("hibernate.connection.characterEncoding","utf8");
        properties.put("hibernate.connection.useUnicode","true");
        return properties;
    }


    @Bean
    public JpaVendorAdapter vendorAdapter() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(showSql());
        vendorAdapter.setDatabasePlatform(hibernateDialect());
        return vendorAdapter;
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager());
    }

    public boolean showSql() {
        Boolean showSql = environment.getProperty("hibernate.show_sql",Boolean.class);
        if ( null != showSql ) return showSql;
        return false;
    }


    public String hibernateDialect() {
        return environment.getRequiredProperty("hibernate.dialect");
    }


    /*
    @Bean
    public EntityManagerFactory entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setPackagesToScan(new String[]{"ru.qrhandshake.qrpos.domain"});
        entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter());
        entityManagerFactory.afterPropertiesSet();
        return entityManagerFactory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory());
        transactionManager.setDataSource(dataSource);
        transactionManager.setJpaDialect(new HibernateJpaDialect());
        return transactionManager;
    }

    HibernateJpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(true);
        adapter.setGenerateDdl(true);
        adapter.setDatabase(Database.H2);
        return adapter;
    }
    */

}
