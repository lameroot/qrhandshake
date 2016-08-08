package ru.qrhandshake.qrpos.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.OrderTemplate;

/**
 * Created by lameroot on 08.08.16.
 */
@Repository
public interface OrderTemplateRepository extends CrudRepository<OrderTemplate, Long> {
}
