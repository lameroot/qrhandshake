alter table IF EXISTS BINDING drop constraint if EXISTS UK_BINDING_binding_id;
alter table IF EXISTS BINDING drop constraint if EXISTS UK_BINDING_order_id;
drop table if exists BINDING cascade;
drop sequence IF EXISTS seq_binding;

alter table IF EXISTS merchant drop constraint if EXISTS UK_MERCHANT_name;
drop table if exists merchant cascade;
drop sequence IF EXISTS seq_merchant;

alter table IF EXISTS merchant_detail drop constraint if EXISTS FK_MERCHANT_DETAIL_merchant_id;
drop table if exists merchant_detail cascade;
drop sequence IF EXISTS seq_merchant_detail;

alter table IF EXISTS "user" drop constraint if EXISTS UK_USER_username;
alter table IF EXISTS "user" drop constraint if EXISTS FK_USER_merchant_id;
drop table if exists "user" cascade;
drop sequence IF EXISTS seq_user;

alter table IF EXISTS client drop constraint if EXISTS UK_CLIENT_client_id;
drop table if exists client cascade;
drop sequence IF EXISTS seq_client;

alter table IF EXISTS terminal drop constraint if EXISTS UK_TERMINAL_auth_name;
alter table IF EXISTS terminal drop constraint if EXISTS FK_TERMINAL_merchant_id;
drop table if exists terminal cascade;
drop sequence IF EXISTS seq_terminal;

alter table IF EXISTS endpoint_catalog drop constraint if EXISTS UK_ENDPOINT_CATALOG_integration_support;
drop table if exists endpoint_catalog cascade;
drop sequence IF EXISTS seq_endpoint_catalog;

alter table IF EXISTS endpoint drop constraint if EXISTS FK_ENDPOINT_endpoint_catalog_id;
alter table IF EXISTS endpoint drop constraint if EXISTS FK_ENDPOINT_merchant_id;
drop table if exists endpoint cascade;
drop sequence IF EXISTS seq_endpoint;

alter table IF EXISTS userpassword_endpoint drop constraint if EXISTS FK_UP_ENPOINT_endpoint_id;
drop table if exists userpassword_endpoint cascade;

alter table IF EXISTS order_template drop constraint if EXISTS FK_ORDER_TEMPLATE_terminal_id;
drop table if exists order_template cascade;
drop sequence IF EXISTS seq_order_template;

drop table if exists order_template_history cascade;
drop sequence IF EXISTS seq_order_template_history;

alter table IF EXISTS merchant_order drop constraint if EXISTS UK_MERCHANT_ORDER_order_id;
alter table IF EXISTS merchant_order drop constraint if EXISTS FK_MERCHANT_ORDER_client_id;
alter table IF EXISTS merchant_order drop constraint if EXISTS FK_MERCHANT_ORDER_merchant_id;
alter table IF EXISTS merchant_order drop constraint if EXISTS FK_MERCHANT_ORDER_terminal_id;
drop table if exists merchant_order cascade;
drop sequence IF EXISTS seq_merchant_order;


