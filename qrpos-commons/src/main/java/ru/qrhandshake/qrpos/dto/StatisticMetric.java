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
    private Long terminalId;
    private Long orderTemplateId;
    private long value;
    private Date timestamp;
    private Statistic.StatisticType type;


    public static StatisticMetric create(@NotNull Statistic.StatisticType type, @NotNull Long merchantId, @NotNull Long terminalId, @Nullable Long orderTemplateId, long value, @NotNull Date timestamp) {
        try {
            Objects.requireNonNull(type, "type is null");
            Objects.requireNonNull(merchantId, "merchantId is null");
            Objects.requireNonNull(timestamp, "timestamp is null");
            Objects.requireNonNull(terminalId, "terminalId is null");
            Assert.state(value >= 0, "value less 0");

            StatisticMetric statisticMetric = new StatisticMetric();
            statisticMetric.merchantId = merchantId;
            statisticMetric.type = type;
            statisticMetric.orderTemplateId = orderTemplateId;
            statisticMetric.value = value;
            statisticMetric.timestamp = timestamp;
            statisticMetric.terminalId = terminalId;

            return statisticMetric;
        } catch (Exception e) {
            logger.error("Error create staticMetric",e);
            return null;
        }
    }

    public static StatisticMetric[] createTemplateAmount(Long merchantId, Long terminalId, Long orderTemplateId, Long amount, Date timestamp, boolean status) {
        if ( status ) {
            return Arrays.asList(
                    create(Statistic.StatisticType.AMOUNT_PAID, merchantId, terminalId, orderTemplateId, amount, timestamp),
                    create(Statistic.StatisticType.COUNT_PAID, merchantId, terminalId, orderTemplateId, 1, timestamp)
            ).toArray(new StatisticMetric[]{});
        }
        else {
            return Arrays.asList(
                    create(Statistic.StatisticType.AMOUNT_DECLINED, merchantId, terminalId, orderTemplateId, amount, timestamp),
                    create(Statistic.StatisticType.COUNT_DECLINED, merchantId, terminalId, orderTemplateId, 1, timestamp)
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

    public Long getTerminalId() {
        return terminalId;
    }

    public boolean isValid() {
        return null != merchantId && null != terminalId && null != type && 0L != value && null != timestamp;
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
