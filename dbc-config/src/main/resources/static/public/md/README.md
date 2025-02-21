一个基于数据库(mysql、oracle)的配置中心实现。

A configuration center implementation based on database (mysql, oracle).



## 快速开始 (Quick Start)
### 安装
```bash
git clone https://github.com/hbq969/h-dbc.git
# 打包UI页面
cd h-dbc/dbc-config/src/main/resources/static
nvm use 16 
npm i && npm run build
# 构建配置中心服务端
cd h-dbc 
mvn -U -DskipTests=true clean
# 构建配置中心依赖
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

> 服务依赖
```xml
<dependency>
    <groupId>com.github.hbq969</groupId>
    <artifactId>dbc-driver</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
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
          secret: api加密传输秘钥，参考配置中心配置
          auth:
            basic:
              username: api认证账号
              password: api认证密码
            enabled: true
```

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
          username: 数据库账号
          password: 数据库密码
          jdbc-url: 数据库url
```



## 非java程序接入指南

```bash
curl -XPOST 'http://user:pwd@ip:port/h-dbc/api/config/list' \
-d "3RCAL41+Rvbv/+s+jlqAw9Rjoiy.....SK5STJ7NbYGAuNdElRWA==" \
-H 'Content-Type:application/json'
```



> 请求体加密

```javascript
import CryptoJS from "crypto-js";

function encrypt(word: any, key: any, iv: any): string {
    let srcs = CryptoJS.enc.Utf8.parse(word);
    let encrypted = CryptoJS.AES.encrypt(srcs, key, {
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    });
    return CryptoJS.enc.Base64.stringify(encrypted.ciphertext);
}
let query = {"serviceName":"h-example","profileName":"default"}
let body = encrypt(JSON.stringify(query),加密密码,iv)
```



> 响应体解密

```javascript
import CryptoJS from "crypto-js";

function decrypt(word: any, key: any, iv: any): string {
    let base64 = CryptoJS.enc.Base64.parse(word);
    let src = CryptoJS.enc.Base64.stringify(base64);
    let decrypt = CryptoJS.AES.decrypt(src, key, {
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    });
    var decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
    return decryptedStr.toString();
}
```

