package ru.qrhandshake.qrpos.controller;

import org.junit.Test;
import ru.qrhandshake.qrpos.ServletConfigTest;


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

    @Test
    public void testAuth() throws Exception {
        mockMvc.perform(post("/terminal/auth")
        .param("authName","auth")
        .param("authPassword","password"))
                .andDo(print());
    }

    @Test
    public void testRegister() throws Exception {
        mockMvc.perform(get(TerminalController.TERMINAL_PATH + TerminalController.REGISTER_PATH)
                //.param("login","login")
                .param("password", "password")
                .param("merchant_id", "1111"))
                .andDo(print());
    }
}
