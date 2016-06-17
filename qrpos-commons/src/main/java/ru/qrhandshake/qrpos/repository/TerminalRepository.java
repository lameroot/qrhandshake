package ru.qrhandshake.qrpos.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.Terminal;

/**
 * Created by lameroot on 24.05.16.
 */
@Repository
public interface TerminalRepository extends CrudRepository<Terminal, Long>{

    public Terminal findByAuthName(String authName);
}
