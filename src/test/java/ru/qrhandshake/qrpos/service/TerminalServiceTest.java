package ru.qrhandshake.qrpos.service;

import org.junit.Test;
import ru.qrhandshake.qrpos.GeneralTest;

import javax.annotation.Resource;
import java.util.stream.IntStream;

/**
 * Created by lameroot on 25.05.16.
 */
public class TerminalServiceTest extends GeneralTest {

    @Resource
    private TerminalService terminalService;

    @Test
    public void testGenerateUniqueAuthName() {
        IntStream.range(1,10).forEach(idx->{
            String name = terminalService.generateUniqueAuthName();
            System.out.println(name);
        });
    }
}
