package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.qrhandshake.qrpos.api.OrderTemplateHistoryParams;
import ru.qrhandshake.qrpos.api.OrderTemplateHistoryResult;
import ru.qrhandshake.qrpos.domain.OrderTemplateHistory;
import ru.qrhandshake.qrpos.repository.OrderTemplateHistoryRepository;
import ru.qrhandshake.qrpos.util.MaskUtil;

import javax.annotation.Resource;
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

    public OrderTemplateHistoryResult getOrders(OrderTemplateHistoryParams orderTemplateHistoryParams, Pageable pageable) {
        OrderTemplateHistoryResult orderTemplateHistoryResult = new OrderTemplateHistoryResult();
        Sort.Direction direction = Sort.Direction.ASC;
        if ( null != pageable && null != pageable.getSort() && null != pageable.getSort().getOrderFor("id")) {
            Sort sort = pageable.getSort();
            direction = sort.getOrderFor("id").getDirection();
        }
        Page<OrderTemplateHistory> orderTemplateHistories = null;
        switch (direction) {
            case ASC: {
                Assert.notNull(orderTemplateHistoryParams.getId(),"Id must not be null for this request");
                orderTemplateHistories = orderTemplateHistoryRepository.findByOrderTemplateIdAndStatusAndIdGreaterThan(orderTemplateHistoryParams.getOrderTemplateId(), orderTemplateHistoryParams.isStatus(), orderTemplateHistoryParams.getId(), pageable);
                break;
            }
            case DESC: {
                if ( null != orderTemplateHistoryParams.getId() ) {
                    orderTemplateHistories = orderTemplateHistoryRepository.findByOrderTemplateIdAndStatusAndIdLessThan(orderTemplateHistoryParams.getOrderTemplateId(),orderTemplateHistoryParams.isStatus(), orderTemplateHistoryParams.getId(), pageable);
                }
                else {
                    orderTemplateHistories = orderTemplateHistoryRepository.findByOrderTemplateIdAndStatus(orderTemplateHistoryParams.getOrderTemplateId(), orderTemplateHistoryParams.isStatus(), pageable);
                }
                break;
            }
        }
        for (OrderTemplateHistory orderTemplateHistory : orderTemplateHistories) {
            orderTemplateHistoryResult.getOrders().add(new OrderTemplateHistoryResult.OrderTemplateHistoryData()
                            .setDeviceMobileNumberMasked(MaskUtil.getMaskedMobileNumber(orderTemplateHistory.getDeviceMobileNumber()))
                            .setDate(orderTemplateHistory.getDate())
                            .setDeviceModel(orderTemplateHistory.getDeviceModel())
                            .setHumanOrderNumber(orderTemplateHistory.getHumanOrderNumber())
                            .setId(orderTemplateHistory.getId())
            );
        }
        return orderTemplateHistoryResult;
    }

}
