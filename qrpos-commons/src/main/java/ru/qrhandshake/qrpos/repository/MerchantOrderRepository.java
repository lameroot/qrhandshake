package ru.qrhandshake.qrpos.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.MerchantOrder;

import java.util.List;

/**
 * Created by lameroot on 18.05.16.
 */
@Repository
public interface MerchantOrderRepository extends PagingAndSortingRepository<MerchantOrder, Long> {

    MerchantOrder findByOrderId(String orderId);
    List<MerchantOrder> findByClient(Client client, Pageable pageable);
}
