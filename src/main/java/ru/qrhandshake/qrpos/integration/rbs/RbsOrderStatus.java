package ru.qrhandshake.qrpos.integration.rbs;

/**
 * Created by lameroot on 20.05.16.
 */
public enum RbsOrderStatus {
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
            if ( orderStatus.getStatus() == status ) return orderStatus;
        }
        return null;
    }

    public int getStatus() {
        return status;
    }
}
