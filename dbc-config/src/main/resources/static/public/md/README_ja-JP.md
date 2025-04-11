データベースに基づく構成センターの実装。



## クイックスタート
### インストール
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

### 展開する
```bash
cd h-dbc/dbc-config/target
tar xvf dbc-config-1.0-SNAPSHOT.tar.gz
cd dbc-config-1.0-SNAPSHOT/deploy/bootstrap
sh start.sh
```





## 構成
### 構成センター
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

### サービス構成

> Maven 構成
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



## 非 Java プログラム アクセス ガイド

1. RSA公開鍵を取得する
```bash
curl -XGET 'http://<Username>:<Password>@ip:port/h-dbc/api/publicKey'
```

2. 8-32ビットのAESキーとIVを生成する

3. 暗号化されたリクエスト本文
```json
{
  "key": rsa(<生成された8-32ビットのAESキー>, <取得した公開鍵>),
  "iv": rsa(<8-32ビットIVを生成する>, <取得した公開鍵>),
  "body": aes('{"serviceName":"h-example","profileName":"dev", "type":"YAML"}',<生成された8-32ビットのAESキー>, <8-32ビットIVを生成する>)
}
```

4. リクエストを送信
> typeはPROPとYAML形式をサポートします
```bash
curl -XPOST 'http://<Username>:<Password>@ip:port/h-dbc/api/config/list' \
-d ‘{
  "key": rsa(<生成された8-32ビットのAESキー>, <取得した公開鍵>),
  "iv": rsa(<8-32ビットIVを生成する>, <取得した公開鍵>),
  "body": aes('{"serviceName":"h-example","profileName":"dev", "type":"YAML"}',<生成された8-32ビットのAESキー>, <8-32ビットIVを生成する>)
}’ \
-H 'Content-Type:application/json'
```
5. 他の言語アクセス例の参考
```javascript
aes('<暗号化された応答データ>', <生成された8-32ビットのAESキー>, <8-32ビットIVを生成する>)
```


## 他の言語の `SDK` サンプルコードリファレンス

1. [sdk-python-examples](./sdk/python/dbc-sdk-python.zip)
2. [sdk-javascript-examples](./sdk/javascript/dbc-sdk-javascript.zip)
3. [sdk-golang-examples](./sdk/golang/dbc-sdk-golang.zip)
4. [sdk-C++-examples](./sdk/C++/dbc-sdk-C++.zip)