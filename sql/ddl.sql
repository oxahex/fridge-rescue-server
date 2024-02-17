use fridgerescue;

# 멤버 테이블 생성
create table member
(
    member_id   bigint unique                 not null auto_increment,
    name        varchar(15)                   not null,
    nickname    varchar(15)                   not null,
    email       varchar(50) unique            not null,
    password    varchar(100)                  not null,
    role        enum ('GUEST','USER','ADMIN') not null,
    provider    enum ('EMAIL','GOOGLE')       not null,
    provider_id varchar(100),
    email_code  varchar(10),
    jwt_token   varchar(255),
    created_at  timestamp(6)                  not null,
    modified_at timestamp(6),

    primary key (member_id)
);

select *
from member;
delete
from member
where member_id = 9;

# 냉장고 테이블 생성
create table fridge
(
    fridge_id   bigint unique not null auto_increment,
    member_id   bigint        not null,
    created_at  timestamp(6)  not null,
    modified_at timestamp(6),

    primary key (fridge_id),
    foreign key (member_id) references member (member_id)
);

# 냉장고 재료 테이블 생성
create table fridge_ingredient
(
    fridge_ingredient_id bigint unique not null auto_increment,
    fridge_id            bigint        not null,
    name                 varchar(20)   not null,
    memo                 varchar(20),
    expired_at           timestamp(6),

    PRIMARY KEY (fridge_ingredient_id)
);

# 알림 테이블 생성
create table notification
(
    notification_id       bigint unique not null auto_increment,
    member_id             bigint        not null,
    notification_type     enum ('INGREDIENT_EXPIRED','RECIPE_REVIEWED','RECIPE_RECOMMENDED'),
    notification_property json          not null,
    created_at            timestamp(6)  not null,
    checked_at            timestamp(6)  null,

    primary key (notification_id),
    foreign key (member_id) references member (member_id)
);

# 레시피 테이블 생성
CREATE TABLE recipe
(
    recipe_id        bigint unique not null auto_increment,
    member_id        bigint        not null,
    title            varchar(100)  not null,
    summary          varchar(100)  not null,
    recipe_image_url varchar(150)  not null,
    view_count       int           not null,
    review_count     int           not null,
    report_count     int           not null,
    bookmark_count   int           not null,
    created_at       timestamp(6)  not null,
    modified_at      timestamp(6),

    primary key (recipe_id),
    foreign key (member_id) references member (member_id)
);

select *
from recipe;

# 레시피 재료 테이블 생성
create table recipe_ingredient
(
    recipe_ingredient_id bigint not null auto_increment,
    recipe_id            bigint not null,
    name                 varchar(20),
    amount               varchar(20),

    primary key (recipe_ingredient_id),
    foreign key (recipe_id) references recipe (recipe_id)
);

# 레시피 과정 테이블 생성
create table recipe_step
(
    recipe_step_id   bigint not null auto_increment,
    recipe_id        bigint not null,
    step_no          int    not null,
    step_image_url   varchar(150),
    step_description varchar(100),
    step_tip         varchar(100),

    PRIMARY KEY (recipe_step_id),
    foreign key (recipe_id) references recipe (recipe_id)
);

# 레시피 후기 테이블 생성
create table review
(
    review_id        bigint unique not null auto_increment,
    member_id        bigint        not null,
    recipe_id        bigint        not null,
    cook_id          bigint        not null,
    title            varchar(50)   not null,
    review_image_url varchar(150),
    contents         varchar(1000) not null,
    created_at       timestamp(6)  not null,
    modified_at      timestamp(6),

    primary key (review_id),
    foreign key (member_id) references member (member_id),
    foreign key (recipe_id) references recipe (recipe_id)
);

# 요리 완료 테이블 생성
create table cook
(
    cook_id    bigint unique not null auto_increment,
    member_id  bigint        not null,
    recipe_id  bigint        not null,
    created_at timestamp(6)  not null,

    primary key (cook_id),
    foreign key (member_id) references member (member_id),
    foreign key (recipe_id) references recipe (recipe_id)
);

# 신고 테이블 생성
create table report
(
    report_id  bigint unique not null auto_increment,
    member_id  bigint        not null,
    recipe_id  bigint        not null,
    reason     varchar(200)  not null,
    created_at timestamp(6)  not null,

    primary key (report_id),
    foreign key (member_id) references member (member_id),
    foreign key (recipe_id) references recipe (recipe_id)
);

# 북마크 테이블 생성
create table bookmark
(
    bookmark_id bigint unique not null auto_increment,
    member_id   bigint        not null,
    recipe_id   bigint        not null,
    created_at  timestamp(6)  not null,

    primary key (bookmark_id),
    foreign key (member_id) references member (member_id),
    foreign key (recipe_id) references recipe (recipe_id)
);
