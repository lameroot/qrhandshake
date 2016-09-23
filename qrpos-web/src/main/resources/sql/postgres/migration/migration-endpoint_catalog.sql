insert into endpoint_catalog (address, integration_support, params, id) values ('https://3dsec.sberbank.ru/payment/webservices/merchant-ws?wsdl', 'RBS_SBRF', null, nextval ('seq_endpoint_catalog'));

--insert into endpoint (enabled, fk_endpoint_catalog_id, fk_merchant_id, params, id) values (?, ?, ?, ?, select nextval ('seq_endpoint'));
