-- Usuarios
create table if not exists users (
    id_usuario int primary key auto_increment,
    nombre_apellidos varchar(100),
    usuario varchar(50) not null,
    correo varchar(80) not null,
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
