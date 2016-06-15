package ru.qrhandshake.qrpos.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.Binding;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.PaymentWay;

import java.util.List;

/**
 * Created by lameroot on 31.05.16.
 */
@Repository
public interface BindingRepository extends CrudRepository<Binding, Long> {

    Binding findByOrderId(String orderId);
    Binding findByBindingId(String bindingId);
    List<Binding> findByEnabled(boolean enabled);
    @Query(value = "from Binding b where b.client = :client and b.paymentWay in (:paymentWays)")
    List<Binding> findByClientAndPaymentsWays(@Param("client") Client client, @Param("paymentWays") PaymentWay... paymentWays);
    List<Binding> findByClient(Client client);
    List<Binding> findByClientAndEnabled(Client client, boolean enabled);
}
