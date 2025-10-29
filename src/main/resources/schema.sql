-- Usuarios
create table if not exists users (
    username varchar(50) primary key,
    password varchar(100) not null,
    enabled boolean not null
    );

-- Roles (autoridades)
create table if not exists authorities (
    username varchar(50) not null,
    authority varchar(50) not null,
    constraint fk_auth_user foreign key(username) references users(username)
    );

create unique index if not exists ix_auth_username on authorities (username, authority);
