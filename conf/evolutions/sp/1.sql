# User schema

# --- !Ups
create table user (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  role TEXT NOT NULL,
  userName TEXT NOT NULL,
  password TEXT NOT NULL,
  email TEXT NOT NULL
);

create table upload (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  path TEXT NOT NULL,
  timestamp BIGINT NOT NULL,
  userId INT NOT NULL,
  FOREIGN KEY (userId) REFERENCES user(id)
);

# --- !Downs
drop table user;
drop table upload;