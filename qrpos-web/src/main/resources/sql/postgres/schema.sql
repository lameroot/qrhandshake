create table BINDING (
  ID BIGINT NOT NULL PRIMARY KEY,
  binding_id varchar(255) not null,
  created_date timestamp not null,
  enabled boolean not null,
  external_binding_id varchar(255),
  integration_support varchar(255) not null,
  order_id varchar(255) not null,
  payment_params varchar(255) not null,
  payment_secure_type varchar(255),
  payment_way varchar(255) not null,
  fk_client_id int8 not null,
  constraint UK_BINDING_binding_id  unique (binding_id),
  constraint UK_BINDING_order_id  unique (order_id)
);

create sequence seq_binding;

create table merchant (
  ID BIGINT NOT NULL PRIMARY KEY,
  create_binding boolean,
  created_date timestamp,
  description varchar(255),
  integration_support varchar(255),
  name varchar(255) not null,
  constraint UK_MERCHANT_name unique (name)
);

create sequence seq_merchant;

create table merchant_detail (
  ID BIGINT NOT NULL PRIMARY KEY,
  bik varchar(255),
  inn varchar(255),
  kbk varchar(255),
  okato varchar(255),
  p2p_pan varchar(255),
  fk_merchant_id int8,
  constraint FK_MERCHANT_DETAIL_merchant_id foreign key (fk_merchant_id) references merchant
);

create sequence seq_merchant_detail;

create table "user" (
  ID BIGINT NOT NULL PRIMARY KEY,
  created_date timestamp,
  is_enabled boolean,
  is_expired boolean,
  is_locked boolean,
  password varchar(255) not null,
  roles varchar(255),
  username varchar(255) not null,
  fk_merchant_id int8,
  constraint UK_USER_username  unique (username),
  constraint FK_USER_merchant_id foreign key (fk_merchant_id) references merchant
);

create sequence seq_user;

create table client (
  ID BIGINT NOT NULL PRIMARY KEY,
  address varchar(255),
  client_id varchar(255) not null,
  email varchar(255),
  lat float8,
  lon float8,
  name varchar(255),
  password varchar(255),
  phone varchar(255),
  username varchar(255),
  confirm_code varchar(255),
  is_enabled boolean,
  is_expired boolean,
  is_locked boolean,
  constraint UK_CLIENT_client_id unique (client_id)
);

create sequence seq_client;

create table endpoint_catalog (
  ID BIGINT NOT NULL PRIMARY KEY,
  address varchar(255) not null,
  integration_support varchar(255) not null,
  params varchar(255),
  constraint UK_ENDPOINT_CATALOG_integration_support unique (integration_support)
);

create sequence seq_endpoint_catalog;

create table endpoint (
  ID BIGINT NOT NULL PRIMARY KEY,
  enabled boolean,
  params varchar(255),
  fk_endpoint_catalog_id int8 not null,
  fk_merchant_id int8 not null,
  constraint FK_ENDPOINT_endpoint_catalog_id foreign key (fk_endpoint_catalog_id) references endpoint_catalog,
  constraint FK_ENDPOINT_merchant_id foreign key (fk_merchant_id) references merchant
);

create sequence seq_endpoint;

create table userpassword_endpoint (
  password varchar(255),
  username varchar(255),
  ID BIGINT NOT NULL PRIMARY KEY,
  constraint FK_UP_ENPOINT_endpoint_id foreign key (id) references endpoint
);

create table terminal (
  ID BIGINT NOT NULL PRIMARY KEY,
  auth_name varchar(255) not null,
  auth_password varchar(255) not null,
  enabled boolean,
  fk_merchant_id int8 not null,
  constraint UK_TERMINAL_auth_name unique (auth_name),
  constraint FK_TERMINAL_merchant_id foreign key (fk_merchant_id) references merchant
);

create sequence seq_terminal;

create table order_template (
  ID BIGINT NOT NULL PRIMARY KEY,
  amount int8,
  description varchar(255),
  name varchar(255),
  fk_terminal_id int8,
  constraint FK_ORDER_TEMPLATE_terminal_id foreign key (fk_terminal_id) references terminal
);

create sequence seq_order_template;

create table order_template_history (
  ID BIGINT NOT NULL PRIMARY KEY,
  fk_client_id int8,
  date timestamp,
  device_id varchar(255),
  device_mobile_number varchar(255),
  device_model varchar(255),
  human_order_number varchar(255),
  fk_order_id int8,
  fk_order_template_id int8,
  status boolean
);

create sequence seq_order_template_history;

create table merchant_order (
  ID BIGINT NOT NULL PRIMARY KEY,
  amount int8,
  created_date timestamp not null,
  description varchar(255),
  device_id varchar(255),
  external_id varchar(255),
  external_order_status varchar(255),
  integration varchar(255),
  order_id varchar(255),
  orderStatus varchar(255),
  payment_date timestamp,
  payment_secure_type varchar(255),
  payment_type varchar(255),
  payment_way varchar(255),
  session_id varchar(255),
  fk_client_id int8,
  fk_merchant_id int8,
  fk_terminal_id int8,
  constraint UK_MERCHANT_ORDER_order_id  unique (order_id),
  constraint FK_MERCHANT_ORDER_client_id foreign key (fk_client_id) references client,
  constraint FK_MERCHANT_ORDER_merchant_id foreign key (fk_merchant_id) references merchant,
  constraint FK_MERCHANT_ORDER_terminal_id foreign key (fk_terminal_id) references terminal
);

create sequence seq_merchant_order;

create table persistent_logins (
  username varchar(64) not null,
  series varchar(64) primary key,
  token varchar(64) not null,
  last_used timestamp not null
);

create table RETRIABLE_TASK (
  UUID VARCHAR(36) NOT NULL,
  QUEUE_NAME VARCHAR(50) DEFAULT 'DEFAULT_QUEUE' NOT NULL,
  CREATED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  NEXT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  ATTEMPT_NUMBER DECIMAL(4) NOT NULL,
  CLASS_NAME VARCHAR(500) NOT NULL,
  CREATED_BY VARCHAR(36) NOT NULL,
  HOLDED_BY VARCHAR(36),
  DATA BYTEA NOT NULL,
  IV  VARCHAR(32)
);

create table RETRIABLE_TRASH (
  UUID VARCHAR(36) NOT NULL,
  QUEUE_NAME VARCHAR(50) DEFAULT 'DEFAULT_QUEUE' NOT NULL,
  CREATED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  NEXT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  ATTEMPT_NUMBER DECIMAL(4) NOT NULL,
  CLASS_NAME VARCHAR(500) NOT NULL,
  CREATED_BY VARCHAR(36) NOT NULL,
  DATA BYTEA NOT NULL,
  IV  VARCHAR(32),
  TRASH_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  TRASH_REASON VARCHAR(25) NOT NULL
);

create table XDUAL (
  DUMMY varchar(1)
);
insert into XDUAL (DUMMY) values ('X');

create table DB_LOCK (
  LOCK_ID VARCHAR(200) NOT NULL,
  PROCESS_ID VARCHAR(36) NOT NULL,
  CREATED_DATE TIMESTAMP DEFAULT current_timestamp NOT NULL
);
CREATE UNIQUE INDEX DB_LOCK_ID_INDX ON DB_LOCK(LOCK_ID);

create table DB_LOCK_KEEPALIVE (
  PROCESS_ID VARCHAR(36) NOT NULL,
  EXPIRY_DATE TIMESTAMP NOT NULL,
  CREATED_DATE TIMESTAMP DEFAULT current_timestamp NOT NULL
);
CREATE UNIQUE INDEX DB_LOCK_KEEP_INDX ON DB_LOCK_KEEPALIVE(PROCESS_ID);