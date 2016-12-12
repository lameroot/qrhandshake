package ru.qrhandshake.qrpos.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.Statistic;

import java.util.List;

@Repository
public interface StatisticRepository extends CrudRepository<Statistic, Long> {

    @Query("select sum(s.value) from Statistic s where s.type = :type and s.merchant = :merchant and s.startTime <= :start and s.endTime > :finish")
    long sumByPeriod(@Param("type")Statistic.StatisticType type, @Param("merchant")Merchant merchant, @Param("start")Long start, @Param("finish")Long finish);

    @Query("select sum(s.value) from Statistic s where s.type = :type and s.merchant = :merchant and s.orderTemplate = :orderTemplate and s.startTime <= :start and s.endTime > :finish")
    long sumByPeriod(@Param("type")Statistic.StatisticType type, @Param("merchant")Merchant merchant, @Param("orderTemplate")OrderTemplate orderTemplate, @Param("start")Long start, @Param("finish")Long finish);

    @Query("select s from Statistic s where s.type = :type and s.merchant = :merchant and s.startTime <= :start and s.endTime > :finish")
    List<Statistic> findByPeriod(@Param("type")Statistic.StatisticType type, @Param("merchant")Merchant merchant, @Param("start")Long start, @Param("finish")Long finish);

    @Query("select s from Statistic s where s.type = :type and s.merchant = :merchant and s.orderTemplate = :orderTemplate and s.startTime <= :start and s.endTime > :finish")
    List<Statistic> findByPeriod(@Param("type")Statistic.StatisticType type, @Param("merchant")Merchant merchant, @Param("orderTemplate")OrderTemplate orderTemplate, @Param("start")Long start, @Param("finish")Long finish);
}
