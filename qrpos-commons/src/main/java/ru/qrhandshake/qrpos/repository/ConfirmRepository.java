package ru.qrhandshake.qrpos.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.Confirm;


@Repository
public interface ConfirmRepository extends CrudRepository<Confirm, Long> {

    @Query("from Confirm c where c.client = :client and c.authType = :authType and c.enabled = true")
    Confirm findByClientAndAuthType(@Param("client") Client client, @Param("authType") AuthType authType);
}
