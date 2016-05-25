package ru.qrhandshake.qrpos.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.domain.Merchant;

/**
 * Created by lameroot on 18.05.16.
 */
@Repository
public interface MerchantRepository extends CrudRepository<Merchant, Long>{

    @Transactional(readOnly = true)
    public Merchant findByName(String name);
}
