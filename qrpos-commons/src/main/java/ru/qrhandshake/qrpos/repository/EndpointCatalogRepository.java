package ru.qrhandshake.qrpos.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.qrhandshake.qrpos.domain.EndpointCatalog;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;

@Repository
public interface EndpointCatalogRepository extends CrudRepository<EndpointCatalog,Long> {

    EndpointCatalog findByIntegrationSupport(IntegrationSupport integrationSupport);
}
