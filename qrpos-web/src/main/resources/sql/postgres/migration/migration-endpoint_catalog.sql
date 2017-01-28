truncate table retriable_task;
truncate table db_lock;
truncate TABLE db_lock_keepalive;
DELETE from userpassword_endpoint;
delete from endpoint;
DELETE from "user";
DELETE FROM merchant_order where fk_terminal_id=-1;
DELETE from terminal where id=-1;
DELETE FROM merchant where id=-1;
DELETE FROM endpoint_catalog;

SELECT * FROM userpassword_endpoint where id != -1;

insert into endpoint_catalog (address, integration_support, params, id)
values ('https://3dsec.sberbank.ru/payment/webservices/merchant-ws?wsdl', 'RBS_SBRF_SOAP', null, -1);

insert into endpoint_catalog (address, integration_support, params, id)
values ('https://3dsec.sberbank.ru/payment', 'RBS_SBRF', null, -2);

INSERT INTO merchant (id, create_binding, created_date, description, integration_support, name)
values (-1, true, now(), 'paystudio-merchant', NULL , 'paystudio');

insert into terminal (id, auth_name, auth_password, enabled, fk_merchant_id)
VALUES (-1, 'paystudio-terminal','06183ed21d09e039ef6443fc529fadbaa1bcccd3cf6f27cbc1959f7eedb1b912262c357ac7d0ad9f', true, -1);
--paystudio-terminal/paystudio-terminal

insert into "user" (id, created_date, is_enabled, is_expired, is_locked, password, username, fk_merchant_id, roles)
VALUES (-1, now(), true, false, false, 'bc58dd4d0944f378b2de5a84628106b301ee86e970b453fde70248082065e362b524012938955e01', 'paystudio-admin', -1, 'ADMIN');
--paystudio-admin/paystudio-admin

insert into endpoint (enabled, fk_endpoint_catalog_id, fk_merchant_id, params, id)
values (true,-2, -1, NULL , -1);--rest integration

insert into userpassword_endpoint (id, username, password)
values (-1, 'paystudio-api','paystudio-api');

--pay.trans1_operator / bZXaPQYT
