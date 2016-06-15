package ru.qrhandshake.qrpos.repository;

import org.junit.Test;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.domain.Binding;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.PaymentWay;
import ru.qrhandshake.qrpos.service.BindingService;
import ru.qrhandshake.qrpos.service.ClientService;
import ru.qrhandshake.qrpos.service.JsonService;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by lameroot on 03.06.16.
 */
public class BindingRepositoryTest extends GeneralTest {

    @Resource
    private BindingRepository bindingRepository;
    @Resource
    private ClientService clientService;
    @Resource
    private BindingService bindingService;
    @Resource
    private JsonService jsonService;

    @Test
    public void testFindByClient() {
        Client client = clientService.findByUsername("client");
        assertNotNull(client);

        List<Binding> bindings = bindingRepository.findByClient(client);
        assertNotNull(bindings);
        for (Binding binding : bindings) {
            System.out.println(binding);
        }
    }

    @Test
    public void testFindByClientAndEnabled() {
        Client client = clientService.findByUsername("client");
        assertNotNull(client);

        List<Binding> bindings = bindingRepository.findByClientAndEnabled(client,true);
        assertNotNull(bindings);
        for (Binding binding : bindings) {
            System.out.println(binding);
        }
    }

    @Test
    public void testFindByClientAndPaymentPaymentWays() {
        Client client = clientService.findByUsername("client");
        assertNotNull(client);

        List<Binding> bindings = bindingRepository.findByClientAndPaymentsWays(client, PaymentWay.CARD, PaymentWay.BINDING);
        assertNotNull(bindings);
        for (Binding binding : bindings) {
            System.out.println(binding);
        }
    }

    @Test
    public void testExists() {
        Client client = clientService.findByUsername("client");
        assertNotNull(client);

        List<Binding> bindings = bindingRepository.findByClientAndPaymentsWays(client, PaymentWay.CARD);
        Binding binding = bindings.stream().findFirst().get();
        CardPaymentParams paymentParams = jsonService.jsonToPaymentParams(binding.getPaymentParams(), CardPaymentParams.class);
        System.out.println(paymentParams);
        //paymentParams.setYear("2018");


        boolean isExists = bindingService.isExists(client,paymentParams, binding.getPaymentWay());
        System.out.println(isExists);
    }
}
