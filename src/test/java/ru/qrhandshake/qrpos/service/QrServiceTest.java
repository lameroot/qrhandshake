package ru.qrhandshake.qrpos.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import ru.qrhandshake.qrpos.GeneralTest;

import javax.annotation.Resource;

/**
 * Created by lameroot on 18.05.16.
 */
public class QrServiceTest extends GeneralTest {

    @Value("${qr.path}")
    private String filePath;

    @Resource
    private QrService qrService;

    @Test
    public void testFilePath() {
        assertNotNull(filePath);
        System.out.println(filePath);
    }

    @Test
    public void testExists() {
        assertNotNull(qrService);
    }

    @Test
    public void testGenerate() {
        qrService.generate("this is test");
    }
}
