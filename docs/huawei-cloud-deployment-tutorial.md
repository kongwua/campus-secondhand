# 华为云配置与项目编译教程

**项目名称**: 校园二手物品交易系统  
**教程版本**: v2.0  
**更新日期**: 2026-04-23  

---

## 目录

1. [华为云资源配置](#1-华为云资源配置)
   - 1.1 ECS 弹性云服务器配置
   - 1.2 RDS 关系型数据库配置
   - 1.3 OBS 对象存储配置
2. [本地开发环境搭建](#2-本地开发环境搭建)
3. [项目编译教程](#3-项目编译教程)
   - 3.1 后端编译
   - 3.2 前端编译
   - 3.3 Docker Compose 本地联调
4. [华为云部署流程](#4-华为云部署流程)

---

## 1. 华为云资源配置

### 1.1 ECS 弹性云服务器配置

#### 步骤 1：登录华为云控制台

1. 访问 https://console.huaweicloud.com
2. 使用华为账号登录
3. 选择区域（推荐：华南-广州 cn-south-1 或华北-北京四 cn-north-4）

#### 步骤 2：创建 ECS 实例

1. 进入 **弹性云服务器 ECS** 页面
2. 点击 **购买弹性云服务器**
3. 基础配置：

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| 区域 | 华南-广州 | 选择离学校最近的区域 |
| 计费模式 | 按需计费 | 灵活付费，课程实验建议 |
| 规格 | 鲁鹏通用计算增强型 | 2vCPU/4GB 足够运行项目 |
| 镜像 | CentOS 7.9 或 Ubuntu 20.04 | 推荐 CentOS |
| 系统盘 | 高IO 40GB | 默认配置足够 |

4. 网络配置：

| 配置项 | 推荐值 |
|--------|--------|
| 网络 | 选择默认 VPC 或新建 |
| 安全组 | 勾选：允许 SSH(22)、HTTP(80)、自定义(8080) |
| 公网带宽 | 按带宽计费，5Mbps |

5. 点击 **立即购买**，确认订单后等待实例创建（约 5-10 分钟）

#### 步骤 3：配置安全组端口

**重要**：必须开放以下端口才能正常访问系统。

```
进入 ECS 实例详情 → 安全组 → 配置规则 → 入方向规则
```

添加以下规则：

| 端口 | 协议 | 源地址 | 说明 |
|------|------|--------|------|
| 22 | TCP | 0.0.0.0/0 | SSH 远程登录 |
| 80 | TCP | 0.0.0.0/0 | HTTP 网站访问 |
| 8080 | TCP | 0.0.0.0/0 | Spring Boot API（调试用） |

#### 步骤 4：登录 ECS 服务器

**方式一：华为云控制台远程登录**
1. ECS 实例列表 → 点击实例名称
2. 点击 **远程登录** → 选择 CloudShell
3. 输入用户名 `root` 和创建时设置的密码

**方式二：本地 SSH 登录**
```bash
# 替换 <ECS公网IP> 为实际 IP 地址
ssh root@<ECS公网IP>

# 输入密码登录
```

#### 步骤 5：安装基础软件

登录 ECS 后执行以下命令：

```bash
# ========== CentOS 7.9 ==========

# 1. 更新系统
yum update -y

# 2. 安装 JDK 21
yum install java-21-openjdk-devel -y
java -version  # 验证安装

# 3. 安装 Maven
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
tar -xzf apache-maven-3.9.6-bin.tar.gz -C /opt
echo 'export PATH=$PATH:/opt/apache-maven-3.9.6/bin' >> ~/.bashrc
source ~/.bashrc
mvn -version  # 验证安装

# 4. 安装 Node.js
curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
yum install nodejs -y
node -v  # 验证安装
npm -v

# 5. 安装 Nginx
yum install nginx -y
nginx -v  # 验证安装

# 6. 安装 MySQL Client（用于连接 RDS）
yum install mysql -y

# 7. 安装 Git
yum install git -y
git --version

# 8. 创建项目目录
mkdir -p /opt/campus-secondhand
mkdir -p /var/log/campus-secondhand
mkdir -p /var/www/campus-secondhand/frontend
```

**Ubuntu 20.04 用户使用以下命令：**
```bash
# Ubuntu 版本安装命令
apt update && apt upgrade -y

# JDK 21
apt install openjdk-21-jdk -y

# Maven
apt install maven -y

# Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt install nodejs -y

# Nginx
apt install nginx -y

# MySQL Client
apt install mysql-client -y

# Git
apt install git -y

# 创建目录（同上）
mkdir -p /opt/campus-secondhand
mkdir -p /var/log/campus-secondhand
mkdir -p /var/www/campus-secondhand/frontend
```

---

### 1.2 RDS 关系型数据库配置

#### 步骤 1：创建 RDS MySQL 实例

1. 进入 **关系型数据库 RDS** 页面
2. 点击 **购买数据库实例**
3. 基础配置：

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| 区域 | 与 ECS 同区域 | 确保内网互通 |
| 实例类型 | 单机 | 成本最低 |
| 数据库引擎 | MySQL | 8.0 版本 |
| 实例规格 | 通用型 2vCPU/4GB | 与 ECS 配置匹配 |
| 存储空间 | 20GB SSD | 足够存储业务数据 |

4. 网络配置：

| 配置项 | 推荐值 |
|--------|--------|
| VPC | 选择与 ECS 相同的 VPC |
| 子网 | 选择与 ECS 相同的子网 |
| 安全组 | 默认安全组（后续需配置） |

5. 设置数据库参数：

| 参数 | 值 |
|------|------|
| 管理员账户名 | root |
| 管理员密码 | 自定义强密码（记住此密码！） |
| 时区 | Asia/Shanghai |

6. 点击 **立即购买**，等待实例创建（约 10-15 分钟）

#### 步骤 2：配置 RDS 安全组

```
RDS 实例详情 → 安全组 → 配置规则 → 入方向规则
```

添加规则：

| 端口 | 协议 | 源地址 | 说明 |
|------|------|--------|------|
| 3306 | TCP | ECS 内网 IP | 仅允许 ECS 内网访问（安全） |

**获取 ECS 内网 IP：**
- ECS 实例详情页查看 "私有 IP 地址"
- 或在 ECS 上执行 `hostname -I`

#### 步骤 3：获取 RDS 连接信息

创建完成后，记录以下信息：

```
RDS 实例详情页 → 连接信息

数据库连接地址（内网）: <RDS内网IP> 或 <RDS域名>
端口: 3306
用户名: root
密码: 创建时设置的密码
```

**示例连接地址：**
```
jdbc:mysql://rm-xxxxxxx.mysql.cn-south-1.myhuaweicloud.com:3306/campus_secondhand
```

#### 步骤 4：在 ECS 上初始化数据库

脚本会自动从 `huawei-cloud.yml` 配置文件读取 RDS 配置。

登录 ECS，执行以下命令：

```bash
cd /opt/campus-secondhand

# 方式一：自动读取配置文件初始化（推荐）
bash deploy/scripts/init-rds.sh

# 方式二：指定配置文件路径
bash deploy/scripts/init-rds.sh campus-secondhand-backend/src/main/resources/huawei-cloud.yml

# 方式三：使用环境变量覆盖配置
export DB_HOST=<RDS内网IP>  # 覆盖配置文件中的 host
bash deploy/scripts/init-rds.sh
```

脚本执行时会显示：
```
==============================================
Database Initialization
==============================================
Config file: campus-secondhand-backend/src/main/resources/huawei-cloud.yml
Host: rm-xxxx.mysql.cn-south-1.myhuaweicloud.com
Port: 3306
Database: campus_secondhand
User: root
Charset: utf8mb4
==============================================
```

**手动初始化方式（备用）：**
```bash
# 从配置文件读取连接信息后手动执行
mysql -h<RDS_HOST> -P3306 -uroot -p<RDS_PASSWORD> \
  -e "CREATE DATABASE IF NOT EXISTS campus_secondhand DEFAULT CHARACTER SET utf8mb4"

mysql -h<RDS_HOST> -P3306 -uroot -p<RDS_PASSWORD> campus_secondhand < sql/schema.sql
mysql -h<RDS_HOST> -P3306 -uroot -p<RDS_PASSWORD> campus_secondhand < sql/seed.sql
```

预期输出：
```
+-------------------------+
| Tables_in_campus_second |
+-------------------------+
| category                |
| message                 |
| product                 |
| review                  |
| transaction             |
| user                    |
+-------------------------+
```

---

### 1.3 OBS 对象存储配置

#### 步骤 1：创建 OBS Bucket

1. 进入 **对象存储服务 OBS** 页面
2. 点击 **创建桶**
3. 配置：

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| 区域 | 与 ECS 同区域 | 降低延迟 |
| 桶名称 | campus-secondhand-images | 自定义唯一名称 |
| 存储类别 | 标准 | 默认配置 |
| 桶策略 | 私有 | 安全性最高 |
| 多AZ存储 | 关闭 | 成本优化 |

4. 点击 **立即创建**

#### 步骤 2：获取 AK/SK 密钥

OBS 需要访问密钥（AK/SK）才能使用。

1. 进入 **我的凭证** 页面（右上角用户名 → 我的凭证）
2. 点击 **访问密钥** → **新增访问密钥**
3. 输入描述（如 "campus-secondhand"）
4. 点击 **确定**，下载密钥文件 `credentials.csv`

**密钥文件内容：**
```
Access Key ID: <AK>
Secret Access Key: <SK>
```

**重要提示：** AK/SK 是敏感信息，不要泄露给他人！

#### 步骤 3：配置 Bucket CORS（跨域访问）

前端直传 OBS 需要配置 CORS 规则。

```
OBS 控制台 → 桶详情 → 权限管理 → 跨域规则
```

添加规则：

| 配置项 | 值 |
|--------|--------|
| 允许的来源 | * |
| 允许的方法 | GET, PUT, POST, DELETE, HEAD |
| 允许的头域 | * |
| 暴露的头域 | ETag, x-obs-request-id |
| 缓存时间 | 3600 |

#### 步骤 4：记录 OBS 配置信息

```
OBS 桶详情页记录：

Endpoint: https://obs.cn-south-1.myhuaweicloud.com
Bucket: campus-secondhand-images
AK: <Access Key ID>
SK: <Secret Access Key>
```

---

## 2. 本地开发环境搭建

### 2.1 环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | 21+ | Spring Boot 3.x 需要 |
| Maven | 3.8+ | 项目构建工具 |
| Node.js | 18+ | 前端开发环境 |
| npm | 9+ | Node 包管理器 |
| MySQL | 8.0+ | 本地数据库（可选） |

### 2.2 安装 JDK 21

**macOS:**
```bash
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
java -version
```

**Windows:**
1. 下载 JDK 21: https://jdk.java.net/21/
2. 解压到 `C:\Java\jdk-21`
3. 配置环境变量：
   - `JAVA_HOME = C:\Java\jdk-21`
   - `Path` 添加 `%JAVA_HOME%\bin`
4. cmd 执行 `java -version`

**Linux:**
```bash
# Ubuntu
sudo apt install openjdk-21-jdk

# CentOS
sudo yum install java-21-openjdk-devel

java -version
```

### 2.3 安装 Maven

**macOS:**
```bash
brew install maven
mvn -version
```

**Windows:**
1. 下载 Maven: https://maven.apache.org/download.cgi
2. 解压到 `C:\Maven`
3. 配置环境变量：
   - `MAVEN_HOME = C:\Maven`
   - `Path` 添加 `%MAVEN_HOME%\bin`
4. cmd 执行 `mvn -version`

**Linux:**
```bash
# Ubuntu
sudo apt install maven

# CentOS
sudo yum install maven

mvn -version
```

### 2.4 安装 Node.js

**推荐使用 nvm 管理 Node 版本：**

```bash
# macOS/Linux
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.zshrc  # 或 ~/.bashrc
nvm install 18
nvm use 18
node -v
npm -v
```

**Windows:**
1. 下载 Node.js: https://nodejs.org/
2. 安装 LTS 版本（18.x）
3. cmd 执行 `node -v`

### 2.5 安装 MySQL（本地开发用）

**macOS:**
```bash
brew install mysql
brew services start mysql
mysql -uroot
```

**Windows:**
- 下载 MySQL: https://dev.mysql.com/downloads/mysql/
- 安装并设置 root 密码

**Docker 方式（推荐）：**
```bash
docker run -d --name mysql-local \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=campus_secondhand \
  -p 3306:3306 \
  mysql:8.0

# 初始化数据库
docker exec -i mysql-local mysql -uroot -proot campus_secondhand < sql/schema.sql
docker exec -i mysql-local mysql -uroot -proot campus_secondhand < sql/seed.sql
```

---

## 3. 项目编译教程

### 3.1 后端编译

#### 步骤 1：克隆项目代码

```bash
# 本地开发
cd ~/Documents
git clone <项目仓库地址> campus-secondhand
cd campus-secondhand/campus-secondhand-backend

# 或直接使用现有目录
cd /Users/a0000/Documents/云计算实验/2 课程大作业/project/campus-secondhand-backend
```

#### 步骤 2：配置华为云服务配置文件

项目使用统一的华为云配置文件 `huawei-cloud.yml` 来管理 RDS 和 OBS 服务配置。

**配置文件位置：**
- `src/main/resources/application.yml` - 基础配置（端口、JWT、MyBatis）
- `src/main/resources/huawei-cloud.yml` - 华为云服务配置（RDS + OBS）

**本地开发配置：**

编辑 `src/main/resources/huawei-cloud.yml`：

```yaml
huawei-cloud:
  rds:
    enabled: true
    host: localhost
    port: 3306
    database: campus_secondhand
    username: root
    password: root  # 修改为你的 MySQL 密码
    charset: utf8mb4
    timezone: Asia/Shanghai
    use-ssl: false
    pool:
      max-active: 20
      max-idle: 10
      min-idle: 5
      max-wait: 30000

  obs:
    enabled: false  # 本地开发禁用 OBS
    access-key: your-ak
    secret-key: your-sk
    endpoint: https://obs.cn-south-1.myhuaweicloud.com
    region: cn-south-1
    bucket: your-bucket-name
    upload:
      max-size: 10485760  # 10MB
      allowed-types: jpg,jpeg,png,gif,webp
      path-prefix: products/
```

**配置文件说明：**

| 配置项 | 说明 |
|--------|------|
| `huawei-cloud.rds.host` | RDS 数据库地址 |
| `huawei-cloud.rds.port` | RDS 端口 |
| `huawei-cloud.rds.database` | 数据库名称 |
| `huawei-cloud.rds.username` | 数据库用户名 |
| `huawei-cloud.rds.password` | 数据库密码 |
| `huawei-cloud.obs.enabled` | 是否启用 OBS |
| `huawei-cloud.obs.access-key` | OBS 访问密钥 AK |
| `huawei-cloud.obs.secret-key` | OBS 访问密钥 SK |
| `huawei-cloud.obs.bucket` | OBS 存储桶名称 |

#### 步骤 3：初始化本地数据库

```bash
# 创建数据库
mysql -uroot -proot -e "CREATE DATABASE campus_secondhand DEFAULT CHARACTER SET utf8mb4"

# 导入表结构
mysql -uroot -proot campus_secondhand < ../sql/schema.sql

# 导入种子数据
mysql -uroot -proot campus_secondhand < ../sql/seed.sql

# 验证
mysql -uroot -proot campus_secondhand -e "SHOW TABLES"
```

#### 步骤 4：编译后端项目

```bash
cd campus-secondhand-backend

# 清理并编译（跳过测试）
mvn clean package -DskipTests

# 查看编译结果
ls -la target/
```

预期输出：
```
target/
├── campus-secondhand-backend.jar  # 可执行 JAR 包
├── classes/
└── ...
```

#### 步骤 5：运行后端服务

```bash
# 方式一：Maven 启动（开发调试）
mvn spring-boot:run

# 方式二：JAR 包启动（生产部署）
java -jar target/campus-secondhand-backend.jar

# 方式三：指定配置文件启动
java -jar target/campus-secondhand-backend.jar --spring.profiles.active=prod
```

启动成功标志：
```
Started SecondhandApplication in X seconds
```

#### 步骤 6：测试 API

```bash
# 测试健康检查（如果有配置）
curl http://localhost:8080/api/categories

# 测试注册接口
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test123456","nickname":"测试用户"}'

# 测试登录接口
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test123456"}'
```

---

### 3.2 前端编译

#### 步骤 1：安装依赖

```bash
cd campus-secondhand-frontend

# 安装 npm 依赖
npm install

# 查看依赖列表
npm list
```

#### 步骤 2：配置 API 地址

**开发环境配置（vite.config.ts）：**

```ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 后端地址
        changeOrigin: true
      }
    }
  }
})
```

**生产环境配置（修改 src/api/request.ts）：**

```ts
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',  // 生产环境使用相对路径，由 Nginx 代理
  timeout: 10000
})

export default api
```

#### 步骤 3：启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000 查看前端页面。

#### 步骤 4：构建生产版本

```bash
# 构建生产版本
npm run build

# 查看构建结果
ls -la dist/
```

预期输出：
```
dist/
├── index.html
├── assets/
│   ├── index-xxx.js
│   ├── index-xxx.css
│   └── ...
```

#### 步骤 5：本地预览生产版本

```bash
# 安装预览服务器
npm install -g serve

# 预览构建结果
serve -s dist -p 3001

# 访问 http://localhost:3001
```

---

### 3.3 Docker Compose 本地联调

#### 步骤 1：安装 Docker

**macOS:**
```bash
brew install docker docker-compose
```

**Windows:**
- 下载 Docker Desktop: https://www.docker.com/products/docker-desktop

**Linux:**
```bash
# Ubuntu
sudo apt install docker.io docker-compose
sudo systemctl start docker
sudo systemctl enable docker

# 添加用户到 docker 组
sudo usermod -aG docker $USER
```

#### 步骤 2：创建 Dockerfile

**后端 Dockerfile (`campus-secondhand-backend/Dockerfile`)：**

```dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/campus-secondhand-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**前端 Dockerfile (`campus-secondhand-frontend/Dockerfile`)：**

```dockerfile
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY ../deploy/nginx/campus-secondhand.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### 步骤 3：启动 Docker Compose

```bash
cd project

# 启动所有服务
docker-compose up -d

# 查看运行状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 访问服务
# 前端: http://localhost
# 后端: http://localhost:8080/api
```

#### 步骤 4：停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

---

## 4. 华为云部署流程

### 4.1 上传项目代码到 ECS

**方式一：Git 克隆（推荐）**

```bash
# 在 ECS 上执行
ssh root@<ECS公网IP>

cd /opt/campus-secondhand
git clone <项目仓库地址> .

# 或从 GitHub/Gitee 克隆
git clone https://github.com/xxx/campus-secondhand.git .
```

**方式二：SCP 上传**

```bash
# 在本地执行（上传整个项目）
scp -r project/* root@<ECS公网IP>:/opt/campus-secondhand/

# 上传单个文件
scp target/campus-secondhand-backend.jar root@<ECS公网IP>:/opt/campus-secondhand/backend/
```

### 4.2 配置生产环境华为云服务

在 ECS 上创建华为云服务配置文件：

```bash
# 创建配置文件
vi /opt/campus-secondhand/backend/src/main/resources/huawei-cloud.yml
```

写入以下内容（替换为实际值）：

```yaml
huawei-cloud:
  rds:
    enabled: true
    host: <RDS内网IP或域名>  # 如 rm-xxxx.mysql.cn-south-1.myhuaweicloud.com
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
    enabled: true  # 生产环境启用 OBS
    access-key: <你的AK>
    secret-key: <你的SK>
    endpoint: https://obs.cn-south-1.myhuaweicloud.com
    region: cn-south-1
    bucket: campus-secondhand-images
    upload:
      max-size: 10485760
      allowed-types: jpg,jpeg,png,gif,webp
      path-prefix: products/
```

**生产环境配置说明：**

| 配置项 | 值 | 说明 |
|--------|--------|------|
| `rds.host` | RDS 内网地址 | 从 RDS 控制台获取 |
| `rds.password` | 创建时设置的密码 | 妥善保管 |
| `obs.enabled` | `true` | 启用 OBS 图片上传 |
| `obs.access-key` | AK | 从我的凭证获取 |
| `obs.secret-key` | SK | 从我的凭证获取 |
| `obs.bucket` | 桶名称 | 创建的 OBS 桶名 |

**配置文件示例模板：**

项目中提供了配置示例模板文件 `huawei-cloud-example.yml`，可直接复制修改：

```bash
# 复制模板并修改
cp src/main/resources/huawei-cloud-example.yml src/main/resources/huawei-cloud.yml
vi src/main/resources/huawei-cloud.yml
```

### 4.3 构建后端

```bash
cd /opt/campus-secondhand/campus-secondhand-backend

# 编译打包
mvn clean package -DskipTests

# 查看结果
ls -la target/campus-secondhand-backend.jar
```

### 4.4 构建前端

```bash
cd /opt/campus-secondhand/campus-secondhand-frontend

# 安装依赖
npm install

# 构建
npm run build

# 复制到 Nginx 目录
cp -r dist/* /var/www/campus-secondhand/frontend/
```

### 4.5 配置 Nginx

```bash
# 复制配置文件
cp /opt/campus-secondhand/deploy/nginx/campus-secondhand.conf /etc/nginx/conf.d/

# 删除默认配置
rm /etc/nginx/conf.d/default.conf

# 测试配置
nginx -t

# 启动 Nginx
systemctl start nginx
systemctl enable nginx

# 查看状态
systemctl status nginx
```

### 4.6 启动后端服务

```bash
# 使用启动脚本
bash /opt/campus-secondhand/deploy/scripts/backend-start.sh

# 或手动启动（华为云配置已在 huawei-cloud.yml 中）
cd /opt/campus-secondhand/campus-secondhand-backend
nohup java -Xms512m -Xmx1024m \
  -jar target/campus-secondhand-backend.jar \
  > /var/log/campus-secondhand/backend.log 2>&1 &

# 查看进程
ps aux | grep java

# 查看日志
tail -f /var/log/campus-secondhand/backend.log
```

### 4.7 验证部署

```bash
# 在 ECS 上测试 API
curl http://localhost:8080/api/categories
curl http://localhost/api/categories  # 通过 Nginx

# 查看端口
netstat -tlnp | grep -E '80|8080'
```

### 4.8 外部访问验证

在浏览器访问：
```
http://<ECS公网IP>/

# 测试 API
http://<ECS公网IP>/api/categories
```

---

## 5. 常见问题与解决方案

### Q1: ECS 无法连接 RDS

**原因**：安全组未配置或网络不通

**解决**：
```bash
# 检查安全组是否开放 3306 端口给 ECS 内网 IP
# 检查 ECS 和 RDS 是否在同一 VPC

# 测试连通性
telnet <RDS内网IP> 3306
```

### Q2: 前端无法访问后端 API

**原因**：Nginx 配置错误或后端未启动

**解决**：
```bash
# 检查 Nginx 配置
nginx -t

# 检查后端进程
ps aux | grep java

# 检查日志
tail -f /var/log/campus-secondhand/backend.log
```

### Q3: OBS 上传失败

**原因**：AK/SK 配置错误或 Bucket 权限问题

**解决**：
```bash
# 检查配置
cat /opt/campus-secondhand/backend/application-prod.yml | grep obs

# 测试 OBS 连接（在 ECS 上）
curl -X PUT "<预签名URL>" -d "test"
```

### Q4: Maven 编译失败

**原因**：JDK 版本不匹配或依赖下载失败

**解决**：
```bash
# 检查 JDK 版本
java -version  # 必须是 21+

# 清理 Maven 缓存
rm -rf ~/.m2/repository

# 重新编译
mvn clean package -U
```

---

## 6. 部署检查清单

| 检查项 | 命令 | 状态 |
|--------|------|------|
| ECS 软件安装 | `java -v && mvn -v && node -v && nginx -v` | ⬜ |
| RDS 连接测试 | `mysql -h<RDS_IP> -uroot -p<密码> -e "SELECT 1"` | ⬜ |
| 数据库初始化 | `mysql -h<RDS_IP> ... -e "SHOW TABLES"` | ⬜ |
| 华为云配置文件 | `cat huawei-cloud.yml` 检查 RDS/OBS 配置 | ⬜ |
| OBS 配置 | 检查 huawei-cloud.yml obs 配置 | ⬜ |
| 后端编译 | `ls target/campus-secondhand-backend.jar` | ⬜ |
| 前端构建 | `ls dist/index.html` | ⬜ |
| Nginx 配置 | `nginx -t` | ⬜ |
| 后端启动 | `ps aux | grep java` | ⬜ |
| Nginx 启动 | `systemctl status nginx` | ⬜ |
| 外部访问 | 浏览器访问 `http://<ECS_IP>` | ⬜ |

---

**教程版本**: v2.0  
**最后更新**: 2026-04-23  
**作者**: 李嘉辉

**v2.0 更新内容：**
- 新增统一华为云服务配置文件 `huawei-cloud.yml`
- RDS 和 OBS 配置集中管理
- 优化配置文件结构，简化部署流程