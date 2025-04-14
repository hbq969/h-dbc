#### **自主生成（开发/测试环境）**

通过 JDK 的 `keytool` 命令生成自签名证书：

```bash
keytool -genkeypair \
  -alias hdbc.for.host.internal \
  -keyalg RSA \
  -keysize 2048 \
  -keystore dbc.jks  \
  -validity 3650 
```

- 参数说明
  - `-alias`：证书别名（如 `mydomain`）
  - `-keystore`：指定生成的 JKS 文件路径
  - `-validity`：证书有效期（天）



#### **证书格式转换**

**支持格式**：Spring Boot 默认支持 PEM（`.crt`/`.key`）或 PKCS12（`.p12`/`.pfx`），若使用其他格式需转换

```bash
# 将 JKS 转 PKCS12（需JDK keytool）
keytool -importkeystore -srckeystore dbc.jks  -destkeystore dbc.p12 -deststoretype PKCS12 
# 合并 PEM 证书链（服务证书 + 中间证书 + 根证书）
# cat server.crt  intermediate.crt  root.crt  > fullchain.pem  
```



#### 启动服务

....



#### 导出证书

```bash
# 生成dbc.crt
openssl s_client -connect hdbc.for.host.internal:30170  -showcerts </dev/null | openssl x509 -out dbc.crt 

# 导出truststore.jks
keytool -importcert -keystore truststore.jks  -file dbc.crt  -alias hdbc-cert -storepass 123456

# 转换为p12格式
keytool -importkeystore -srckeystore truststore.jks  -destkeystore truststore.p12 -deststoretype PKCS12 
```



