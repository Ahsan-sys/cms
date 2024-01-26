CREATE DATABASE cms;

create table users (
    id int(11) primary key AUTO_INCREMENT not null,
    uuid varchar(100) not null unique default uuid(),
    name varchar(100) not null,
    password varchar(100) not null,
    email varchar(50) not null unique,
    password_change_dt timestamp,
    created_dt timestamp not null default current_timestamp(),
    updated_dt timestamp not null default current_timestamp() ON update current_timestamp(),
    created_by int(11),
    updated_by int(11),
    is_deleted tinyint(1) default 0,
    is_active tinyint(1) default 0
);

create table profiles (
    id int(11) primary key AUTO_INCREMENT not null,
    role varchar(50) unique not null,
    created_dt timestamp not null default current_timestamp(),
    updated_dt timestamp not null default current_timestamp() ON update current_timestamp(),
    created_by int(11),
    updated_by int(11),
    is_deleted tinyint(1) default 0,
    is_active tinyint(1) default 0
);

create table user_profiles(
    user_id int(11) not null,
    profile_id int(11) not null,
    unique KEY `uk_user_profil` (`user_id`,`profile_id`)
);

create table user_sessions (
    id int(11) primary key AUTO_INCREMENT not null,
    user_id int(11) not null,
    access_token varchar(255),
    refresh_token varchar(255),
    start_dt timestamp not null default current_timestamp(),
    end_dt timestamp not null default current_timestamp()
);

create table profile_authorities(
    id int(11) primary key AUTO_INCREMENT not null,
    profile_id int(11) not null,
    url_id int(11) default 0,
    url_group_id int(11) default 0
);

create table cms_urls(
    id int(11) primary key AUTO_INCREMENT not null,
    url_name varchar(255),
    url text,
    url_group_id int(11) default 0
);

create table cms_url_groups(
    id int(11) primary key AUTO_INCREMENT not null,
    group_name varchar(255)
);
