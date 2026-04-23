# 管理员与用户商品管理功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为校园二手交易平台添加管理员角色和用户商品状态调整功能

**Architecture:** User 表新增 role 字段区分角色，JWT token 包含角色信息，管理员可硬删除任意商品，用户可调整自己商品状态（下架→在售、在售→已售）

**Tech Stack:** Spring Boot 3 + MyBatis + JWT + Vue 3 + Element Plus + MySQL

---

## 文件结构

| 层级 | 文件 | 修改类型 | 职责 |
|------|------|----------|------|
| **数据库** | `sql/schema-update.sql` | 新建 | ALTER TABLE + 管理员账号初始化 |
| **后端-实体** | `entity/User.java` | 修改 | 新增 role 字段 |
| **后端-上下文** | `controller/UserContext.java` | 修改 | 角色获取/判断方法 |
| **后端-JWT** | `utils/JwtUtil.java` | 修改 | token 包含 role |
| **后端-拦截器** | `config/JwtInterceptor.java` | 修改 | 提取 role 到 UserContext |
| **后端-Mapper** | `mapper/ProductMapper.java` | 修改 | 新增 hardDelete 方法 |
| **后端-Mapper XML** | `mapper/ProductMapper.xml` | 修改 | 新增 hardDelete SQL |
| **后端-Service** | `service/ProductService.java` | 修改 | 新增 updateStatus/adminDelete |
| **后端-Controller** | `controller/ProductController.java` | 修改 | 新增两个 API 接口 |
| **前端-状态** | `store/auth.ts` | 修改 | 存储 role + isAdmin |
| **前端-API** | `api/product.ts` | 修改 | 新增 API 方法 |
| **前端-个人中心** | `views/UserCenterView.vue` | 修改 | 修复状态文字 + 状态调整按钮 |
| **前端-首页** | `views/HomeView.vue` | 修改 | 管理员删除按钮 |
| **前端-详情页** | `views/ProductDetailView.vue` | 修改 | 管理员删除 + 用户状态调整 |

---

## Task 1: 创建数据库迁移脚本

**Files:**
- Create: `campus/sql/schema-update.sql`

- [ ] **Step 1: 创建 schema-update.sql 文件**

```sql
-- 管理员角色与商品管理功能 - 数据库迁移脚本
-- 执行时间: 2026-04-23

-- 1. User 表添加 role 字段
ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER-普通用户, ADMIN-管理员';

-- 2. 初始化管理员账号
-- 密码 'admin123' 的 BCrypt 加密值（需要在服务器上通过注册接口生成真实密码）
-- 以下为示例，实际使用时请通过注册接口创建管理员账号
-- INSERT INTO `user` (username, password, nickname, role, credit_score, status) 
-- VALUES ('admin', '$2a$10$...', '管理员', 'ADMIN', 100, 0);
```

- [ ] **Step 2: 验证文件创建成功**

```bash
ls -la campus/sql/schema-update.sql
```

Expected: 文件存在且内容正确

---

## Task 2: User.java - 新增 role 字段

**Files:**
- Modify: `campus-secondhand-backend/src/main/java/com/campus/secondhand/entity/User.java`

- [ ] **Step 1: 修改 User.java，新增 role 字段**

完整修改后的文件：

```java
package com.campus.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String email;
    private Integer creditScore;
    private Byte status;
    private String role;  // 新增: USER 或 ADMIN
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2: 验证修改**

```bash
grep "private String role" campus-secondhand-backend/src/main/java/com/campus/secondhand/entity/User.java
```

Expected: 输出包含 `private String role;`

---

## Task 3: UserContext.java - 新增角色相关方法

**Files:**
- Modify: `campus-secondhand-backend/src/main/java/com/campus/secondhand/controller/UserContext.java`

- [ ] **Step 1: 修改 UserContext.java，新增角色相关方法**

完整修改后的文件：

```java
package com.campus.secondhand.controller;

import com.campus.secondhand.exception.BusinessException;

public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw BusinessException.of(401, "未登录");
        }
        return userId;
    }

    public static void setCurrentUserRole(String role) {
        USER_ROLE.set(role);
    }

    public static String getCurrentUserRole() {
        return USER_ROLE.get();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(USER_ROLE.get());
    }

    public static void clear() {
        USER_ID.remove();
        USER_ROLE.remove();
    }
}
```

- [ ] **Step 2: 验证修改**

```bash
grep -E "(USER_ROLE|isAdmin|setCurrentUserRole)" campus-secondhand-backend/src/main/java/com/campus/secondhand/controller/UserContext.java
```

Expected: 输出包含新增的方法和字段

---

## Task 4: JwtUtil.java - token 包含 role

**Files:**
- Modify: `campus-secondhand-backend/src/main/java/com/campus/secondhand/utils/JwtUtil.java`

- [ ] **Step 1: 修改 JwtUtil.java，token 包含 role 信息**

完整修改后的文件：

```java
package com.campus.secondhand.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:campus-secondhand-default-secret-key-for-development-only}")
    private String secret;

    @Value("${jwt.expiration:604800000}") // 7 days in milliseconds
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 修改：生成 token 时包含 role
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)  // 新增 role claim
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // 保持兼容：旧方法（role 默认 USER）
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, "USER");
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    // 新增：获取 role
    public String getRole(String token) {
        Claims claims = parseToken(token);
        String role = claims.get("role", String.class);
        return role != null ? role : "USER";  // 兼容旧 token
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

- [ ] **Step 2: 验证修改**

```bash
grep -E "(claim\(\"role\"|getRole)" campus-secondhand-backend/src/main/java/com/campus/secondhand/utils/JwtUtil.java
```

Expected: 输出包含 role claim 和 getRole 方法

---

## Task 5: JwtInterceptor.java - 提取 role 到 UserContext

**Files:**
- Modify: `campus-secondhand-backend/src/main/java/com/campus/secondhand/config/JwtInterceptor.java`

- [ ] **Step 1: 修改 JwtInterceptor.java，提取 role 信息**

完整修改后的文件：

```java
package com.campus.secondhand.config;

import com.campus.secondhand.controller.UserContext;
import com.campus.secondhand.exception.BusinessException;
import com.campus.secondhand.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            throw BusinessException.of(401, "未登录");
        }

        if (!jwtUtil.validateToken(token)) {
            throw BusinessException.of(401, "登录已过期，请重新登录");
        }

        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);  // 新增：提取 role
        
        UserContext.setCurrentUserId(userId);
        UserContext.setCurrentUserRole(role);  // 新增：设置 role

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 2: 验证修改**

```bash
grep -E "(getRole|setCurrentUserRole)" campus-secondhand-backend/src/main/java/com/campus/secondhand/config/JwtInterceptor.java
```

Expected: 输出包含 role 提取和设置逻辑

---

## Task 6: ProductMapper - 新增硬删除方法

**Files:**
- Modify: `campus-secondhand-backend/src/main/java/com/campus/secondhand/mapper/ProductMapper.java`
- Modify: `campus-secondhand-backend/src/main/resources/mapper/ProductMapper.xml`

- [ ] **Step 1: 修改 ProductMapper.java，新增 hardDelete 方法**

修改后的文件（新增第 26 行）：

```java
package com.campus.secondhand.mapper;

import com.campus.secondhand.dto.request.ProductQueryRequest;
import com.campus.secondhand.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    int insert(Product product);

    Product selectById(@Param("id") Long id);

    List<Product> selectPage(ProductQueryRequest request);

    Long countPage(ProductQueryRequest request);

    int update(Product product);

    int delete(@Param("id") Long id);

    int increaseViewCount(@Param("id") Long id);

    // 新增：硬删除（真正的 DELETE FROM）
    int hardDelete(@Param("id") Long id);
}
```

- [ ] **Step 2: 修改 ProductMapper.xml，新增 hardDelete SQL**

在文件末尾 `</mapper>` 前新增：

```xml
    <!-- 新增：硬删除商品（真正的 DELETE FROM） -->
    <delete id="hardDelete">
        DELETE FROM product WHERE id = #{id}
    </delete>

</mapper>
```

- [ ] **Step 3: 验证修改**

```bash
grep "hardDelete" campus-secondhand-backend/src/main/java/com/campus/secondhand/mapper/ProductMapper.java
grep "hardDelete" campus-secondhand-backend/src/main/resources/mapper/ProductMapper.xml
```

Expected: 两个文件都包含 hardDelete

---

## Task 7: ProductService.java - 新增方法

**Files:**
- Modify: `campus-secondhand-backend/src/main/java/com/campus/secondhand/service/ProductService.java`

- [ ] **Step 1: 修改 ProductService.java，新增 updateStatus 和 adminDelete 方法**

在文件末尾（`parseImages` 和 `serializeImages` 方法之后）新增以下两个方法：

```java
    /**
     * 用户调整自己商品状态
     * 允许转换: 0(下架) → 1(在售), 1(在售) → 2(已售)
     */
    @Transactional
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
    @Transactional
    public void adminDelete(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw BusinessException.of(404, "商品不存在");
        }
        productMapper.hardDelete(id);  // 真正的 DELETE FROM
    }
```

- [ ] **Step 2: 验证修改**

```bash
grep -E "(updateStatus|adminDelete)" campus-secondhand-backend/src/main/java/com/campus/secondhand/service/ProductService.java
```

Expected: 输出包含两个新方法

---

## Task 8: ProductController.java - 新增接口

**Files:**
- Modify: `campus-secondhand-backend/src/main/java/com/campus/secondhand/controller/ProductController.java`

- [ ] **Step 1: 修改 ProductController.java，新增两个 API 接口**

在现有 `delete` 方法之后（第 62 行附近）新增以下两个方法：

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

- [ ] **Step 2: 验证修改**

```bash
grep -E "(updateStatus|adminDelete|@PutMapping.*status|@DeleteMapping.*admin)" campus-secondhand-backend/src/main/java/com/campus/secondhand/controller/ProductController.java
```

Expected: 输出包含两个新接口

---

## Task 9: 前端 auth.ts - 存储 role 和 isAdmin

**Files:**
- Modify: `campus-secondhand-frontend/src/store/auth.ts`

- [ ] **Step 1: 修改 auth.ts，新增 isAdmin 计算属性**

在文件顶部新增 `computed` 导入，在 state/user 后新增 isAdmin getter：

修改后的完整文件：

```typescript
import { defineStore } from 'pinia'
import { computed } from 'vue'  // 新增导入
import { login, register, logout, getUserInfo } from '../api/auth'
import { getToken, setToken, clearToken, setUser } from '../utils/token'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    user: null as any,
    isLoggedIn: !!getToken()
  }),

  getters: {
    // 新增：判断是否为管理员
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },

  actions: {
    async loginAction(username: string, password: string) {
      const res: any = await login({ username, password })
      if (res.code === 200) {
        this.token = res.data.token
        this.user = res.data.user
        this.isLoggedIn = true
        setToken(res.data.token)
        setUser(res.data.user)
      }
      return res
    },

    async registerAction(username: string, password: string, nickname: string) {
      const res: any = await register({ username, password, nickname })
      return res
    },

    async logoutAction() {
      await logout()
      this.token = null
      this.user = null
      this.isLoggedIn = false
      clearToken()
    },

    async fetchUser() {
      if (!this.token) return
      try {
        const res: any = await getUserInfo()
        if (res.code === 200) {
          this.user = res.data
          setUser(res.data)
        }
      } catch (e) {
        this.logoutAction()
      }
    }
  }
})
```

- [ ] **Step 2: 验证修改**

```bash
grep "isAdmin" campus-secondhand-frontend/src/store/auth.ts
```

Expected: 输出包含 isAdmin getter

---

## Task 10: 前端 product.ts - 新增 API 方法

**Files:**
- Modify: `campus-secondhand-frontend/src/api/product.ts`

- [ ] **Step 1: 修改 product.ts，新增两个 API 方法**

在文件末尾（`deleteProduct` 方法之后）新增：

```typescript
// 用户调整商品状态
export function updateProductStatus(id: number, status: number): Promise<any> {
  return api.put(`/products/${id}/status`, null, { params: { status } })
}

// 管理员删除商品
export function adminDeleteProduct(id: number): Promise<any> {
  return api.delete(`/products/admin/${id}`)
}
```

- [ ] **Step 2: 验证修改**

```bash
grep -E "(updateProductStatus|adminDeleteProduct)" campus-secondhand-frontend/src/api/product.ts
```

Expected: 输出包含两个新方法

---

## Task 11: 前端 UserCenterView.vue - 修复状态文字和新增状态调整

**Files:**
- Modify: `campus-secondhand-frontend/src/views/UserCenterView.vue`

- [ ] **Step 1: 修复 statusText 映射（第 127 行）**

将原有代码修改为：

```typescript
const statusText = (s: number) => ['下架', '在售', '已售'][s]
const statusClass = (s: number) => ['offline', 'selling', 'sold'][s]
```

- [ ] **Step 2: 导入新的 API 方法**

在第 105 行的导入中新增：

```typescript
import { deleteProduct, updateProductStatus } from '../api/product'
```

- [ ] **Step 3: 新增状态调整处理函数**

在第 141 行 `handleDeleteProduct` 之后新增：

```typescript
const handleRelist = async (id: number) => {
  const res: any = await updateProductStatus(id, 1)
  if (res.code === 200) { ElMessage.success('已重新上架'); fetchMyProducts() }
}

const handleMarkSold = async (id: number) => {
  const res: any = await updateProductStatus(id, 2)
  if (res.code === 200) { ElMessage.success('已标记为已售'); fetchMyProducts() }
}
```

- [ ] **Step 4: 修改商品列表按钮区域（第 63-66 行）**

将原有按钮区域修改为：

```html
            <div class="item-actions">
              <button class="action-btn edit" @click="editProduct(p.id)">编辑</button>
              <button v-if="p.status === 0" class="action-btn relist" @click="handleRelist(p.id)">重新上架</button>
              <button v-if="p.status === 1" class="action-btn sold" @click="handleMarkSold(p.id)">标记已售</button>
              <button class="action-btn delete" @click="handleDeleteProduct(p.id)">下架</button>
            </div>
```

- [ ] **Step 5: 新增按钮样式**

在 `<style scoped>` 部分（第 192 行附近）新增：

```css
.action-btn.relist { background: var(--color-acent-cool); color: white; }
.action-btn.sold { background: var(--color-accent-warm); color: white; }
```

- [ ] **Step 6: 验证修改**

```bash
grep -E "(updateProductStatus|handleRelist|handleMarkSold)" campus-secondhand-frontend/src/views/UserCenterView.vue
```

Expected: 输出包含新功能相关代码

---

## Task 12: 前端 HomeView.vue - 管理员删除按钮

**Files:**
- Modify: `campus-secondhand-frontend/src/views/HomeView.vue`

- [ ] **Step 1: 导入新的 API 和 isAdmin**

在第 171-172 行导入中新增：

```typescript
import { getProducts, getCategories, searchProducts, adminDeleteProduct, Category, Product, PageResult } from '../api/product'
```

- [ ] **Step 2: 新增 isAdmin computed（第 189 行后）**

在 `const user` computed 后新增：

```typescript
const isAdmin = computed(() => authStore.isAdmin)
```

- [ ] **Step 3: 新增管理员删除处理函数**

在第 277 行 `handleLogout` 后新增：

```typescript
const handleAdminDelete = async (id: number) => {
  try {
    const res: any = await adminDeleteProduct(id)
    if (res.code === 200) {
      ElMessage.success('商品已删除')
      fetchProducts()
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}
```

- [ ] **Step 4: 修改商品卡片模板，新增管理员删除按钮**

在第 109 行 `@click="goDetail(product.id)"` 后，卡片内新增删除按钮（需阻止事件冒泡）：

在 `<div class="card-body">` 之前，`<div class="card-image">` 之后新增：

```html
          <!-- 管理员删除按钮 -->
          <button 
            v-if="isAdmin" 
            class="admin-delete-btn" 
            @click.stop="handleAdminDelete(product.id)"
          >
            删除
          </button>
```

- [ ] **Step 5: 新增删除按钮样式**

在 `<style scoped>` 部分新增：

```css
.admin-delete-btn {
  position: absolute;
  top: 12px;
  left: 12px;
  background: var(--color-accent-pink);
  color: white;
  border: none;
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  cursor: pointer;
  z-index: 10;
  opacity: 0.9;
}

.admin-delete-btn:hover {
  opacity: 1;
  transform: scale(1.05);
}
```

- [ ] **Step 6: 验证修改**

```bash
grep -E "(adminDeleteProduct|isAdmin|handleAdminDelete|admin-delete-btn)" campus-secondhand-frontend/src/views/HomeView.vue
```

Expected: 输出包含管理员删除相关代码

---

## Task 13: 前端 ProductDetailView.vue - 管理员删除和用户状态调整

**Files:**
- Modify: `campus-secondhand-frontend/src/views/ProductDetailView.vue`

- [ ] **Step 1: 导入新的 API 和 isAdmin**

在第 110 行导入中新增：

```typescript
import { getProductById, adminDeleteProduct, updateProductStatus } from '../api/product'
```

- [ ] **Step 2: 新增 isAdmin 和 isOwner computed**

在第 123 行 `isLoggedIn` computed 后新增：

```typescript
const isAdmin = computed(() => authStore.isAdmin)
const isOwner = computed(() => authStore.user?.id === product.value?.userId)
```

- [ ] **Step 3: 新增管理员删除和状态调整处理函数**

在第 161 行 `createTransaction` 后新增：

```typescript
const handleAdminDelete = async () => {
  if (!product.value) return
  try {
    const res: any = await adminDeleteProduct(product.value.id)
    if (res.code === 200) {
      ElMessage.success('商品已删除')
      router.push('/')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleRelist = async () => {
  if (!product.value) return
  const res: any = await updateProductStatus(product.value.id, 1)
  if (res.code === 200) {
    ElMessage.success('已重新上架')
    fetchDetail()
  }
}

const handleMarkSold = async () => {
  if (!product.value) return
  const res: any = await updateProductStatus(product.value.id, 2)
  if (res.code === 200) {
    ElMessage.success('已标记为已售')
    fetchDetail()
  }
}
```

- [ ] **Step 4: 修改 action-zone 模板，新增管理/状态按钮**

将第 86-99 行的 action-zone 修改为：

```html
          <div class="action-zone">
            <!-- 管理员删除按钮 -->
            <button v-if="isAdmin" class="admin-delete-btn" @click="handleAdminDelete">
              <span class="btn-icon">🗑️</span>
              <span class="btn-text">删除商品</span>
            </button>
            
            <!-- 卖家状态调整按钮 -->
            <template v-if="isOwner">
              <button v-if="product.status === 0" class="relist-btn" @click="handleRelist">
                <span class="btn-icon">📤</span>
                <span class="btn-text">重新上架</span>
              </button>
              <button v-if="product.status === 1" class="sold-btn" @click="handleMarkSold">
                <span class="btn-icon">✅</span>
                <span class="btn-text">标记已售</span>
              </button>
            </template>
            
            <!-- 普通用户操作 -->
            <button class="contact-btn" @click="contactSeller" v-if="isLoggedIn && !isOwner">
              <span class="btn-icon">💬</span>
              <span class="btn-text">联系卖家</span>
            </button>
            <button class="buy-btn" @click="createTransaction" v-if="isLoggedIn && !isOwner && product.status === 1">
              <span class="btn-icon">🛒</span>
              <span class="btn-text">发起交易</span>
            </button>
            <div class="login-hint" v-if="!isLoggedIn">
              <p>登录后可联系卖家</p>
              <button class="login-btn" @click="goLogin">去登录</button>
            </div>
          </div>
```

注意：需要将原来的 `product.status === 0` 改为 `product.status === 1`（因为在售是 status=1）

- [ ] **Step 5: 新增按钮样式**

在 `<style scoped>` 部分新增：

```css
.admin-delete-btn {
  background: var(--color-accent-pink);
  color: white;
  padding: 16px;
  border: none;
  border-radius: var(--radius-md);
  font-family: var(--font-display);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex: 1;
}

.relist-btn {
  background: var(--color-acent-cool);
  color: white;
  padding: 16px;
  border: none;
  border-radius: var(--radius-md);
  font-family: var(--font-display);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex: 1;
}

.sold-btn {
  background: var(--color-accent-warm);
  color: white;
  padding: 16px;
  border: none;
  border-radius: var(--radius-md);
  font-family: var(--font-display);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex: 1;
}
```

- [ ] **Step 6: 验证修改**

```bash
grep -E "(isAdmin|isOwner|handleAdminDelete|handleRelist|handleMarkSold)" campus-secondhand-frontend/src/views/ProductDetailView.vue
```

Expected: 输出包含新功能相关代码

---

## Task 14: 部署到服务器

**Files:**
- 执行部署命令

- [ ] **Step 1: 上传修改后的源代码到服务器**

```bash
cd /Users/a0000/Documents/云计算实验/2 课程大作业/project/campus

# 上传后端源代码
scp -r campus-secondhand-backend/src root@120.46.48.71:/opt/campus-secondhand/campus-secondhand-backend/

# 上传前端源代码
scp -r campus-secondhand-frontend/src root@120.46.48.71:/opt/campus-secondhand/campus-secondhand-frontend/

# 上传数据库迁移脚本
scp sql/schema-update.sql root@120.46.48.71:/opt/campus-secondhand/sql/
```

- [ ] **Step 2: SSH 连接服务器，执行数据库迁移和构建**

```bash
ssh root@120.46.48.71 << 'EOF'
cd /opt/campus-secondhand

# 执行数据库迁移（添加 role 字段）
mysql campus < sql/schema-update.sql

# 构建后端
cd campus-secondhand-backend
mvn clean package -DskipTests

# 构建前端
cd ../campus-secondhand-frontend
npm run build
cp -r dist/* /var/www/campus-secondhand/

# 重启后端服务
killall java
nohup java -jar /opt/campus-secondhand/campus-secondhand-backend/target/campus-secondhand-backend.jar > app.log 2>&1 &

# 验证服务启动
sleep 5
ps aux | grep java
EOF
```

- [ ] **Step 3: 创建管理员账号**

通过注册接口创建管理员账号，或直接在数据库中插入：

```sql
-- 使用注册接口注册 admin 用户后，手动更新 role 为 ADMIN
UPDATE user SET role = 'ADMIN' WHERE username = 'admin';
```

- [ ] **Step 4: 验证部署成功**

访问网站：
- 管理员登录后，首页商品卡片应显示"删除"按钮
- 用户登录后，个人中心应显示"重新上架"/"标记已售"按钮

---

## 自我检查

### 1. Spec 覆盖检查

| Spec 需求 | 对应 Task |
|-----------|-----------|
| User 表添加 role 字段 | Task 1, Task 2 |
| 初始化管理员账号 | Task 14 Step 3 |
| UserContext 角色方法 | Task 3 |
| JWT token 包含 role | Task 4, Task 5 |
| ProductMapper 硬删除 | Task 6 |
| ProductService 新方法 | Task 7 |
| ProductController 新接口 | Task 8 |
| 前端 auth.ts isAdmin | Task 9 |
| 前端 API 方法 | Task 10 |
| 前端状态调整按钮 | Task 11, Task 13 |
| 前端管理员删除按钮 | Task 12, Task 13 |
| 部署流程 | Task 14 |

✅ 所有需求已覆盖

### 2. Placeholder 检查

- 无 TBD/TODO
- 无 "implement later"
- 无 "add appropriate error handling"
- 所有代码步骤包含具体代码
- 所有命令步骤包含具体命令

✅ 无 placeholder 问题

### 3. 类型一致性检查

- User.role: String → JwtUtil.getRole: String → UserContext.USER_ROLE: ThreadLocal<String>
- status: Byte → updateProductStatus(id: number, status: number)
- ProductMapper.hardDelete(int) → ProductService.adminDelete(Long)

✅ 类型一致

---

**计划完成，保存到 `docs/superpowers/plans/2026-04-23-admin-user-product-management.md`**