package ru.qrhandshake.qrpos.controller;

import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.ServletConfigTest;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.repository.UserRepository;


import javax.annotation.Resource;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by lameroot on 24.05.16.
 */
public class TerminalControllerTest extends ServletConfigTest {

    @Resource
    private UserRepository userRepository;

    @Test
    public void testAuth() throws Exception {
        mockMvc.perform(post("/terminal/auth")
        .param("authName","WBJa9M4Z")
        .param("authPassword","FMLa9QEW"))
                .andDo(print());
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testRegister() throws Exception {
        User user = userRepository.findByUsername("auth");
        assertNotNull(user);
        Authentication authentication = new TestingAuthenticationToken(user, null);
        mockMvc.perform(get(TerminalController.TERMINAL_PATH + TerminalController.REGISTER_PATH)
                .principal(authentication)
                .param("authName","terminal1")
                .param("authPassword","terminal1")
        ).andDo(print());
    }
}
