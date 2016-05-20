package ru.qrhandshake.qrpos.repository;

import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.dto.MerchantDto;
import ru.qrhandshake.qrpos.service.MerchantService;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * Created by lameroot on 18.05.16.
 */
public class MerchantRepositoryTest extends GeneralTest {

    @Resource
    private MerchantService merchantService;
    @Resource
    private MerchantRepository merchantRepository;

    @Test
    public void testCreate() {
        Merchant merchant = merchantService.create(createMerchantDto("first"));
        assertNotNull(merchant);
        assertNotNull(merchant.getId());

        Merchant merchant1 = merchantRepository.findByName("first");
        assertNotNull(merchant1);
        assertEquals(merchant.getUsername(),merchant1.getUsername());
        Merchant merchant2 = merchantRepository.findByUsername(merchant.getUsername());
        assertNotNull(merchant2);

        UserDetails userDetails = merchantService.loadUserByUsername(merchant.getUsername());
        assertNotNull(userDetails);
    }

    private MerchantDto createMerchantDto(String name) {
        MerchantDto merchantDto = new MerchantDto();
        merchantDto.setName(name);
        String unique = UUID.randomUUID().toString();
        merchantDto.setUsername(name + "_" +  unique);
        merchantDto.setPassword(unique);
        merchantDto.setContact("Moscow");
        merchantDto.setDescription("this is description");

        return merchantDto;
    }
}
