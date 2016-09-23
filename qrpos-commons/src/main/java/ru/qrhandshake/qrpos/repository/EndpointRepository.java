package ru.qrhandshake.qrpos.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.Endpoint;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.Merchant;

@Repository
public interface EndpointRepository extends CrudRepository<Endpoint, Long> {

    @Query("from Endpoint e where e.merchant = :merchant and e.endpointCatalog.integrationSupport = :integrationSupport and e.enabled = true")
    Endpoint findByMerchantAndIntegrationSupport(@Param("merchant")Merchant merchant, @Param("integrationSupport")IntegrationSupport integrationSupport);
}
