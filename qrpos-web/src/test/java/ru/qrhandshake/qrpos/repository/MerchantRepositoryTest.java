package ru.qrhandshake.qrpos.repository;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.dto.MerchantDto;
import ru.qrhandshake.qrpos.service.MerchantService;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * Created by lameroot on 18.05.16.
 */
@Ignore
public class MerchantRepositoryTest extends GeneralTest {

    @Resource
    private MerchantService merchantService;
    @Resource
    private MerchantRepository merchantRepository;
    @Resource
    private MerchantOrderRepository merchantOrderRepository;



    @Test
    @Transactional
    public void testFindOne() {
        MerchantOrder merchantOrder = merchantOrderRepository.findOne(9L);
        System.out.println(merchantOrder.getMerchant());
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
