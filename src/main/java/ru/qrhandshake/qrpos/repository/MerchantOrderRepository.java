package ru.qrhandshake.qrpos.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.MerchantOrder;

/**
 * Created by lameroot on 18.05.16.
 */
@Repository
public interface MerchantOrderRepository extends CrudRepository<MerchantOrder, Long> {

    MerchantOrder findByOrderId(String orderId);
    MerchantOrder findBySessionId(String sessionId);
}
