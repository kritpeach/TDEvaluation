create table "USER"
(
  "ID"         bigserial not null
    constraint "USER_pkey"
    primary key,
  "USERNAME"   varchar   not null,
  "PASSWORD"   varchar   not null,
  "IS_MANAGER" boolean   not null
);

create unique index idx_username
  on "USER" ("USERNAME");

create table "Evaluation"
(
  id        bigserial not null
    constraint "Evaluation_pkey"
    primary key,
  title     varchar   not null,
  enabled   boolean   not null,
  create_at timestamp not null,
  creator   bigint    not null
    constraint "CREATOR_FK"
    references "USER"
);

create unique index index_title
  on "Evaluation" (title);

create table "Question"
(
  id             bigserial not null
    constraint "Question_pkey"
    primary key,
  content        varchar   not null,
  question_type  varchar   not null,
  "evaluationId" bigint    not null
    constraint "EVALUATION_FK"
    references "Evaluation"
    on delete cascade,
  seq            integer default 0
);

create table "Response"
(
  id         bigserial not null
    constraint "Response_pkey"
    primary key,
  answer     varchar   not null,
  "createAt" timestamp not null,
  creator    bigint    not null
    constraint "CREATOR_FK"
    references "USER"
    on delete cascade,
  question   bigint    not null
    constraint "QUESTION_FK"
    references "Question"
    on delete cascade
);

create unique index idx
  on "Response" (creator, question);

create table "Comment"
(
  "ID"        bigserial not null
    constraint "Comment_pkey"
    primary key,
  comment     varchar   not null,
  user_id     bigint    not null
    constraint "USER_FK"
    references "USER"
    on delete cascade,
  response_id bigint    not null
    constraint "RESPONSE_FK"
    references "Response"
    on delete cascade
);

create unique index "commentUserResponseIndex"
  on "Comment" (user_id, response_id);


