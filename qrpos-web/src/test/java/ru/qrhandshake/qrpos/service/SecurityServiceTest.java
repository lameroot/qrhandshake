package ru.qrhandshake.qrpos.service;

import org.junit.Test;
import ru.qrhandshake.qrpos.GeneralTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by lameroot on 25.05.16.
 */
public class SecurityServiceTest extends GeneralTest {

    @Resource
    private SecurityService securityService;

    @Test
    public void testMatch() throws Exception {
        String password = "password";
        List<String> encodedPasswords = new ArrayList<>();
        encodedPasswords.add(securityService.encodePassword(password));
        encodedPasswords.add(securityService.encodePassword(password));
        encodedPasswords.add(securityService.encodePassword(password));
        encodedPasswords.add(securityService.encodePassword(password));
        encodedPasswords.add(securityService.encodePassword(password));

        for (String encodedPassword : encodedPasswords) {
            assertTrue(securityService.match(password,encodedPassword));
        }
    }

    @Test
    public void testEncode() throws Exception {
        String password = "paystudio-admin";
        String encoded = securityService.encodePassword(password);
        assertTrue(securityService.match(password,encoded));
        System.out.println(encoded);
    }
}
