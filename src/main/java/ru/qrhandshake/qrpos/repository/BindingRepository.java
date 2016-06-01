package ru.qrhandshake.qrpos.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.Binding;

import java.util.List;

/**
 * Created by lameroot on 31.05.16.
 */
@Repository
public interface BindingRepository extends CrudRepository<Binding, Long> {

    Binding findByOrderId(String orderId);
    Binding findByBindingId(String bindingId);
}
