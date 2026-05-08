# TokenShop API

COCO CLAW AI API平台后端服务

## 技术栈

- Java 1.8
- Spring Boot 2.7.18
- MySQL 8.0
- Redis
- MyBatis-Plus 3.5.3
- JWT

## 项目结构

```
tokenshop-api/
├── pom.xml
├── src/main/
│   ├── java/com/cococlaw/tokenshop/
│   │   ├── TokenshopApplication.java     # 启动类
│   │   ├── config/                          # 配置类
│   │   ├── controller/                      # 控制器
│   │   ├── service/                         # 服务层
│   │   ├── mapper/                          # Mapper接口
│   │   ├── entity/                          # 实体类
│   │   ├── dto/                             # 数据传输对象
│   │   ├── common/                          # 通用类
│   │   └── utils/                           # 工具类
│   └── resources/
│       ├── application.yml                  # 应用配置
│       └── sql/init.sql                     # 数据库初始化脚本
└── README.md
```

## 快速开始

### 1. 环境要求

- JDK 1.8+
- MySQL 8.0+
- Redis

### 2. 数据库配置

1. 创建数据库:
```sql
CREATE DATABASE tokenshop DEFAULT CHARACTER SET utf8mb4;
```

2. 执行初始化脚本:
```bash
mysql -u root -p tokenshop < src/main/resources/sql/init.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tokenshop
    username: your_username
    password: your_password

  redis:
    host: localhost
    port: 6379
```

### 4. 启动服务

```bash
mvn spring-boot:run
```

服务将在 http://localhost:8080/api 启动

## API接口

### 认证接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/auth/sendCode | 发送验证码 |
| POST | /api/auth/login/phone | 手机号登录 |
| POST | /api/auth/login/email | 邮箱登录 |
| POST | /api/auth/register | 用户注册 |

### 模型接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/models | 获取模型列表 |
| GET | /api/models/{provider} | 按厂商获取模型 |
| GET | /api/models/detail/{modelId} | 获取模型详情 |

### API密钥接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/keys | 创建API密钥 |
| GET | /api/keys | 获取密钥列表 |
| DELETE | /api/keys/{id} | 删除密钥 |

### 套餐接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/packages | 获取套餐列表 |
| GET | /api/packages/{id} | 获取套餐详情 |

### 订单接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/orders | 创建订单 |
| GET | /api/orders | 获取订单列表 |
| GET | /api/orders/{orderNo} | 获取订单详情 |
| POST | /api/orders/pay | 支付订单 |

### 支付回调

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/pay/callback/wechat | 微信支付回调 |
| POST | /api/pay/callback/alipay | 支付宝回调 |

### 用户接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/user/info | 获取用户信息 |

### 统计接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/usage/stats | 获取使用统计 |
| GET | /api/usage/records | 获取消费记录 |

### 代理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/chat/completions | 聊天补全 |

## 认证方式

API请求需要在Header中携带认证信息:

```
Authorization: Bearer YOUR_API_KEY
```

## 测试账号

- 手机号: 13800138000
- 密码: 123456
- 初始余额: 5元

## 支付配置

### 微信支付

编辑 `application.yml`:

```yaml
wechatpay:
  appid: your_appid
  mchid: your_mchid
  serial_no: your_serial_no
  private_key_path: /path/to/apiclient_key.pem
  apiv3_key: your_apiv3_key
  notify_url: http://your-domain.com/api/pay/callback/wechat
```

### 支付宝

编辑 `application.yml`:

```yaml
alipay:
  appid: your_appid
  private_key: your_private_key
  alipay_public_key: alipay_public_key
  notify_url: http://your-domain.com/api/pay/callback/alipay
```

## 开发说明

### 代码规范

- 使用MyBatis-Plus简化CRUD操作
- 使用Redis缓存热点数据
- 统一的响应格式
- 完善的异常处理

### 数据库表

- `user` - 用户表
- `api_key` - API密钥表
- `model` - 模型表
- `package` - 套餐表
- `orders` - 订单表
- `transaction` - 交易记录表
- `usage_log` - 使用记录表

## License

Copyright © 2024 COCO CLAW. All rights reserved.
