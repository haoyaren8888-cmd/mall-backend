# 太原理工大学校园闲置商城后端

Spring Boot + Spring MVC + MyBatis-Plus + MySQL。

## 启动步骤

1. 从仓库根目录导入初始化脚本：

```bash
mysql -uroot -p123456 < mall.sql
```

也可以直接导入主脚本：

```bash
mysql -uroot -p123456 < src/main/resources/db/mall_init.sql
```

如果是从旧库升级到校园闲置商城字段，先备份数据，再执行：

```bash
mysql -uroot -p123456 mall < src/main/resources/db/tyut_flea_market_patch.sql
```

2. 数据库默认连接为 `root / 123456`。如果本机密码不同，可以设置环境变量。

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

## 默认账号

- 管理员：`admin / 123456`
- 学生用户：`user / 123456`

登录使用 Session/Cookie，前端请求需要开启 `withCredentials`。
