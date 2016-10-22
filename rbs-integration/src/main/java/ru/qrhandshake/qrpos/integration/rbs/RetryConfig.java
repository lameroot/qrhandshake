package ru.qrhandshake.qrpos.integration.rbs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.rbs.commons.cluster.keepalive.KeepaliveService;
import ru.rbs.commons.cluster.lock.DatabaseSLockProvider;
import ru.rbs.commons.cluster.retry.RetriableExecutor;
import ru.rbs.commons.cluster.retry.RetriableExecutorBuilder;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

@Configuration
public class RetryConfig {

    @Resource
    private ApplicationContext applicationContext;
    @Value("${retry.checkIntervalInSec:60}")
    private Integer checkIntervalInSec;

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(initMethod = "start")
    public RetriableExecutor retriableExecutor(DataSource dataSource) {
        return new RetriableExecutorBuilder()
                .withDataSource(dataSource)
                .withLockProvider(lockProvider(dataSource))
                .withKeepalive(keepaliveService(dataSource))
                .withSpring(applicationContext)
                .withCheckInterval(checkIntervalInSec, TimeUnit.SECONDS)
                .build();
    }

    @Bean(initMethod = "start", destroyMethod = "destroy")
    public KeepaliveService keepaliveService(DataSource dataSource) {
        return new KeepaliveService(dataSource, 60L, 60);
    }

    @Bean(initMethod = "start", destroyMethod = "destroy")
    public DatabaseSLockProvider lockProvider(DataSource dataSource) {
        return new DatabaseSLockProvider(dataSource, keepaliveService(dataSource));
    }

    @Bean
    public PaymentBindingRetryTask paymentBindingRetryTask() {
        return new PaymentBindingRetryTask();
    }

}
