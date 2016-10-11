package ru.qrhandshake.qrpos.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
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
    @Query("from MerchantOrder mo where mo.client = :client and mo.orderStatus = 'PAID' and mo.merchant.id != -1")
    List<MerchantOrder> findByClient(@Param("client") Client client, Pageable pageable);
}
