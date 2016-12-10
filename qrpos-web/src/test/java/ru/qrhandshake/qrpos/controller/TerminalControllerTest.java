package ru.qrhandshake.qrpos.controller;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.ServletConfigTest;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.repository.UserRepository;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Created by lameroot on 24.05.16.
 */
public class TerminalControllerTest extends ServletConfigTest {

    @Resource
    private UserRepository userRepository;

    @Test
    @Ignore
    public void testAuth() throws Exception {
        mockMvc.perform(post("/terminal/auth")
        .param("authName","merchant.auth")
        .param("authPassword","merchant.password"))
                .andDo(print());
    }

    @Test
    public void testAuthSandbox() throws Exception {
        mockMvc.perform(post("/terminal/auth")
                .param("authName","test6")
                .param("authPassword","test6"))
                .andDo(print());
    }

    @Test
    @Transactional
    @Rollback(false)
    @Ignore
    public void testRegister() throws Exception {
        User user = userRepository.findByUsername("merchant.auth");
        assertNotNull(user);
        Authentication authentication = new TestingAuthenticationToken(user, null);
        mockMvc.perform(get(TerminalController.TERMINAL_PATH + TerminalController.REGISTER_PATH)
                .principal(authentication)
                .param("authName","merchant.auth")
                .param("authPassword","merchant.auth")
        ).andDo(print());
    }
}
