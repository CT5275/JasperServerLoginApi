# JasperServerLoginApi
Jasper Server Login with Encryption

Jasper Report Get Encryption Key: <jasper server context>/GetEncryptionKey
Jasper Report API: rest_v2/Login

To be able to use /login service, you have to pass cookie from /GetEncryptionKey
Get the encryption key and perform encryption of the original plain text password, and then pass to /login service
