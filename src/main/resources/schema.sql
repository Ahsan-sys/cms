CREATE DATABASE cms;

create table users (
    id int(11) primary key AUTO_INCREMENT not null,
    uuid varchar(100) not null unique default uuid(),
    name varchar(100) not null,
    password varchar(100) not null,
    email varchar(50) not null unique,
    phone_number varchar(50),
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
    name varchar(50) unique not null,
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
    request_methods varchar(25)
);

create table urls(
    id int(11) primary key AUTO_INCREMENT not null,
    url text
);

create table categories(
    id int(11) primary key AUTO_INCREMENT not null,
    uuid varchar(100) not null unique default uuid(),
    title varchar(255) not null unique,
    type enum("doc","tmp"),
    created_dt timestamp not null default current_timestamp(),
    updated_dt timestamp not null default current_timestamp() ON update current_timestamp(),
    created_by int(11) default 0,
    updated_by int(11),
    UNIQUE KEY `title_owner` (`title`,`created_by`,`type`)
);

create table templates(
    id int(11) primary key AUTO_INCREMENT not null,
    uuid varchar(100) not null unique default uuid(),
    category_id int(11) not null,
    title varchar(255) not null,
    actual_file_name varchar(255),
    description text,
    version int(11) default 1,
    expiry_date DATETIME default null,
    created_dt timestamp not null default current_timestamp(),
    updated_dt timestamp not null default current_timestamp() ON update current_timestamp(),
    created_by int(11) default 0,
    updated_by int(11),
    UNIQUE KEY `title_owner` (`title`,`created_by`,`category_id`)
);

create table config(
    code varchar(100) primary key,
    val varchar(255),
    comments text
);

insert into profiles (name,role) value ("Super Admin","super_admin"),("User","user");
insert into urls (url) value ("/api/admin/user"),("/api/admin/profile"),("/api/cms/refresh_token");
insert into urls (url) value ("/api/cms/logout"),("/api/cms/documents"),("/api/admin/templates"),("/api/cms/documentCategories"),("/api/cms/updateUser"),
("/api/admin/templateCategories");
insert into profile_authorities (profile_id,url_id,request_methods) SELECT p.id, u.id,"*" FROM profiles p CROSS JOIN urls u WHERE p.role = 'super_admin';
insert into profile_authorities (profile_id,url_id,request_methods) SELECT p.id, u.id,"*" FROM profiles p CROSS JOIN urls u WHERE p.role = 'user' and u.url in
("/api/cms/refresh_token","/api/cms/logout","/api/cms/updateUser","/api/cms/documents","/api/cms/documentCategories","/api/cms/updateUser");

insert into config (code,val) values ("admin_documents","/home/etn/uploads/admin/templates"),("user_documents","/home/etn/uploads/cms/documents");