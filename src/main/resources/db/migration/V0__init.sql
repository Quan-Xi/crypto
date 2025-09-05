create table admin
(
    id          bigint                                               not null comment '主键'
        primary key,
    create_time datetime                   default CURRENT_TIMESTAMP not null comment '创建时间',
    username    varchar(32)                                          not null comment '账号',
    status      enum ('enable', 'disable') default 'enable'          not null comment '禁用状态',
    phone       varchar(32)                                          null comment '手机号',
    note        varchar(32)                                          null comment '备注',
    password    varchar(255)                                         not null,
    nickname    varchar(32)                                          null comment '昵称',
    constraint username
        unique (username)
);

create table config
(
    name  varchar(100)  not null
        primary key,
    value varchar(3000) null
);

create table defibox
(
    id           int auto_increment comment '自增主键'
        primary key,
    update_time  datetime                                                            not null comment '更新时间',
    type         enum ('mining', 'dex', 'borrowLoan', 'derivative') default 'mining' not null comment '类别  ''mining'':挖矿; ''dex'':dex; ''borrowLoan'': 借贷; ''derivative'': 衍生品',
    name         varchar(125)                                                        not null comment '名字',
    mining_token varchar(64)                                                         null comment '挖矿 存入资产的类型',
    icon         varchar(512)                                                        null comment 'icon',
    website      varchar(125)                                                        not null comment '网址',
    brief        varchar(1024)                                                       null comment '简介',
    ext          varchar(1024)                                                       null comment '扩展信息',
    constraint type_name_index
        unique (type, name, mining_token)
);

create table news
(
    id       bigint                              not null comment '主键'
        primary key,
    _id      varchar(32)                         not null comment '去重id',
    datetime timestamp default CURRENT_TIMESTAMP not null comment '时间',
    type     int       default 0                 not null comment '类别 0(000): 全部里面非行情, 政策, 交易所的快讯 1(001): 行情 2(010): 政策 4(100): 交易所',
    title    varchar(255)                        null comment '标题',
    content  varchar(3000)                       null comment '内容',
    ext      varchar(512)                        null comment '扩展字段',
    constraint _id
        unique (_id)
);

