package ru.qrhandshake.qrpos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.OrderTemplateHistory;

import java.util.Date;
import java.util.List;

/**
 * Created by lameroot on 10.08.16.
 */
@Repository
public interface OrderTemplateHistoryRepository extends PagingAndSortingRepository<OrderTemplateHistory,Long> {

    @Query("from OrderTemplateHistory oth where oth.orderTemplateId = :orderTemplateId and oth.date >= :date and oth.status = true order by oth.date desc")
    List<OrderTemplateHistory> findLastSuccessFromDate(@Param("date")Date date, @Param("orderTemplateId")Long orderTemplateId);

    OrderTemplateHistory findByMerchantOrderId(Long MerchantOrderId);
}
