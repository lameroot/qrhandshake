package ru.qrhandshake.qrpos;

import junit.framework.TestCase;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

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
}
