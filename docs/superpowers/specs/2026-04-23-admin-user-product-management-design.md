# 管理员与用户商品管理功能设计

**日期**: 2026-04-23  
**项目**: 校园二手交易平台 (campus-secondhand)

---

## 概述

为校园二手交易平台添加管理员功能和用户商品状态调整功能：
- 管理员可硬删除任意商品
- 用户可调整自己商品状态（重新上架、标记已售）
- 功能集成在现有页面中

---

## 需求确认

| 需求 | 决策 |
|------|------|
| 管理员标识方式 | User 表添加 `role` 字段 |
| 管理员删除方式 | 硬删除（从数据库彻底删除） |
| 用户状态调整 | 重新上架（下架→在售）、标记已售（在售→已售） |
| 管理员界面位置 | 集成在现有页面（首页/商品详情页显示"删除"按钮） |
| 部署方式 | 上传源代码到服务器，服务器构建 |

---

## 商品状态定义（基于现有代码实际使用）

| status 值 | 含义 | 后端实际使用 |
|-----------|------|-------------|
| **0** | 下架 | 用户"下架"操作设置此状态 |
| **1** | 在售 | 新建商品默认值，列表查询条件 `status=1` |
| **2** | 已售 | 未使用，可用于标记已售 |

**状态转换规则：**
- 下架 → 在售 (0 → 1)：重新上架
- 在售 → 已售 (1 → 2)：标记已售

---

## 数据库修改

### 1. User 表添加 role 字段

```sql
ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER-普通用户, ADMIN-管理员';
```

### 2. 初始化管理员账号

```sql
-- 管理员账号密码需要通过后端注册接口生成加密密码，或手动插入
-- 示例：用户名 admin，密码需使用 BCrypt 加密
INSERT INTO `user` (username, password, nickname, role, credit_score, status) 
VALUES ('admin', '<BCrypt加密后的密码>', '管理员', 'ADMIN', 100, 0);
```

### 3. 修复 schema.sql 注释（可选，保持一致性）

```sql
-- 原注释: 0-在售, 1-已售, 2-下架
-- 修正为: 0-下架, 1-在售, 2-已售
`status` TINYINT DEFAULT 0 COMMENT '状态: 0-下架, 1-在售, 2-已售',
```

---

## 后端修改

### 1. User.java - 新增 role 字段

```java
private String role; // USER 或 ADMIN
```

### 2. UserContext.java - 新增角色获取方法

```java
private static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();

public static void setCurrentUserRole(String role) {
    USER_ROLE.set(role);
}

public static String getCurrentUserRole() {
    return USER_ROLE.get();
}

public static boolean isAdmin() {
    return "ADMIN".equals(USER_ROLE.get());
}

// clear() 方法中同时清除 USER_ROLE
public static void clear() {
    USER_ID.remove();
    USER_ROLE.remove();
}
```

### 3. JwtUtil.java - token 包含 role

- 生成 token 时添加 role claim
- 解析 token 时提取 role
- 新增 `getRole(String token)` 方法

### 4. JwtInterceptor.java - 提取 userId 和 role

```java
Long userId = jwtUtil.getUserId(token);
String role = jwtUtil.getRole(token);
UserContext.setCurrentUserId(userId);
UserContext.setCurrentUserRole(role);
```

### 5. ProductService.java - 新增方法

```java
/**
 * 用户调整自己商品状态
 * 允许转换: 0(下架) → 1(在售), 1(在售) → 2(已售)
 */
public void updateStatus(Long id, Byte newStatus, Long userId) {
    Product product = productMapper.selectById(id);
    if (product == null) {
        throw BusinessException.of(404, "商品不存在");
    }
    if (!product.getUserId().equals(userId)) {
        throw BusinessException.of(403, "无权修改此商品");
    }
    
    // 状态转换校验
    Byte currentStatus = product.getStatus();
    boolean validTransition = 
        (currentStatus == 0 && newStatus == 1) ||  // 下架 → 在售
        (currentStatus == 1 && newStatus == 2);     // 在售 → 已售
    if (!validTransition) {
        throw BusinessException.of(400, "不允许的状态转换");
    }
    
    product.setStatus(newStatus);
    productMapper.update(product);
}

/**
 * 管理员硬删除商品
 */
public void adminDelete(Long id) {
    Product product = productMapper.selectById(id);
    if (product == null) {
        throw BusinessException.of(404, "商品不存在");
    }
    productMapper.hardDelete(id); // 真正的 DELETE FROM
}
```

### 6. ProductMapper.java - 新增硬删除方法

```java
int hardDelete(@Param("id") Long id);
```

### 7. ProductMapper.xml - 新增硬删除 SQL

```xml
<delete id="hardDelete">
    DELETE FROM product WHERE id = #{id}
</delete>
```

### 8. ProductController.java - 新增接口

```java
// 用户调整自己商品状态
@PutMapping("/{id}/status")
public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Byte status) {
    Long userId = UserContext.getCurrentUserId();
    productService.updateStatus(id, status, userId);
    return ApiResponse.success("状态已更新");
}

// 管理员删除商品（需管理员权限）
@DeleteMapping("/admin/{id}")
public ApiResponse<Void> adminDelete(@PathVariable Long id) {
    if (!UserContext.isAdmin()) {
        throw BusinessException.of(403, "需要管理员权限");
    }
    productService.adminDelete(id);
    return ApiResponse.success("商品已删除");
}
```

---

## 前端修改

### 1. store/auth.ts - 存储 role

```typescript
interface User {
  id: number
  username: string
  nickname: string
  role: string // 'USER' | 'ADMIN'
  ...
}

// 添加 isAdmin 计算属性
const isAdmin = computed(() => user.value?.role === 'ADMIN')
```

### 2. HomeView.vue - 商品卡片显示管理员删除按钮

- 商品卡片区域：如果 `isAdmin`，显示"删除"按钮
- 点击后调用 `/api/products/admin/{id}` DELETE 接口
- 删除成功后刷新列表

### 3. ProductDetailView.vue - 商品详情页

- 管理员可见"删除"按钮（调用 adminDelete API）
- 如果是卖家自己的商品，显示状态调整按钮：
  - 下架商品 (status=0) 显示"重新上架"，点击设为 status=1
  - 在售商品 (status=1) 显示"标记已售"，点击设为 status=2

### 4. UserCenterView.vue - 我的发布列表

**修复状态文字映射：**

```typescript
// 原代码（错误）:
const statusText = (s: number) => ['在售', '已售', '下架'][s]

// 修正为:
const statusText = (s: number) => ['下架', '在售', '已售'][s]
```

**新增状态调整按钮：**

- 下架商品 (status=0) 显示"重新上架"
- 在售商品 (status=1) 显示"标记已售"
- 调用 `/api/products/{id}/status` PUT 接口

### 5. api/product.ts - 新增 API 方法

```typescript
// 用户调整商品状态
export const updateProductStatus = (id: number, status: number) => 
  request.put(`/api/products/${id}/status`, null, { params: { status } })

// 管理员删除商品
export const adminDeleteProduct = (id: number) => 
  request.delete(`/api/products/admin/${id}`)
```

---

## API 接口总结

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/api/products/{id}/status` | PUT | 用户本人 | 调整自己商品状态 (0→1 或 1→2) |
| `/api/products/admin/{id}` | DELETE | 管理员 | 硬删除任意商品 |

---

## 部署流程

服务器已部署该项目，上传源代码后在服务器构建：

```bash
# 1. 上传修改后的源代码
scp -r campus-secondhand-backend/src root@120.46.48.71:/opt/campus-secondhand/campus-secondhand-backend/
scp -r campus-secondhand-frontend/src root@120.46.48.71:/opt/campus-secondhand/campus-secondhand-frontend/

# 2. 上传数据库迁移脚本
scp sql/schema-update.sql root@120.46.48.71:/opt/campus-secondhand/sql/

# 3. SSH 执行构建和重启
ssh root@120.46.48.71 << 'EOF'
cd /opt/campus-secondhand

# 数据库迁移
mysql -u<用户> -p<密码> campus < sql/schema-update.sql

# 构建后端
cd campus-secondhand-backend
mvn clean package -DskipTests

# 构建前端
cd ../campus-secondhand-frontend
npm run build
cp -r dist/* /var/www/campus-secondhand/

# 重启后端
killall java
nohup java -jar /opt/campus-secondhand/campus-secondhand-backend/target/campus-secondhand-backend.jar > app.log 2>&1 &
EOF
```

---

## 涉及文件清单

### 后端 (campus-secondhand-backend)

| 文件 | 操作 |
|------|------|
| `entity/User.java` | 修改 - 新增 role 字段 |
| `controller/UserContext.java` | 修改 - 新增角色相关方法 |
| `utils/JwtUtil.java` | 修改 - token 包含 role，新增 getRole 方法 |
| `config/JwtInterceptor.java` | 修改 - 提取 role 到 UserContext |
| `service/ProductService.java` | 修改 - 新增 updateStatus、adminDelete 方法 |
| `controller/ProductController.java` | 修改 - 新增两个接口 |
| `mapper/ProductMapper.java` | 修改 - 新增 hardDelete 方法 |
| `mapper/ProductMapper.xml` | 修改 - 新增 hardDelete SQL |

### 前端 (campus-secondhand-frontend)

| 文件 | 操作 |
|------|------|
| `store/auth.ts` | 修改 - 存储 role，添加 isAdmin |
| `views/HomeView.vue` | 修改 - 管理员删除按钮 |
| `views/ProductDetailView.vue` | 修改 - 管理员删除、用户状态调整 |
| `views/UserCenterView.vue` | 修改 - 修复状态文字、新增状态调整按钮 |
| `api/product.ts` | 修改 - 新增 API 调用方法 |

### 数据库

| 文件 | 操作 |
|------|------|
| `sql/schema-update.sql` | 新建 - ALTER TABLE 和管理员账号 |
| `sql/schema.sql` | 可选修复 - 更正 status 注释 |

---

## 测试验证

1. 管理员登录后，首页商品卡片显示"删除"按钮
2. 管理员点击删除，商品从列表消失，数据库记录彻底删除
3. 普通用户登录，进入个人中心：
   - 下架商品显示"重新上架"按钮，点击后状态变为在售
   - 在售商品显示"标记已售"按钮，点击后状态变为已售
4. 商品详情页：
   - 管理员可见"删除"按钮
   - 卖家可见状态调整按钮
5. 非管理员调用删除接口返回 403
6. 用户尝试不允许的状态转换（如已售→在售）返回 400

---

**设计完成，等待用户审核后进入实现计划阶段。**