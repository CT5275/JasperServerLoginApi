# JasperServerLoginApi
Jasper Server Login with Encryption

Jasper Report Get Encryption Key: <jasper server context>/GetEncryptionKey
Jasper Report API: rest_v2/login

To be able to use /login service, you have to pass cookie from /GetEncryptionKey
Get the encryption key and perform encryption of the original plain text password, and then pass to /login service
  
Reference: https://community.jaspersoft.com/documentation/tibco-jasperreports-server-rest-api-reference/v790/authentication
