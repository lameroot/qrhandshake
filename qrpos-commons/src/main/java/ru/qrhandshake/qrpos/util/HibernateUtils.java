package ru.qrhandshake.qrpos.util;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

/**
 * Created by lameroot on 26.10.16.
 */
public class HibernateUtils {

    public static <T> T initializeAndUnproxy(T entity) {
        if (entity == null) {
            throw new
                    NullPointerException("Entity passed for initialization is null");
        }

        if ( !Hibernate.isInitialized(entity) ) {
            Hibernate.initialize(entity);
        }
        if (entity instanceof HibernateProxy) {
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
                    .getImplementation();
        }
        return entity;
    }
}
