package ru.qrhandshake.qrpos.integration.rbs;

import ru.qrhandshake.qrpos.integration.IntegrationOrderStatus;

/**
 * Created by lameroot on 20.05.16.
 */
public enum RbsOrderStatus implements IntegrationOrderStatus{
    CREATED(0),
    APPROVED(1),
    DEPOSITED(2),
    REVERSED(3),
    REFUNDED(4),
    REDIRECTED_TO_ACS(5),
    DECLINED(6);

    private final int status;

    RbsOrderStatus(int status) {
        this.status = status;
    }

    public static RbsOrderStatus valueOf(Integer status) {
        if ( null == status ) return null;
        for (RbsOrderStatus orderStatus : values()) {
            if ( orderStatus.status == status ) return orderStatus;
        }
        return null;
    }

    public String getStatus() {
        return String.valueOf(status);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RbsOrderStatus{");
        sb.append("status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
