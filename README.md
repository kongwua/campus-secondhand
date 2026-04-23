# 校园二手交易平台

基于 Spring Boot 3 + Vue 3 的校园二手物品交易系统，部署于华为云 ECS + RDS + OBS。

---

## 项目简介

本系统提供完整的校园二手交易功能：
- 商品发布与管理（支持图片上传）
- 商品搜索与筛选
- 用户认证（JWT）
- 实时消息沟通
- 交易管理
- 信用评价系统

---

## 目录结构

```
campus-secondhand/
│
├── campus-secondhand-backend/     # 后端 (Spring Boot 3)
│   ├── src/main/java/
│   │   └── com/campus/secondhand/
│   │       ├── controller/        # REST API
│   │       ├── service/           # 业务逻辑
│   │       ├── mapper/            # MyBatis Mapper
│   │       ├── entity/            # 数据实体
│   │       └── dto/               # 数据传输对象
│   │       └── exception/         # 异常处理
│   │       └── utils/             # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml        # 主配置
│   │   ├── huawei-cloud.yml       # 华为云配置（需自行创建）
│   │   ├── huawei-cloud-example.yml # 配置模板
│   │   └── mapper/*.xml           # MyBatis XML
│   └── pom.xml                    # Maven 配置
│
├── campus-secondhand-frontend/    # 前端 (Vue 3)
│   ├── src/
│   │   ├── views/                 # 页面组件
│   │   ├── api/                   # API 封装
│   │   ├── router/                # 路由配置
│   │   ├── store/                 # 状态管理
│   │   └── styles/                # 样式文件
│   ├── vite.config.ts             # Vite 配置
│   └── package.json               # npm 配置
│
├── sql/                           # 数据库脚本
│   ├── schema.sql                 # 表结构
│   └── seed.sql                   # 初始数据
│
├── scripts/                       # 脚本工具
│   ├── setup-env.sh               # 环境配置脚本 (macOS/Linux)
│   └── setup-env.bat              # 环境配置脚本 (Windows)
│
├── deploy/                        # 部署配置
│   └── nginx/                     # Nginx 配置
│
└── README.md                      # 说明文档
```

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | Java 框架 |
| MyBatis | 3.0.3 | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| JWT | jjwt 0.12.5 | 身份认证 |
| OBS SDK | 3.23.9 | 对象存储 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 | 3.4.x | 前端框架 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由 |
| Element Plus | 2.6.x | UI 组件库 |
| Vite | 5.x | 构建工具 |

### 云服务

| 服务 | 说明 |
|------|------|
| ECS | 云服务器 |
| RDS | MySQL 数据库 |
| OBS | 图片存储 |

---

## 快速开始

### 环境要求

| 工具 | 最低版本 |
|------|----------|
| Java JDK | 21 |
| Maven | 3.8 |
| Node.js | 18 |
| npm | 9 |
| MySQL | 8.0 |

### 第一步：克隆项目

```bash
git clone https://github.com/kongwua/campus-secondhand.git
cd campus-secondhand
```

### 第二步：配置华为云服务

**2.1 创建配置文件**

```bash
cd campus-secondhand-backend/src/main/resources
cp huawei-cloud-example.yml huawei-cloud.yml
```

**2.2 编辑配置文件**

编辑 `huawei-cloud.yml`，填入您的华为云服务信息：

```yaml
huawei-cloud:
  rds:
    host: your-rds-instance.cn-north-4.myhuaweicloud.com
    port: 3306
    database: campus
    username: your-db-username
    password: your-db-password
  
  obs:
    access-key: your-access-key
    secret-key: your-secret-key
    endpoint: obs.cn-north-4.myhuaweicloud.com
    bucket: your-bucket-name
```

**获取华为云配置信息：**
- **RDS**：华为云控制台 → 数据库服务 → RDS → 实例详情 → 连接地址
- **OBS**：华为云控制台 → 对象存储服务 → 桶列表 → 桶名
- **AK/SK**：华为云控制台 → 我的凭证 → 访问密钥

### 第三步：运行环境配置脚本

配置好 `huawei-cloud.yml` 后，执行环境配置脚本进行环境检测：

**macOS / Linux：**
```bash
cd campus-secondhand
./scripts/setup-env.sh
```

**Windows：**
```cmd
cd campus-secondhand
scripts\setup-env.bat
```

脚本将自动检测并显示：
- Java、Maven、Node.js、npm 是否已安装
- 配置文件是否存在并正确配置
- 后端 JAR 文件是否已构建
- 前端构建产物是否存在

---

## 开发运行

### 后端启动

```bash
cd campus-secondhand-backend

# 构建项目
mvn clean package -DskipTests

# 启动服务
java -jar target/campus-secondhand-backend.jar
```

后端运行于 http://localhost:8080

### 前端启动

```bash
cd campus-secondhand-frontend

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

前端开发服务器运行于 http://localhost:3000

---

## 数据库初始化

### 方式一：命令行

```bash
mysql -h<数据库地址> -u<用户名> -p<密码> -e "CREATE DATABASE campus CHARACTER SET utf8mb4"
mysql -h<数据库地址> -u<用户名> -p<密码> campus < sql/schema.sql
mysql -h<数据库地址> -u<用户名> -p<密码> campus < sql/seed.sql
```

### 方式二：华为云 RDS 控制台

1. 登录华为云 RDS 控制台
2. 使用数据管理服务 DAS
3. 执行 `sql/schema.sql` 和 `sql/seed.sql`

---

## 华为云部署

### 部署架构

```
用户浏览器
    ↓
ECS (Nginx 反向代理)
    ├── /          → 前端静态文件
    └── /api       → 后端服务 (8080)
    ↓
RDS MySQL (数据库)
OBS (图片存储)
```

### 部署步骤

**1. 创建云资源**
- ECS：2vCPU / 4GB / 50GB（推荐）
- RDS：MySQL 8.0
- OBS：创建公开读桶

**2. 配置安全组**
- 入站规则：80 (HTTP)、8080 (API)、22 (SSH)

**3. 服务器初始化**
```bash
yum install -y java-21-openjdk-devel maven nodejs nginx
```

**4. 上传代码**
```bash
scp -r campus-secondhand root@<ECS公网IP>:/opt/
```

**5. 配置并构建**
```bash
cd /opt/campus-secondhand

# 编辑 huawei-cloud.yml（填入华为云信息）
cd campus-secondhand-backend/src/main/resources
cp huawei-cloud-example.yml huawei-cloud.yml
vi huawei-cloud.yml

# 运行环境检测脚本
cd /opt/campus-secondhand
./scripts/setup-env.sh

# 构建后端
cd campus-secondhand-backend
mvn clean package -DskipTests

# 构建前端
cd ../campus-secondhand-frontend
npm install
npm run build
```

**6. 部署前端**
```bash
mkdir -p /var/www/campus-secondhand
cp -r dist/* /var/www/campus-secondhand/
```

**7. 配置 Nginx**
```bash
cp deploy/nginx/campus-secondhand.conf /etc/nginx/conf.d/
nginx -s reload
```

**8. 启动后端**
```bash
nohup java -jar campus-secondhand-backend/target/campus-secondhand-backend.jar > app.log 2>&1 &
```

---

## 功能清单

| 模块 | 功能 | 说明 |
|------|------|------|
| 认证 | 注册、登录 | JWT 令牌认证 |
| 商品 | 发布、编辑、删除、搜索 | 支持图片上传 |
| 分类 | 分类筛选 | 5 个默认分类 |
| 消息 | 实时聊天 | 卖家买家沟通 |
| 交易 | 创建、确认、完成 | 交易状态管理 |
| 评价 | 信用评分 | 交易后评价 |

---

## API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 登录 |
| `/api/auth/register` | POST | 注册 |
| `/api/products` | GET | 商品列表 |
| `/api/products` | POST | 发布商品 |
| `/api/products/{id}` | GET | 商品详情 |
| `/api/products/upload-url` | GET | 获取上传地址 |
| `/api/products/categories` | GET | 分类列表 |
| `/api/transactions` | POST | 创建交易 |
| `/api/messages` | GET | 消息列表 |
| `/api/reviews` | POST | 提交评价 |

---

## 常见问题

**Q: 图片上传失败？**
- 检查 OBS 配置是否正确
- 确认桶已配置公开读权限
- 检查 CORS 规则

**Q: 数据库连接失败？**
- 检查 RDS 安全组是否允许 ECS 访问
- 确认 `huawei-cloud.yml` 配置正确

**Q: 后端启动失败？**
- 检查 Java 版本是否为 21+
- 查看日志：`tail -f app.log`

---

## 许可证

MIT License

---

**最后更新**: 2026-04-23