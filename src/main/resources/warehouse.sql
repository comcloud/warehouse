create database warehouse;
use warehouse;

create table if not exists warehouse.product
(
    id         int auto_increment
    primary key,
    name       varchar(50) null,
    unit       varchar(10) null,
    stock      varchar(10) null,
    unit_price varchar(10) null
    );

create index name_unit_idx
    on warehouse.product (name, unit);

