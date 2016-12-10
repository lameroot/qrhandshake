package ru.qrhandshake.qrpos.service;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;
import ru.qrhandshake.qrpos.GeneralTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    @Ignore
    public void testEncode() throws Exception {
        String password = "paystudio-admin";
        String encoded = securityService.encodePassword(password);
        assertTrue(securityService.match(password,encoded));
        System.out.println(encoded);
    }

    @Test
    public void testSanbox() {
        String password = "test5";
        String encoded = null;//securityService.encodePassword(password);
        encoded = "6c7a5f196c7ca240cb994135559fb9c2eecb14a62661e7cff327f9774ade0f035911c7e2125243ae";
        assertTrue(securityService.match(password,encoded));
        System.out.println(encoded);
    }
}
