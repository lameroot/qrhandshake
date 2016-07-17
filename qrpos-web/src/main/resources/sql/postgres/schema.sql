create table client (id bigint not null, address varchar(255), email varchar(255), ip varchar(255), lat double not null, lon double not null, name varchar(255), phone varchar(255), primary key (id));
create table merchant_detail (id bigint not null, bik varchar(255), inn varchar(255), kbk varchar(255), okato varchar(255), p2p_pan varchar(255), fk_merchant_id bigint, primary key (id));
create table merchant_order (id bigint not null, amount bigint, approved_date timestamp, confirmed_date timestamp, created_date timestamp not null, currency varchar(255), description varchar(255), expired_date timestamp, external_id varchar(255), fee bigint, integration varchar(255), language varchar(255), payment_date timestamp, orderStatus varchar(255), fk_merchant_id bigint, primary key (id));

create table persistent_logins (
	username varchar(64) not null,
	series varchar(64) primary key,
	token varchar(64) not null,
	last_used timestamp not null
	);