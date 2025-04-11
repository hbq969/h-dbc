A configuration center implementation based on a database.

## Quick Start
### Install
```bash
git clone https://github.com/hbq969/h-dbc.git
# Packaging UI pages
cd h-dbc/dbc-config/src/main/resources/static
nvm use 16
npm i && npm run build
#Build the configuration center server
cd h-dbc
mvn -U -DskipTests=true clean
# Build configuration center dependencies
cd h-dbc/dbc-driver
mvn install
```

### Deployment
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

### Service configuration

> Maven configuration
```xml
<dependency>
    <groupId>com.github.hbq969</groupId>
    <artifactId>spring-cloud-starter-hdbc-config</artifactId>
    <version>1.1</version>
</dependency>
```

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
            username: <Username>
            password: <Password>
          charset: utf-8
          url: http://localhost:30170/h-dbc
          api-log: true
```

## Non-Java access method
1. Construct request parameters
> Here, `type` supports `PROP` (property key-value pair) and `YAML` formats
```json
{
    "serviceName": "h-example",
    "profileName": "dev",
    "type": "PROP"
}
```

2. Encrypt request parameters

![](./request_encrypt.png)

3. Call the configuration center API interface to pull configuration

## Non-Java Program Access Guide

1. Get RSA public key
```bash
curl -XGET 'http://<Username>:<Password>@ip:port/h-dbc/api/publicKey'
```

2. Generate 8-32 bit AES key and IV

3. Encrypted request body
> type supports PROP and YAML formats
```json
{
  "key": rsa(<Generated 8-32 bit AES key>, <Obtained public key>),
  "iv": rsa(<Generated 8-32 bit IV>, <Obtained public key>),
  "body": aes('{"serviceName":"h-example","profileName":"dev", "type":"YAML"}',<Generated 8-32 bit AES key>, <Generated 8-32 bit IV>)
}
```

4. Send Request
```bash
curl -XPOST 'http://<Username>:<Password>@ip:port/h-dbc/api/config/list' \
-d ‘{
  "key": rsa(<Generated 8-32 bit AES key>, <Obtained public key>),
  "iv": rsa(<Generated 8-32 bit IV>, <Obtained public key>),
  "body": aes('{"serviceName":"h-example","profileName":"dev", "type":"YAML"}',<Generated 8-32 bit AES key>, <Generated 8-32 bit IV>)
}’ \
-H 'Content-Type:application/json'
```
5. Response data decryption
```javascript
aes('<Encrypted response data>', <Generated 8-32 bit AES key>, <Generated 8-32 bit IV>)
```

## Reference for other language access examples

1. [sdk-python-examples](./sdk/python/dbc-sdk-python.zip)
2. [sdk-javascript-examples](./sdk/javascript/dbc-sdk-javascript.zip)
3. [sdk-golang-examples](./sdk/golang/dbc-sdk-golang.zip)
4. [sdk-C++-examples](./sdk/C++/dbc-sdk-C++.zip)