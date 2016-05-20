package ru.qrhandshake.qrpos.config;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import ru.qrhandshake.qrpos.GeneralTest;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Created by lameroot on 18.05.16.
 */
public class DatabaseConfigTest extends GeneralTest {

    @Resource
    private DataSource dataSource;

    @Test
    public void testExists() {
        assertNotNull(dataSource);
        System.out.println(dataSource);
    }
}
