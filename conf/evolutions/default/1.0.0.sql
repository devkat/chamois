# --- !Ups

create sequence "seq__user__id" increment 1 start 10;

create table "user" (
  "id" bigint default nextval("seq__user__id") not null primary key,
  "name" varchar not null,
  "age" int not null
);

# --- !Downs

drop table "people" if exists;
drop sequence "seq__user__id" if exists;