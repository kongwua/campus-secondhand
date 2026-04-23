# 校园二手物品交易系统 - 部署说明文档

## 项目概述

校园二手物品交易系统是一个面向广东工业大学学生的二手物品交易平台，支持商品发布、搜索、消息沟通、交易管理、信用评价等功能。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| ORM | MyBatis | 3.0.3 |
| 数据库 | MySQL | 8.0 |
| 身份认证 | JWT | jjwt 0.12.5 |
| 对象存储 | 华为云 OBS SDK | 3.23.9 |
| 前端框架 | Vue 3 | 3.4.x |
| 状态管理 | Pinia | 2.x |
| UI组件 | Element Plus | 2.6.x |
| 构建工具 | Vite | 5.x |
| Web服务器 | Nginx | 1.24 |

---

## 本地开发启动

### 环境要求

- JDK 21+ (Spring Boot 3.x 需要)
- Maven 3.8+
- Node.js 18+
- npm 9+
- MySQL 8.0+

### 后端启动

```bash
cd campus-secondhand-backend

# 初始化数据库
mysql -uroot -proot -e "CREATE DATABASE campus_secondhand"
mysql -uroot -proot campus_secondhand < ../sql/schema.sql
mysql -uroot -proot campus_secondhand < ../sql/seed.sql

# 编译并启动
mvn spring-boot:run
```

后端运行在 http://localhost:8080

### 前端启动

```bash
cd campus-secondhand-frontend

npm install
npm run dev
```

前端运行在 http://localhost:3000，API 自动代理到 8080

---

## 华为云部署方案

### 1. 创建云资源

| 服务 | 配置建议 | 说明 |
|------|----------|------|
| **ECS** | 鲲鹏通用增强型 2vCPU/4GB | 部署 Nginx + Spring Boot |
| **RDS** | MySQL 8.0 单机实例 20GB | 存储业务数据 |
| **OBS** | 标准存储桶 | 存储商品图片 |

### 2. ECS 配置步骤

```bash
# 1. 安装 JDK 21
sudo yum install java-21-openjdk-devel

# 2. 安装 Maven
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
tar -xzf apache-maven-3.9.6-bin.tar.gz -C /opt
export PATH=$PATH:/opt/apache-maven-3.9.6/bin

# 3. 安装 Node.js
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install nodejs

# 4. 安装 Nginx
sudo yum install nginx

# 5. 克隆项目代码
git clone <repository-url> /opt/campus-secondhand
```

### 3. RDS 数据库初始化

脚本会自动从 `huawei-cloud.yml` 配置文件读取 RDS 连接配置。

```bash
# 方式一：自动读取配置文件（推荐）
bash deploy/scripts/init-rds.sh

# 方式二：指定配置文件路径
bash deploy/scripts/init-rds.sh campus-secondhand-backend/src/main/resources/huawei-cloud.yml

# 方式三：使用环境变量覆盖配置
export DB_HOST=<RDS内网地址>
export DB_PASS=<密码>
bash deploy/scripts/init-rds.sh
```

### 4. 华为云服务配置

项目使用统一的华为云配置文件 `huawei-cloud.yml` 管理 RDS 和 OBS 服务。

配置文件位置：`campus-secondhand-backend/src/main/resources/huawei-cloud.yml`

```yaml
huawei-cloud:
  rds:
    enabled: true
    host: <RDS内网地址>
    port: 3306
    database: campus_secondhand
    username: root
    password: <RDS密码>
    charset: utf8mb4
    timezone: Asia/Shanghai
    use-ssl: false
    pool:
      max-active: 20
      max-idle: 10
      min-idle: 5
      max-wait: 30000

  obs:
    enabled: true
    access-key: <AccessKey>
    secret-key: <SecretKey>
    endpoint: https://obs.cn-south-1.myhuaweicloud.com
    region: cn-south-1
    bucket: campus-secondhand-images
    upload:
      max-size: 10485760
      allowed-types: jpg,jpeg,png,gif,webp
      path-prefix: products/
```

### 5. 启动后端

```bash
bash deploy/scripts/backend-start.sh
```

### 6. 构建前端

```bash
bash deploy/scripts/frontend-build.sh
```

### 7. 配置 Nginx

```bash
# 复制配置文件
sudo cp deploy/nginx/campus-secondhand.conf /etc/nginx/conf.d/

# 启动 Nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

---

## Docker Compose 本地联调

```bash
# 启动 MySQL + Backend + Frontend
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止
docker-compose down
```

---

## API 端点

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | POST /api/auth/register | 用户注册 |
| 认证 | POST /api/auth/login | 用户登录 |
| 用户 | GET /api/user/info | 获取用户信息 |
| 商品 | GET /api/products | 商品列表 |
| 商品 | POST /api/products | 发布商品 |
| 商品 | GET /api/products/search | 搜索商品 |
| 商品 | GET /api/products/upload-url | OBS上传凭证 |
| 交易 | POST /api/transactions | 创建交易 |
| 交易 | PUT /api/transactions/{id}/confirm | 确认交易 |
| 交易 | PUT /api/transactions/{id}/complete | 完成交易 |
| 消息 | POST /api/messages | 发送消息 |
| 消息 | GET /api/messages/conversation/{userId} | 对话记录 |
| 评价 | POST /api/reviews | 发起评价 |

---

## 验收清单

- [ ] 用户注册登录正常
- [ ] 商品发布、浏览、搜索功能完整
- [ ] 图片上传到 OBS 成功
- [ ] 消息沟通功能可用
- [ ] 交易流程完整（下单→确认→完成）
- [ ] 评价功能正常，信用分更新
- [ ] Nginx 反向代理配置正确
- [ ] JWT 认证机制正常

---

## 文件清单

| 文件 | 说明 |
|------|------|
| deploy/nginx/campus-secondhand.conf | Nginx 配置 |
| deploy/scripts/backend-start.sh | 后端启动脚本 |
| deploy/scripts/frontend-build.sh | 前端构建脚本 |
| deploy/scripts/init-rds.sh | 数据库初始化脚本 |
| docker-compose.yml | Docker Compose 配置 |
| sql/schema.sql | 建表脚本 |
| sql/seed.sql | 分类种子数据 |

---

**文档版本**: v2.0
**最后更新**: 2026-04-23

**v2.0 更新内容：**
- 新增统一华为云服务配置文件 `huawei-cloud.yml`
- RDS 和 OBS 配置集中管理