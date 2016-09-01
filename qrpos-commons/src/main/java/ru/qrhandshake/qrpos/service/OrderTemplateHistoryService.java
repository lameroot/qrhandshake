package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.qrhandshake.qrpos.api.OrderTemplateHistoryParams;
import ru.qrhandshake.qrpos.api.OrderTemplateHistoryResult;
import ru.qrhandshake.qrpos.config.DatabaseConfig;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.OrderTemplateHistory;
import ru.qrhandshake.qrpos.repository.ClientRepository;
import ru.qrhandshake.qrpos.repository.OrderTemplateHistoryRepository;
import ru.qrhandshake.qrpos.util.MaskUtil;

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

    public OrderTemplateHistory findByOrderTemplateIdAndMerchantOrderId(Long merchantOrderId) {
        return orderTemplateHistoryRepository.findByMerchantOrderId(merchantOrderId);
    }

    public OrderTemplateHistoryResult getLastSuccessFromDate(OrderTemplateHistoryParams orderTemplateHistoryParams) {
        OrderTemplateHistoryResult orderTemplateHistoryResult = new OrderTemplateHistoryResult();
        List<OrderTemplateHistory> orderTemplateHistories = orderTemplateHistoryRepository.findLastSuccessFromDate(orderTemplateHistoryParams.getFrom(), orderTemplateHistoryParams.getOrderTemplateId());
        for (OrderTemplateHistory orderTemplateHistory : orderTemplateHistories) {
            orderTemplateHistoryResult.getOrders().add(new OrderTemplateHistoryResult.OrderTemplateHistoryData()
                    .setDeviceMobileNumberMasked(MaskUtil.getMaskedMobileNumber(orderTemplateHistory.getDeviceMobileNumber()))
                    .setDate(orderTemplateHistory.getDate())
                    .setDeviceModel(orderTemplateHistory.getDeviceModel())
                    .setHumanOrderNumber(orderTemplateHistory.getHumanOrderNumber())
            );
        }
        return orderTemplateHistoryResult;
    }

    public OrderTemplateHistoryResult getFromId(OrderTemplateHistoryParams orderTemplateHistoryParams, Pageable pageable) {
        Assert.notNull(orderTemplateHistoryParams.getId(),"Id must not be null for this request");
        OrderTemplateHistoryResult orderTemplateHistoryResult = new OrderTemplateHistoryResult();
        Page<OrderTemplateHistory> orderTemplateHistoryPage = orderTemplateHistoryRepository.findByOrderTemplateIdAndStatusAndIdGreaterThanEqual(orderTemplateHistoryParams.getOrderTemplateId(), orderTemplateHistoryParams.isStatus(), orderTemplateHistoryParams.getId(), pageable);
        for (OrderTemplateHistory orderTemplateHistory : orderTemplateHistoryPage) {
            orderTemplateHistoryResult.getOrders().add(new OrderTemplateHistoryResult.OrderTemplateHistoryData()
                    .setDeviceMobileNumberMasked(MaskUtil.getMaskedMobileNumber(orderTemplateHistory.getDeviceMobileNumber()))
                    .setDate(orderTemplateHistory.getDate())
                    .setDeviceModel(orderTemplateHistory.getDeviceModel())
                    .setHumanOrderNumber(orderTemplateHistory.getHumanOrderNumber())
            );
        }
        return orderTemplateHistoryResult;
    }

    public OrderTemplateHistoryResult getUntilId(OrderTemplateHistoryParams orderTemplateHistoryParams, Pageable pageable) {
        Assert.notNull(orderTemplateHistoryParams.getId(),"Id must not be null for this request");
        OrderTemplateHistoryResult orderTemplateHistoryResult = new OrderTemplateHistoryResult();
        Page<OrderTemplateHistory> orderTemplateHistoryPage = orderTemplateHistoryRepository.findByOrderTemplateIdAndStatusAndIdLessThanEqual(orderTemplateHistoryParams.getOrderTemplateId(), orderTemplateHistoryParams.isStatus(), orderTemplateHistoryParams.getId(), pageable);
        for (OrderTemplateHistory orderTemplateHistory : orderTemplateHistoryPage) {
            orderTemplateHistoryResult.getOrders().add(new OrderTemplateHistoryResult.OrderTemplateHistoryData()
                            .setDeviceMobileNumberMasked(MaskUtil.getMaskedMobileNumber(orderTemplateHistory.getDeviceMobileNumber()))
                            .setDate(orderTemplateHistory.getDate())
                            .setDeviceModel(orderTemplateHistory.getDeviceModel())
                            .setHumanOrderNumber(orderTemplateHistory.getHumanOrderNumber())
            );
        }
        return orderTemplateHistoryResult;
    }
}
