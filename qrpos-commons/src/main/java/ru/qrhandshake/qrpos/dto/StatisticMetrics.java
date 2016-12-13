package ru.qrhandshake.qrpos.dto;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.Statistic;

import java.util.Date;
import java.util.Objects;

public class StatisticMetrics {

    private Merchant merchant;
    private OrderTemplate orderTemplate;
    private long value;
    private Date timestamp;
    private Statistic.StatisticType type;


    public static StatisticMetrics create(@NotNull Statistic.StatisticType type, @NotNull Merchant merchant, @NotNull OrderTemplate orderTemplate, long value, @NotNull Date timestamp) {
        Objects.requireNonNull(type,"type is null");
        Objects.requireNonNull(merchant, "merchant is null");
        Objects.requireNonNull(timestamp,"timestamp is null");
        Objects.requireNonNull(orderTemplate,"orderTemplate is null");
        Assert.state(value >= 0,"value less 0");

        StatisticMetrics statisticMetrics = new StatisticMetrics();
        statisticMetrics.merchant = merchant;
        statisticMetrics.type = type;
        statisticMetrics.orderTemplate = orderTemplate;
        statisticMetrics.value = value;
        statisticMetrics.timestamp = timestamp;

        return statisticMetrics;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public OrderTemplate getOrderTemplate() {
        return orderTemplate;
    }

    public long getValue() {
        return value;
    }

    public Statistic.StatisticType getType() {
        return type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isValid() {
        return null != merchant && null != orderTemplate && null != type && 0L != value && null != timestamp;
    }
}
