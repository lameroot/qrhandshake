package ru.qrhandshake.qrpos;

import junit.framework.TestCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import ru.qrhandshake.qrpos.service.stats.StatisticService;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Created by lameroot on 24.09.16.
 */
@Sql(scripts = {
//        "classpath:drop-table.sql",
//        "classpath:sql/postgres/schema.sql",
        "classpath:sql/postgres/migration/migration-endpoint_catalog.sql"
})
@TestPropertySource(value = "classpath:test-config.properties")
public class AbstractTest extends TestCase {

        @Resource
        protected Environment environment;
        @Resource
        protected StatisticService statisticService;

        @Configuration
        @Profile(value = "test")
        public static class TestDataSourceConfig {

                @Bean
                public DataSource testDataSource() {
                        return new EmbeddedDatabaseBuilder()
                                .setType(EmbeddedDatabaseType.HSQL)
                                .addScript("sql/postgres/schema.sql")
                                .build();
                }


        }
}
