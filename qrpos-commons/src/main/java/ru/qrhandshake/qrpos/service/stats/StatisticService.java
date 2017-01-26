package ru.qrhandshake.qrpos.service.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.domain.Statistic;
import ru.qrhandshake.qrpos.dto.StatisticMetric;
import ru.qrhandshake.qrpos.repository.StatisticRepository;

import javax.annotation.Resource;
import java.util.Arrays;
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

    public void update(StatisticMetric... statisticMetrics) {
        try {
            executorService.submit(() -> {
                Date startTime = new Date();
                Calendar endTime = Calendar.getInstance();
                endTime.add(Calendar.MINUTE, slotInMinutes);
                logger.debug("Update from {} to {}", startTime, endTime.getTime());
                Arrays.asList(statisticMetrics).stream().filter(s -> null != s && s.isValid()).forEach(statisticMetric -> {
                    Long timestamp = statisticMetric.getTimestamp().getTime();
                    List<Statistic> statistics =
                            null != statisticMetric.getOrderTemplateId()
                                    ? statisticRepository.findByPeriod(statisticMetric.getType(), statisticMetric.getMerchantId(), statisticMetric.getOrderTemplateId(), timestamp, timestamp)
                                    : statisticRepository.findByPeriod(statisticMetric.getType(), statisticMetric.getMerchantId(), timestamp, timestamp)
                            ;

                    if (null == statistics || statistics.isEmpty()) {
                        Statistic statistic = new Statistic();
                        statistic.setType(statisticMetric.getType());
                        statistic.setMerchantId(statisticMetric.getMerchantId());
                        statistic.setOrderTemplateId(statisticMetric.getOrderTemplateId());
                        statistic.setTerminalId(statisticMetric.getTerminalId());
                        statistic.setValue(statisticMetric.getValue());
                        statistic.setStartTime(startTime.getTime());
                        statistic.setEndTime(endTime.getTimeInMillis());
                        statisticRepository.save(statistic);
                    } else {
                        for (Statistic statistic : statistics) {
                            statistic.setValue(statistic.getValue() + statisticMetric.getValue());
                            statisticRepository.save(statistic);
                        }
                    }
                });
            });
        } catch (Exception e) {
            logger.error("Error update statistics: " + Arrays.toString(statisticMetrics),e);
        }
    }

    public Long sumByPeriodForTerminals(Statistic.StatisticType type, Date startTime, Date endTime, Long merchantId, Long... terminalIds) {
        return null == terminalIds || 0 == terminalIds.length
                ? statisticRepository.sumByPeriod(type, merchantId, endTime.getTime(), startTime.getTime())
                : statisticRepository.sumByPeriodForTerminals(type, merchantId, terminalIds, endTime.getTime(), startTime.getTime());
    }

    public Long sumByPeriodByOrderTemplates(Statistic.StatisticType type, Date startTime, Date endTime, Long merchantId, Long... orderTemplateIds) {
        return null == orderTemplateIds || 0 == orderTemplateIds.length
                ? statisticRepository.sumByPeriod(type, merchantId, endTime.getTime(), startTime.getTime())
                : statisticRepository.sumByPeriodForOrderTemplates(type, merchantId, orderTemplateIds, endTime.getTime(), startTime.getTime());
    }
}
