package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.config.DatabaseConfig;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.OrderTemplateHistory;
import ru.qrhandshake.qrpos.repository.OrderTemplateHistoryRepository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by lameroot on 11.08.16.
 */
@Service
public class OrderTemplateHistoryService {

    @Resource
    private OrderTemplateHistoryRepository orderTemplateHistoryRepository;

    public String generateHumanOrderNumber(Long id) {//todo: придумать вариант
        return StringUtils.leftPad(String.valueOf(id), 6, "0");
    }

    public void save(OrderTemplateHistory orderTemplateHistory) {
        orderTemplateHistoryRepository.save(orderTemplateHistory);
    }

    public List<OrderTemplateHistory> getLastSuccessFromDate(Date date, Long orderTemplateId) {
        return orderTemplateHistoryRepository.findLastSuccessFromDate(date, orderTemplateId);
    }
}
