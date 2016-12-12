package ru.qrhandshake.qrpos.service.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.Statistic;
import ru.qrhandshake.qrpos.dto.StatisticMetrics;
import ru.qrhandshake.qrpos.repository.StatisticRepository;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class StatisticService {

    private final static Logger logger = LoggerFactory.getLogger(StatisticService.class);

    @Value("${statistics.slotInMin:60}")
    private Integer slotInMinutes;

    @Resource
    private StatisticRepository statisticRepository;

    private ExecutorService executorService = Executors.newFixedThreadPool(50);

    public void update(StatisticMetrics statisticMetrics) {
        if ( !statisticMetrics.isValid() ) return;
        executorService.submit(() -> {
            Date startTime = new Date();
            Calendar endTime = Calendar.getInstance();
            endTime.add(Calendar.MINUTE, slotInMinutes);
            logger.debug("Update from {} to {}", startTime, endTime.getTime());

            List<Statistic> statistics = null;
            if (null == statisticMetrics.getOrderTemplate()) {
                //todo: почемеу то тут возвращает больше чем надо
                statistics = statisticRepository.findByPeriod(statisticMetrics.getType(), statisticMetrics.getMerchant(), statisticMetrics.getTimestamp().getTime(), statisticMetrics.getTimestamp().getTime());
            } else {
                statistics = statisticRepository.findByPeriod(statisticMetrics.getType(), statisticMetrics.getMerchant(), statisticMetrics.getOrderTemplate(), statisticMetrics.getTimestamp().getTime(), statisticMetrics.getTimestamp().getTime());
            }

            if (null != statistics || statistics.isEmpty()) {
                Statistic statistic = new Statistic();
                statistic.setType(statisticMetrics.getType());
                statistic.setMerchant(statisticMetrics.getMerchant());
                statistic.setOrderTemplate(statisticMetrics.getOrderTemplate());
                statistic.setValue(statisticMetrics.getValue());
                statistic.setStartTime(startTime.getTime());
                statistic.setEndTime(endTime.getTimeInMillis());
                statisticRepository.save(statistic);
            } else {
                for (Statistic statistic : statistics) {
                    statistic.setValue(statistic.getValue() + statisticMetrics.getValue());
                    statisticRepository.save(statistic);
                }
            }
        });

    }

    public long sumByPeriod(Statistic.StatisticType type, Merchant merchant, OrderTemplate orderTemplate, Date startTime, Date endTime) {
        return null == orderTemplate
                ? statisticRepository.sumByPeriod(type, merchant, endTime.getTime(), startTime.getTime())
                : statisticRepository.sumByPeriod(type, merchant, orderTemplate, endTime.getTime(), startTime.getTime());
    }
}
