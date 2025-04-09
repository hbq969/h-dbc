一种基于数据库的配置中心实现。



## 快速开始
### 安装
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

### 部署
```bash
cd h-dbc/dbc-config/target
tar xvf dbc-config-1.0-SNAPSHOT.tar.gz
cd dbc-config-1.0-SNAPSHOT/deploy/bootstrap
sh start.sh
```





## 配置
### 配置中心
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

### 服务配置

> Maven 配置
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



## 非Java接入方式
1. 获取RSA公钥
```bash
curl -XGET 'http://<Username>:<Password>@ip:port/h-dbc/api/publicKey'
```

2. 生成8—64位的AES秘钥、IV

3. 加密请求体
> type支持PROP、YAML两种格式
```json
{
  "key": rsa(<生成的8-32位的AES秘钥>, <获取的公钥>),
  "iv": rsa(<生成的8-32位的IV>, <获取的公钥>),
  "body": aes('{"serviceName":"h-example","profileName":"dev", "type":"YAML"}',<生成的8-32位的AES秘钥>, <生成的8-32位的IV>)
}
```

4. 发送请求
```bash
curl -XPOST 'http://<Username>:<Password>@ip:port/h-dbc/api/config/list' \
-d ‘{
  "key": rsa(<生成的8-32位的AES秘钥>, <获取的公钥>),
  "iv": rsa(<生成的8-32位的IV>, <获取的公钥>),
  "body": aes('{"serviceName":"h-example","profileName":"dev", "type":"YAML"}',<生成的8-32位的AES秘钥>, <生成的8-32位的IV>)
}’ \
-H 'Content-Type:application/json'
```
5. 响应数据解密
```javascript
aes('<加密的响应数据>', <生成的8-32位的AES秘钥>, <生成的8-32位的IV>)
```

## 其他语言`SDK`示例代码参考

[sdk-python-examples](./sdk/python/dbc-sdk-python.zip)

[sdk-javascript-examples](./sdk/javascript/dbc-sdk-javascript.zip)

