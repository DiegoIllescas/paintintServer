DROP DATABASE IF EXISTS painting;
CREATE DATABASE painting;
USE painting;

CREATE TABLE PublicKey (
	id int not null primary key auto_increment,
    email varchar(255) unique,
    publickey text
);

CREATE TABLE User (
	id int not null primary key auto_increment,
    email varchar(255) unique not null,
    password varchar(33),
    type ENUM('painter','chairman','judge') not null,
    name varchar(255),
    foreign key (email) references PublicKey(email) on delete cascade
);

CREATE TABLE Painting (
	id int not null primary key auto_increment,
    user_id int not null,
    local_url varchar(255),
    title varchar(255),
    description text,
    iv varchar(255),
    upload_time timestamp default current_timestamp,
    foreign key (user_id) references User(id) on delete cascade
);

CREATE TABLE AESKey (
    painting_id int not null,
    judge_id int not null,
    aeskey text,
    foreign key (painting_id) references Painting(id) on delete cascade,
    foreign key (judge_id) references User(id) on delete cascade
);

CREATE TABLE Evaluation (
	id int not null primary key auto_increment,
    judge_id int not null,
    painting_id int not null,
    stars int,
    comments text,
    blind_message text,
    inv text,
    signature text,
    foreign key (painting_id) references Painting(id) on delete cascade,
    foreign key (judge_id) references User(id) on delete cascade
);