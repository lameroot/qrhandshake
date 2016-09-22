package ru.qrhandshake.qrpos.repository;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.ServletConfigTest;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.domain.TerminalGroup;
import ru.qrhandshake.qrpos.domain.TerminalGroupParam;

import javax.annotation.Resource;

/**
 * User: Krainov
 * Date: 22.09.2016
 * Time: 17:07
 */
@Transactional
public class TerminalGroupRepositoryTest extends ServletConfigTest {

    @Resource
    private TerminalGroupRepository terminalGroupRepository;
    @Resource
    private TerminalGroupParamRepository terminalGroupParamRepository;

    @Test
    public void testFindById() {
        TerminalGroup terminalGroup = terminalGroupRepository.findOne(9L);
        assertNotNull(terminalGroup);
//        TerminalGroupParam terminalGroupParam1 = new TerminalGroupParam(terminalGroup, "test_name","test_value");
//        TerminalGroupParam terminalGroupParam2 = new TerminalGroupParam(terminalGroup, "test_name2","test_value2");
//        terminalGroupParamRepository.save(terminalGroupParam1);
//        terminalGroupParamRepository.save(terminalGroupParam2);
    }

    @Test
    public void testCreateTerminalGroup() {
        Merchant merchant = new Merchant();
        merchant.setName("test_merchant");
        merchantRepository.save(merchant);
        assertNotNull(merchant.getId());

        Terminal terminal = new Terminal();
        terminal.setEnabled(true);
        terminal.setAuthName("terminalAuthName");
        terminal.setAuthPassword("terminalAuthPassword");
        terminal.setMerchant(merchant);
        terminalRepository.save(terminal);
        assertNotNull(terminal.getId());

        TerminalGroup terminalGroup = new TerminalGroup();
        terminalGroup.setName("testTerminalGroup");
        terminalGroup.setMerchant(merchant);
        terminalGroup.getTerminals().add(terminal);
        terminalGroupRepository.save(terminalGroup);
        assertNotNull(terminalGroup.getId());

        TerminalGroupParam terminalGroupParam1 = new TerminalGroupParam(terminalGroup, "test_name","test_value");
        TerminalGroupParam terminalGroupParam2 = new TerminalGroupParam(terminalGroup, "test_name2","test_value2");
        terminalGroupParamRepository.save(terminalGroupParam1);
        terminalGroupParamRepository.save(terminalGroupParam2);

        terminalGroup = terminalGroupRepository.findOne(terminalGroup.getId());
        assertNotNull(terminalGroup.getTerminalGroupParams());
        for (TerminalGroupParam terminalGroupParam : terminalGroup.getTerminalGroupParams()) {
            System.out.println(terminalGroupParam);
        }

    }
}
