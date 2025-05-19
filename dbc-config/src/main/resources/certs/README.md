## Eureka支持https
### 1. 创建服务端证书
```bash
keytool -genkeypair -alias hdbc_server -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore hdbc_server.p12 -validity 18250 \
-ext "SAN=dns:localhost,dns:hdbc.for.host.internal"
```

```text
您的名字与姓氏是什么?
  [Unknown]:  localhost
您的组织单位名称是什么?
  [Unknown]:  My Company
您的组织名称是什么?
  [Unknown]:  HBQ    
您所在的城市或区域名称是什么?
  [Unknown]:  Nanjing
您所在的省/市/自治区名称是什么?
  [Unknown]:  JS
该单位的双字母国家/地区代码是什么?
  [Unknown]:  CN
CN=localhost, OU=My Company, O=SMP, L=Suzhou, ST=JS, C=CN是否正确?
  [否]:  Y
```

### 2. 创建客户端证书
```bash
keytool -genkeypair -alias hdbc_client -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore hdbc_client.p12 -validity 18250 \
-ext "SAN=dns:localhost,dns:hdbc-config.for.host.internal"
```

### 3. 导出服务端、客户端证书
```bash
keytool -export -alias hdbc_server -file hdbc_server.crt --keystore hdbc_server.p12
keytool -export -alias hdbc_client -file hdbc_client.crt --keystore hdbc_client.p12
```

### 4. 信任证书
```bash
# 将 server.crt 文件导入 client.p12 中，使 client端 信任 server 的证书，输入密码client
keytool -import -alias hdbc_server -file hdbc_server.crt -keystore hdbc_client.p12

# 将 client.crt 文件导入 server.p12 中，使 server端 信任client 的证书，输入密码server
keytool -import -alias hdbc_client -file hdbc_client.crt -keystore hdbc_server.p12
```