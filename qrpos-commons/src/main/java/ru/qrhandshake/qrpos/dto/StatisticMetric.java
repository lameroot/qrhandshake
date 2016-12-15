package ru.qrhandshake.qrpos.dto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import ru.qrhandshake.qrpos.domain.Statistic;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class StatisticMetric {

    private final static Logger logger = LoggerFactory.getLogger(StatisticMetric.class);

    private Long merchantId;
    private Long orderTemplateId;
    private long value;
    private Date timestamp;
    private Statistic.StatisticType type;


    public static StatisticMetric create(@NotNull Statistic.StatisticType type, @NotNull Long merchantId, @Nullable Long orderTemplateId, long value, @NotNull Date timestamp) {
        try {
            Objects.requireNonNull(type, "type is null");
            Objects.requireNonNull(merchantId, "merchantId is null");
            Objects.requireNonNull(timestamp, "timestamp is null");
            Assert.state(value >= 0, "value less 0");

            StatisticMetric statisticMetric = new StatisticMetric();
            statisticMetric.merchantId = merchantId;
            statisticMetric.type = type;
            statisticMetric.orderTemplateId = orderTemplateId;
            statisticMetric.value = value;
            statisticMetric.timestamp = timestamp;

            return statisticMetric;
        } catch (Exception e) {
            logger.error("Error create staticMetric",e);
            return null;
        }
    }

    public static StatisticMetric[] createTemplateAmount(Long merchantId, Long orderTemplateId, Long amount, Date timestamp, boolean status) {
        if ( status ) {
            return Arrays.asList(
                    create(Statistic.StatisticType.TEMPLATE_AMOUNT_PAID, merchantId, orderTemplateId, amount, timestamp),
                    create(Statistic.StatisticType.TEMPLATE_COUNT_PAID, merchantId, orderTemplateId, 1, timestamp)
            ).toArray(new StatisticMetric[]{});
        }
        else {
            return Arrays.asList(
                    create(Statistic.StatisticType.TEMPLATE_AMOUNT_DECLINED, merchantId, orderTemplateId, amount, timestamp),
                    create(Statistic.StatisticType.TEMPLATE_COUNT_DECLINED, merchantId, orderTemplateId, 1, timestamp)
            ).toArray(new StatisticMetric[]{});
        }
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public Long getOrderTemplateId() {
        return orderTemplateId;
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
        return null != merchantId && null != type && 0L != value && null != timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatisticMetric{");
        sb.append("merchantId=").append(merchantId);
        sb.append(", orderTemplateId=").append(orderTemplateId);
        sb.append(", value=").append(value);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
