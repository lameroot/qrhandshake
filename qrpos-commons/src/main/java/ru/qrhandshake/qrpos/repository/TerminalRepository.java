package ru.qrhandshake.qrpos.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.Terminal;

/**
 * Created by lameroot on 24.05.16.
 */
@Repository
public interface TerminalRepository extends CrudRepository<Terminal, Long>{

    public Terminal findByAuthName(String authName);
    @Query("from Terminal t where t.defaultTerminal is not null and t.merchant = :merchant")
    public Terminal findDefaultTerminalForMerchant(@Param("merchant") Merchant merchant);
}
