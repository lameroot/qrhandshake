package ru.qrhandshake.qrpos.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.Client;

/**
 * Created by lameroot on 25.05.16.
 */
@Repository
public interface ClientRepository extends CrudRepository<Client, Long> {

    public Client findByUsername(String username);
    public Client findByPhone(String phone);
    public Client findByEmail(String email);
}
