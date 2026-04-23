# 校园二手物品交易系统 - 测试报告

**项目名称**: 校园二手物品交易系统 (Campus Secondhand Trading System)  
**测试日期**: 2026-04-23  
**测试环境**: 本地开发环境 + 华为云 ECS/RDS/OBS  
**测试人员**: [填写姓名]  

---

## 1. 测试概述

本测试报告覆盖校园二手物品交易系统的核心功能模块，包括用户认证、商品管理、交易流程、消息沟通、评价体系等。测试方式包括单元测试、接口测试和功能验收测试。

---

## 2. 测试命令汇总

### 2.1 后端测试

```bash
cd campus-secondhand-backend

# 运行所有单元测试
mvn test

# 运行指定模块测试
mvn test -Dtest=AuthControllerTest          # 认证模块
mvn test -Dtest=ProductControllerTest       # 商品模块
mvn test -Dtest=TransactionFlowTest         # 交易流程测试
mvn test -Dtest=SchemaSmokeTest             # 数据库连接测试

# 构建项目（跳过测试）
mvn clean package -DskipTests

# 启动后端服务
mvn spring-boot:run
```

### 2.2 前端测试

```bash
cd campus-secondhand-frontend

# 安装依赖
npm install

# 运行单元测试
npm run test

# 运行指定测试文件
npx vitest run src/tests/auth.spec.ts
npx vitest run src/tests/product-create.spec.ts
npx vitest run src/tests/transaction-status.spec.ts

# 构建生产版本
npm run build

# 启动开发服务器
npm run dev
```

### 2.3 数据库验证

```bash
# 创建数据库
mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS campus_secondhand DEFAULT CHARACTER SET utf8mb4"

# 导入表结构
mysql -uroot -proot campus_secondhand < sql/schema.sql

# 导入种子数据
mysql -uroot -proot campus_secondhand < sql/seed.sql

# 验证表结构
mysql -uroot -proot campus_secondhand -e "SHOW TABLES"
```

---

## 3. 功能验收表

| 序号 | 验收项 | 验证方式 | 预期结果 | 实际结果 | 状态 |
|------|--------|----------|----------|----------|------|
| 1 | 用户注册 | POST /api/auth/register，传入 username/password/nickname | 返回 code=0，用户创建成功 | [待填写] | ⬜ |
| 2 | 用户登录 | POST /api/auth/login，传入正确凭证 | 返回 JWT Token 和用户信息 | [待填写] | ⬜ |
| 3 | JWT 认证 | 登录后携带 Token 访问 /api/user/info | 返回用户详情，非登录状态返回 401 | [待填写] | ⬜ |
| 4 | 商品发布 | POST /api/products，携带完整商品信息 | 商品创建成功，返回商品 ID | [待填写] | ⬜ |
| 5 | OBS 图片上传 | 获取预签名 URL，前端直传图片 | 图片上传成功，返回可访问 URL | [待填写] | ⬜ |
| 6 | 商品列表 | GET /api/products?pageNum=1&pageSize=10 | 返回分页商品数据 | [待填写] | ⬜ |
| 7 | 关键词搜索 | GET /api/products/search?keyword=教材 | 返回匹配商品列表 | [待填写] | ⬜ |
| 8 | 分类筛选 | GET /api/products/search?categoryId=2 | 返回指定分类商品 | [待填写] | ⬜ |
| 9 | 商品详情 | GET /api/products/{id}，浏览数自动增加 | 返回商品详情，viewCount + 1 | [待填写] | ⬜ |
| 10 | 创建交易 | POST /api/transactions，指定商品和买家 | 交易创建成功，状态为"待确认" | [待填写] | ⬜ |
| 11 | 确认交易 | PUT /api/transactions/{id}/confirm（卖家操作） | 状态变为"进行中" | [待填写] | ⬜ |
| 12 | 完成交易 | PUT /api/transactions/{id}/complete | 状态变为"已完成"，商品标记已售 | [待填写] | ⬜ |
| 13 | 取消交易 | PUT /api/transactions/{id}/cancel | 状态变为"已取消" | [待填写] | ⬜ |
| 14 | 发送消息 | POST /api/messages，指定接收者和内容 | 消息保存成功 | [待填写] | ⬜ |
| 15 | 获取对话 | GET /api/messages/conversation/{userId} | 返回双方聊天记录 | [待填写] | ⬜ |
| 16 | 发起评价 | POST /api/reviews，评分 1-5 星 | 评价保存成功，信用分更新 | [待填写] | ⬜ |
| 17 | Nginx 反向代理 | 访问 http://<ECS-IP>/api/... | API 请求正确转发到后端 | [待填写] | ⬜ |
| 18 | 前端静态资源 | 访问 http://<ECS-IP>/ | 页面正常加载 | [待填写] | ⬜ |

---

## 4. 接口测试场景

### 4.1 认证流程测试

```bash
# 注册新用户
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test123456","nickname":"测试用户"}'

# 登录获取 Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test123456"}'

# 使用 Token 获取用户信息
curl -X GET http://localhost:8080/api/user/info \
  -H "Authorization: Bearer <TOKEN>"
```

### 4.2 商品流程测试

```bash
# 获取分类列表
curl http://localhost:8080/api/categories

# 发布商品（需携带 Token）
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"title":"高数教材","categoryId":2,"price":30,"description":"同济第七版"}'

# 搜索商品
curl "http://localhost:8080/api/products/search?keyword=高数&categoryId=2&pageNum=1&pageSize=10"

# 获取商品详情
curl http://localhost:8080/api/products/1
```

### 4.3 交易流程测试

```bash
# 创建交易（买家发起）
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer <BUYER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"productId":1}'

# 确认交易（卖家操作）
curl -X PUT http://localhost:8080/api/transactions/1/confirm \
  -H "Authorization: Bearer <SELLER_TOKEN>"

# 完成交易
curl -X PUT http://localhost:8080/api/transactions/1/complete \
  -H "Authorization: Bearer <BUYER_TOKEN>"

# 发起评价
curl -X POST http://localhost:8080/api/reviews \
  -H "Authorization: Bearer <BUYER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"transactionId":1,"rating":5,"content":"交易顺利，卖家很友好"}'
```

---

## 5. 前端页面测试清单

| 页面 | 路径 | 测试项 | 状态 |
|------|------|--------|------|
| 首页 | / | 商品卡片展示、分类筛选、关键词搜索 | ⬜ |
| 登录页 | /login | 表单校验、登录成功跳转 | ⬜ |
| 注册页 | /register | 表单校验、注册成功跳转 | ⬜ |
| 商品详情 | /product/:id | 图片轮播、卖家信息、联系按钮 | ⬜ |
| 发布页 | /publish | 图片上传、表单校验、发布成功 | ⬜ |
| 个人中心 | /user/:id | 个人信息、我发布的、交易记录、评价列表 | ⬜ |
| 消息中心 | /messages | 会话列表、聊天面板、消息发送 | ⬜ |
| 交易管理 | /transactions | 买家/卖家切换、状态操作、评价入口 | ⬜ |
| 评价页 | /review/:id | 星级选择、评价内容、提交成功 | ⬜ |

---

## 6. 华为云部署验证

| 云服务 | 验证项 | 状态 |
|--------|--------|------|
| ECS | 后端服务正常启动 (mvn spring-boot:run) | ⬜ |
| ECS | Nginx 正常运行 (systemctl status nginx) | ⬜ |
| ECS | 前端静态资源部署 (dist 目录) | ⬜ |
| RDS | 数据库连接正常 (jdbc:mysql://...) | ⬜ |
| RDS | 表结构和种子数据导入成功 | ⬜ |
| OBS | Bucket 创建成功 | ⬜ |
| OBS | 图片上传和访问正常 | ⬜ |
| 网络 | ECS 80 端口对外开放 | ⬜ |
| 网络 | RDS 内网连接正常 | ⬜ |

---

## 7. 测试结果汇总

| 模块 | 测试数量 | 通过 | 失败 | 覆盖率 |
|------|----------|------|------|--------|
| 认证模块 | - | - | - | - |
| 商品模块 | - | - | - | - |
| 交易模块 | - | - | - | - |
| 消息模块 | - | - | - | - |
| 评价模块 | - | - | - | - |
| 前端组件 | - | - | - | - |

---

## 8. 问题记录

| 序号 | 问题描述 | 发现时间 | 严重程度 | 处理状态 |
|------|----------|----------|----------|----------|
| 1 | [待填写] | - | - | - |

---

## 9. 测试结论

**整体评价**: [待填写]

**建议**: 
1. [待填写]

---

**测试报告版本**: v1.0  
**最后更新**: 2026-04-23