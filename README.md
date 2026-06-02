# 综合网上购物商城后端

Spring Boot + Spring MVC + MyBatis-Plus + MySQL。

## 启动步骤

1. 创建数据库并导入测试数据：

```bash
mysql -uroot -p < mall.sql
```

2. 数据库默认连接为 `root / 123456`。如果本机密码不同，可以直接设置环境变量。

PowerShell：

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="你的密码"
```

CMD：

```bash
set MYSQL_USERNAME=root
set MYSQL_PASSWORD=你的密码
```

3. 启动后端：

```bash
mvn spring-boot:run
```

默认端口：`8080`。

## 测试账号

- 管理员：`admin / 123456`
- 普通用户：`user / 123456`

登录使用 Session/Cookie，前端请求需要开启 `withCredentials`。
