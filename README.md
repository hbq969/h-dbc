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
```

### 服务配置
> bootstrap.yml （db方式拉取配置）
```yaml
spring:
  cloud:
    config:
      h-dbc:
        enabled: true
        dbc-key: h-dbc
        service-name: h-example
        profile-name: dev
        strategy: db
        db:
          driver-class-name: com.mysql.cj.jdbc.Driver
          username: dbc
          password: ENC(O05LzWQnZrj5z+zWl6WJQngl4pWrd5nQddgnPH51WAQeVxBEoLcVGMrocH3xdhc4)
          jdbc-url: jdbc:mysql://docker.for.mac.host.internal:3306/dbc?useUnicode=true&allowPublicKeyRetrieval=true&characterEncoding=utf-8&useSSL=false&autoReconnect=true&failOverReadOnly=false&maxReconnects=15000
# 如果上面属性不用加密方式，则下面配置不需要
# 否则需要添加下面解密密码，密码是敏感信息尽量可通过命令行传入
#jasypt:
#  encryptor:
#    password: 密码

```


> bootstrap.yml （api方式拉取配置）
```yaml
spring:
  cloud:
    config:
      h-dbc:
        enabled: true
        dbc-key: h-dbc
        service-name: h-example
        profile-name: default
        strategy: api
        api:
          secret: ENC(tHRgyoB.....c2b+eBw==)
# 如果上面属性不用加密方式，则下面配置不需要
# 否则需要添加下面解密密码，密码是敏感信息尽量可通过命令行传入
#jasypt:
#  encryptor:
#    password: 密码
```

> 服务依赖
```xml
<dependency>
    <groupId>com.github.hbq969</groupId>
    <artifactId>dbc-driver</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## 功能演示 (Features)

![](dbc-config/src/main/resources/static/src/assets/img/1.png)


![](dbc-config/src/main/resources/static/src/assets/img/2.png)


![](dbc-config/src/main/resources/static/src/assets/img/3.png)


![](dbc-config/src/main/resources/static/src/assets/img/4.png)


![](dbc-config/src/main/resources/static/src/assets/img/5.png)


![](dbc-config/src/main/resources/static/src/assets/img/6.png)



## 问题联系 (Contact)

[hbq969@gmail.com](mailto:hbq969@gmail.com)
