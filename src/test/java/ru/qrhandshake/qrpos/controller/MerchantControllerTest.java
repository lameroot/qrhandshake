package ru.qrhandshake.qrpos.controller;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.ServletConfigTest;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.repository.MerchantRepository;
import ru.qrhandshake.qrpos.repository.TerminalRepository;
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
 * Created by lameroot on 25.05.16.
 */
public class MerchantControllerTest extends ServletConfigTest {

    @Resource
    private MerchantRepository merchantRepository;
    @Resource
    private UserRepository userRepository;
    @Resource
    private TerminalRepository terminalRepository;

    @Test
    public void testExists() throws Exception {
        mockMvc.perform(get("/merchant/is_exists")
        .param("name","name"))
                .andDo(print());
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testRegister() throws Exception {
        String name = "name1";
        Merchant merchant = merchantRepository.findByName(name);
        if ( null != merchant ) {
            userRepository.delete(merchant.getUsers());
            terminalRepository.delete(merchant.getTerminals());
            merchantRepository.delete(merchant);
        }
        mockMvc.perform(post("/merchant/register")
            .param("authName","auth")
            .param("authPassword","password")
            .param("name","name1"))
                .andDo(print());
    }
}
