A configuration center implementation based on database.



## Quick Start
### Install
```bash
git clone https://github.com/hbq969/h-dbc.git
# Packaging UI pages
cd h-dbc/dbc-config/src/main/resources/static
nvm use 16 
npm i && npm run build
# Build the configuration center server
cd h-dbc 
mvn -U -DskipTests=true clean
# Build configuration center dependencies
cd h-dbc/dbc-driver
mvn install
```

### Deploy
```bash
cd h-dbc/dbc-config/target
tar xvf dbc-config-1.0-SNAPSHOT.tar.gz
cd dbc-config-1.0-SNAPSHOT/deploy/bootstrap
sh start.sh
```





## Configuration
### Configuration Center
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
      jdbc-url: Database url
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: Database username
      password: Database password
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

### Service Configuration

> Maven Configuration
```xml
<dependency>
    <groupId>com.github.hbq969</groupId>
    <artifactId>dbc-driver</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

> bootstrap.yml （api pull way）
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
          secret: API encryption transmission key, refer to the configuration center configuration
          auth:
            basic:
              username: API authentication account
              password: API authentication password
            enabled: true
```

> bootstrap.yml （database read way）
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
          username: Database Account
          password: Database password
          jdbc-url: Database url
```



## Non-Java Program Access Guide

```bash
curl -XPOST 'http://user:pwd@ip:port/h-dbc/api/config/list' \
-d "3RCAL41+Rvbv/+s+jlqAw9Rjoiy.....SK5STJ7NbYGAuNdElRWA==" \
-H 'Content-Type:application/json'
```

![](./example.png)


> Request body encryption

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
let body = encrypt(JSON.stringify(query),key,iv)
```



> Response body decryption

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

