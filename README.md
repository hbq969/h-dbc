一个基于数据库(mysql、oracle)的配置中心实现。

A configuration center implementation based on database (mysql, oracle).



## 快速开始 (Quick Start)
### 安装
```bash
git clone https://github.com/hbq969/h-dbc.git
cd h-dbc/dbc-config/src/main/resources/static
nvm use 16 
npm i && npm run build
cd h-dbc 
mvn -U -DskipTests=true clean
cd h-dbc/dbc-driver
mvn install
```

### 部署
```bash
cd h-dbc/dbc-config/target
tar xvf dbc-config-1.0-SNAPSHOT.tar.gz
cd dbc-config-1.0-SNAPSHOT/deploy/bootstrap
sh start.sh
```





## 配置 (Config)
### 配置中心配置
> application-mysql.yml
```yaml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    dynamic:
      enabled: true
      base-packages: com.github.hbq969
      default-lookup-key: hikari
    hikari:
      jdbc-url: 数据库url
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: 账号
      password: 密码
      maximum-pool-size: 10
      minimum-idle: 2
      max-lifetime: 1800000
      connection-test-query: SELECT 1
  mvc:
    interceptors:
      login:
        enabled: true
        dialect: mysql

mybatis:
  mapper-locations:
    - classpath*:mappers/*.xml
    - classpath*:**/mapper/common/*Mapper.xml
    - classpath*:**/mapper/mysql/*Mapper.xml
  config-location: classpath:jpaConfig-mysql.xml

# 开启https（可选，默认不开启为http）
server:
  ssl:
    enabled: true
    key-store: classpath:certs/dbc.p12
    key-store-password: 证书密码
    key-store-type: PKCS12
    key-alias: hdbc.for.host.internal
    enabled-protocols: TLSv1.2,TLSv1.3
    ciphers: TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256
```

### 服务配置
> bootstrap.yml 
```yaml
spring:
  cloud:
    config:
      h-dbc:
        enabled: true
        dbc-key: h-dbc
        service-name: h-example
        profile-name: dev
        api:
          basic-auth:
            username: 账号
            password: 密码
          charset: utf-8
          url: http://localhost:30170/h-dbc
          api-log: true
          # 启用https（可选）
          https: true
          truststore-password: 证书密码
```

> 服务依赖
```xml
<dependency>
    <groupId>com.github.hbq969</groupId>
    <artifactId>spring-cloud-starter-hdbc-config</artifactId>
    <version>latest</version>
</dependency>
```

## 功能演示 (Features)

### 登录页面

![](dbc-config/src/main/resources/static/src/assets/img/0.png)


### 引导文档
![](dbc-config/src/main/resources/static/src/assets/img/1.png)


### 环境配置
![](dbc-config/src/main/resources/static/src/assets/img/2.png)


### 服务管理
![](dbc-config/src/main/resources/static/src/assets/img/3.png)


### 服务配置
![](dbc-config/src/main/resources/static/src/assets/img/4.png)


### 导入配置
![](dbc-config/src/main/resources/static/src/assets/img/5.png)


### 编辑配置(YAML)
![](dbc-config/src/main/resources/static/src/assets/img/6.png)


### 编辑配置(Properties)
![](dbc-config/src/main/resources/static/src/assets/img/7.png)


### 配置批量管理
![](dbc-config/src/main/resources/static/src/assets/img/8.png)


### 配置还原
![](dbc-config/src/main/resources/static/src/assets/img/9.png)


### 配置比较(不同环境)
![](dbc-config/src/main/resources/static/src/assets/img/10.png)


### 国际化(English)
![](dbc-config/src/main/resources/static/src/assets/img/11.png)


### 国际化(日语)
![](dbc-config/src/main/resources/static/src/assets/img/12.png)

## 问题联系 (Contact)

[hbq969@gmail.com](mailto:hbq969@gmail.com)
