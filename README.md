# 校园二手物品交易系统

**项目名称**: Campus Secondhand Trading System  
**作者**: 李嘉辉  
**学号**: 3123004443  
**课程**: 虚拟化与云计算  
**学校**: 广东工业大学  

---

## 项目简介

校园二手物品交易系统是一个面向广东工业大学学生的二手物品交易平台，基于华为云 ECS + RDS + OBS 服务构建，提供商品发布、搜索筛选、在线沟通、交易管理、信用评价等完整功能。

---

## 目录结构

```
校园二手物品交易系统/
│
├── campus-secondhand-backend/     # 后端源码 (Spring Boot 3)
│   ├── src/main/java/
│   │   └── com/gdut/secondhand/
│   │       ├── controller/        # REST API 控制器
│   │       ├── service/           # 业务逻辑层
│   │       ├── mapper/            # MyBatis Mapper
│   │       ├── entity/            # 数据实体
│   │       ├── dto/               # 请求/响应 DTO
│   │       ├── exception/         # 异常处理
│   │       └── utils/             # 工具类 (JWT等)
│   ├── src/main/resources/
│   │   ├── application.yml        # 基础配置文件
│   │   ├── huawei-cloud.yml       # 华为云服务配置（RDS + OBS）
│   │   ├── huawei-cloud-example.yml # 华为云配置示例模板
│   │   └── mapper/*.xml           # MyBatis XML
│   └── pom.xml                    # Maven 配置
│
├── campus-secondhand-frontend/    # 前端源码 (Vue 3)
│   ├── src/
│   │   ├── views/                 # 页面视图 (9个页面)
│   │   ├── components/            # UI 组件
│   │   ├── api/                   # API 请求封装
│   │   ├── router/                # Vue Router 配置
│   │   ├── store/                 # Pinia 状态管理
│   │   ├── styles/                # 全局样式
│   │   └── utils/                 # 工具函数
│   ├── index.html                 # 入口 HTML
│   ├── vite.config.ts             # Vite 配置
│   └── package.json               # npm 配置
│
├── sql/                           # 数据库脚本
│   ├── schema.sql                 # 建表脚本 (6张表)
│   └── seed.sql                   # 种子数据 (5个分类)
│
├── deploy/                        # 部署配置
│   ├── nginx/
│   │   └ campus-secondhand.conf   # Nginx 配置
│   └── scripts/
│       ├── backend-start.sh       # 后端启动脚本
│       ├── frontend-build.sh      # 前端构建脚本
│       └── init-rds.sh            # 数据库初始化脚本
│
├── docs/                          # 项目文档
│   ├── deployment.md              # 部署说明
│   ├── test-report.md             # 测试报告模板
│   ├── huawei-cloud-deployment-tutorial.md  # 华为云配置教程
│   └── screenshots/               # 系统截图目录
│       └── README.md              # 截图清单
│
├── 课程大作业要求-虚拟化与云计算.md   # 课程报告 (已填写)
├── 大作业要求.txt                  # 作业要求原文
├── 大作业参考-虚拟云计算课程设计-代表作1_.md  # 参考案例
│
├── docker-compose.yml             # Docker Compose 配置
│
└── README.md                      # 本文件
```

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | Java 企业级框架 |
| MyBatis | 3.0.3 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| JWT | jjwt 0.12.5 | 身份认证 |
| 华为云 OBS SDK | 3.23.9 | 对象存储 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 | 3.4.x | 前端框架 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Element Plus | 2.6.x | UI 组件库 |
| Vite | 5.x | 构建工具 |

### 云服务

| 服务 | 说明 |
|------|------|
| ECS | 弹性云服务器 (部署应用) |
| RDS | 关系型数据库 MySQL |
| OBS | 对象存储 (商品图片) |

---

## 快速开始

### 1. 本地开发环境

确保已安装：
- JDK 21+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

### 2. 后端启动

```bash
cd campus-secondhand-backend

# 初始化数据库（自动读取 huawei-cloud.yml 配置）
bash ../deploy/scripts/init-rds.sh

# 或手动初始化
mysql -uroot -proot -e "CREATE DATABASE campus_secondhand"
mysql -uroot -proot campus_secondhand < ../sql/schema.sql
mysql -uroot -proot campus_secondhand < ../sql/seed.sql

# 编译运行
mvn spring-boot:run
```

后端运行在 http://localhost:8080

### 3. 前端启动

```bash
cd campus-secondhand-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端运行在 http://localhost:3000

### 4. Docker Compose 本地联调

```bash
docker-compose up -d
```

访问 http://localhost

---

## 华为云部署

详细部署教程请参阅：

- **docs/huawei-cloud-deployment-tutorial.md** - 完整的华为云配置与项目编译教程

核心步骤：
1. 创建 ECS 实例 (2vCPU/4GB)
2. 创建 RDS MySQL 8.0 实例
3. 创建 OBS Bucket
4. 配置安全组端口 (80, 8080, 3306)
5. 上传代码并编译
6. 配置 `huawei-cloud.yml` 华为云服务配置
7. 配置 Nginx 反向代理
8. 启动后端服务

---

## 功能清单

| 模块 | 功能 | 页面/接口 |
|------|------|----------|
| 认证 | 注册、登录 | LoginView, RegisterView |
| 商品 | 发布、浏览、搜索 | HomeView, PublishView, ProductDetailView |
| 交易 | 创建、确认、完成、取消 | TransactionsView |
| 消息 | 发送、接收、对话 | MessagesView |
| 评价 | 发起评价、查看评价 | ReviewView, UserCenterView |
| 用户 | 个人信息、发布记录 | UserCenterView |

---

## 截图要求

课程报告需要系统截图，请按以下清单截图并放入 `docs/screenshots/` 目录：

详见 `docs/screenshots/README.md`

关键截图：
- 首页商品列表
- 登录/注册页面
- 商品详情页
- 发布商品页
- 个人中心
- 消息中心
- 交易管理
- 评价页面

---

## 交付清单

课程提交需要以下内容：

1. **源码**：本目录所有源代码
2. **报告**：课程大作业要求-虚拟化与云计算.md (已填写)
3. **截图**：docs/screenshots/ 目录中的系统截图
4. **华为云截图**：附录中的 ECS/RDS/OBS 截图

---

## 作者信息

- **姓名**: 李嘉辉
- **学号**: 3123004443
- **学院**: 计算机学院
- **班级**: 软件工程卓越班
- **指导教师**: 张静
- **提交日期**: 2026年4月27日

---

**最后更新**: 2026-04-23

**更新内容：**
- 新增华为云统一配置文件 `huawei-cloud.yml`
- RDS 和 OBS 配置集中管理，简化部署流程