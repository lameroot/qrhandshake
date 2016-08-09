package ru.qrhandshake.qrpos.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.Terminal;

import java.util.Set;

/**
 * Created by lameroot on 24.05.16.
 */
@Repository
public interface TerminalRepository extends CrudRepository<Terminal, Long>{

    public Terminal findByAuthName(String authName);
    public Set<Terminal> findByMerchant(Merchant merchant);
}
