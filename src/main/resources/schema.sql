CREATE DATABASE cms;

create table users (
    id int(11) primary key AUTO_INCREMENT not null,
    uuid varchar(100) not null unique default uuid(),
    name varchar(100) not null,
    password varchar(100) not null,
    email varchar(50) not null unique,
    phone_number varchar(50) not null,
    profile_id int(11),
    password_change_dt timestamp,
    created_dt timestamp not null default current_timestamp(),
    updated_dt timestamp not null default current_timestamp() ON update current_timestamp(),
    created_by int(11) default 0,
    updated_by int(11),
    is_deleted tinyint(1) default 0,
    is_active tinyint(1) default 0
);

create table profiles (
    id int(11) primary key AUTO_INCREMENT not null,
    uuid varchar(100) not null unique default uuid(),
    role varchar(50) unique not null,
    created_dt timestamp not null default current_timestamp(),
    updated_dt timestamp not null default current_timestamp() ON update current_timestamp(),
    created_by int(11) default 0,
    updated_by int(11),
    is_deleted tinyint(1) default 0,
    is_active tinyint(1) default 0
);

create table user_sessions (
    user_id int(11) primary key not null,
    access_token varchar(255),
    refresh_token varchar(255)
);

create table profile_authorities(
    id int(11) primary key AUTO_INCREMENT not null,
    profile_id int(11) not null,
    url_id int(11) default 0,
    request_methods varchar(100)
);

create table urls(
    id int(11) primary key AUTO_INCREMENT not null,
    url_name varchar(255),
    url text
);

create table config(
    code varchar(100) primary key,
    val varchar(255),
    comments text
);