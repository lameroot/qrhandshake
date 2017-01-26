package ru.qrhandshake.qrpos.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.Statistic;

import java.util.List;
import java.util.Set;

@Repository
public interface StatisticRepository extends CrudRepository<Statistic, Long> {

    @Query("select sum(s.value) from Statistic s where s.type = :type and s.merchantId = :merchantId and s.startTime <= :start and s.endTime > :finish")
    Long sumByPeriod(@Param("type")Statistic.StatisticType type, @Param("merchantId")Long merchantId, @Param("start")Long start, @Param("finish")Long finish);

    @Query("select sum(s.value) from Statistic s where s.type = :type and s.merchantId = :merchantId and s.orderTemplateId in :orderTemplateIds and s.startTime <= :start and s.endTime > :finish")
    Long sumByPeriodForOrderTemplates(@Param("type")Statistic.StatisticType type, @Param("merchantId")Long merchantId, @Param("orderTemplateIds")Long[] orderTemplateIds, @Param("start")Long start, @Param("finish")Long finish);

    @Query("select sum(s.value) from Statistic s where s.type = :type and s.merchantId = :merchantId and s.terminalId in :terminalIds and s.startTime <= :start and s.endTime > :finish")
    Long sumByPeriodForTerminals(@Param("type")Statistic.StatisticType type, @Param("merchantId")Long merchantId, @Param("terminalIds")Long[] terminalIds, @Param("start")Long start, @Param("finish")Long finish);

    @Query("select s from Statistic s where s.type = :type and s.merchantId = :merchantId and s.startTime <= :start and s.endTime > :finish")
    List<Statistic> findByPeriod(@Param("type")Statistic.StatisticType type, @Param("merchantId")Long merchantId, @Param("start")Long start, @Param("finish")Long finish);

    @Query("select s from Statistic s where s.type = :type and s.merchantId = :merchantId and s.orderTemplateId = :orderTemplateId and s.startTime <= :start and s.endTime > :finish")
    List<Statistic> findByPeriod(@Param("type")Statistic.StatisticType type, @Param("merchantId")Long merchantId, @Param("orderTemplateId")Long orderTemplateId, @Param("start")Long start, @Param("finish")Long finish);
}
