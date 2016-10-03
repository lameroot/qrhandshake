package ru.qrhandshake.qrpos.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.Client;

/**
 * Created by lameroot on 25.05.16.
 */
@Repository
public interface ClientRepository extends CrudRepository<Client, Long> {

    Client findByUsername(String username);
    Client findByPhone(String phone);
    Client findByEmail(String email);
}
