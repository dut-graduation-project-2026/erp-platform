# 🔐 ERP Platform - Authentication Flow Implementation (4 Bước)

## Tổng Quan

Hệ thống sử dụng **Mock Data** để giả lập backend trước khi backend hoàn thành. Luồng xác thực được chia thành **4 bước chi tiết**:

---

## 🟢 BƯỚC 1: XÁC THỰC (AUTHENTICATION)

**Vị trí:** `/login` → `LoginPage` → `LoginForm`

### Quy trình:

1. **Người dùng nhập Email/Password** tại `/login`
2. **Frontend gọi Backend** (hoặc Mock Service):
   - **Email:** admin@erp.com → System Admin
   - **Email:** user@erp.com → Org User
   - **Email:** manager@erp.com → Org User
   - **Mật khẩu:** admin123, user123, manager123
3. **Backend trả về:**
   - Thông tin User cơ bản (id, firstName, lastName, email, role)
   - **Flag quan trọng:** `isSystemAdmin: true/false`
   - Set **HttpOnly Cookies:**
     - `access_token` (JWT Token)
     - `refresh_token` (Token để refresh)

4. **Frontend xử lý (useLogin hook):**

   ```typescript
   // Lưu thông tin User vào Zustand Store
   setUser(user);

   // Load danh sách Organization mà User tham gia
   setOrganizations(mockUserOrganizations);

   // Load danh sách Permission của User
   setPermissions(permissions);

   // Set Cookies cho Middleware Server-side
   document.cookie = `userRole=${user.role}`;
   document.cookie = `userOrgIds=${orgIds.join(",")}`;
   document.cookie = `access_token=...`; // From backend
   document.cookie = `isSystemAdmin=${user.isSystemAdmin}`;
   ```

**File chính:**

- [useLogin.ts](./features/auth/hooks/useLogin.ts) - Logic xác thực
- [LoginForm.tsx](./features/auth/components/login/LoginForm.tsx) - UI
- [api-client.ts](./services/api-client.ts) - Axios config (withCredentials: true)

---

## 🟡 BƯỚC 2: PHÂN LUỒNG ĐIỀU HƯỚNG (ROUTING DECISION)

**Vị trí:** `authFlow.ts` → `getRedirectPath(user)`

### Quy trình:

Sau khi xác thực thành công, **kiểm tra role** để quyết định điểm đến:

| Vai trò          | Điều kiện                 | Đích đến                        | Nhiệm vụ                       |
| ---------------- | ------------------------- | ------------------------------- | ------------------------------ |
| **System Admin** | `isSystemAdmin === true`  | `/administration/organizations` | Quản lý công ty, RBAC toàn sàn |
| **Org User**     | `isSystemAdmin === false` | `/onboarding/select-org`        | Chọn tổ chức để làm việc       |

### Implement:

```typescript
// authFlow.ts
export const getRedirectPath = (user: AuthUser): string => {
  const isSystemAdmin =
    user.role === "system_admin" || user.isSystemAdmin === true;

  if (isSystemAdmin) {
    return "/administration/organizations";
  }
  return "/onboarding/select-org";
};
```

**File chính:**

- [authFlow.ts](./services/authFlow.ts) - Logic phân luồng

---

## 🔵 BƯỚC 3: LỰA CHỌN TỔ CHỨC (ORG SELECTION)

**Vị trí:** `/onboarding/select-org` → `SelectOrgPage` → `SelectOrgForm`

### Quy trình:

**Chỉ dành cho Org User** (System Admin bypass bước này):

1. **Frontend gọi API** lấy danh sách Org mà User tham gia:

   ```typescript
   // Zustand Store có sẵn từ bước 1
   const { organizations } = useAuthStore();
   ```

2. **User chọn 1 Org:**

   ```typescript
   // useSelectOrg hook thực hiện:

   // 3.1: Validate orgId
   if (!orgId) throw new Error("Invalid organization");

   // 3.2: Cập nhật currentOrgId vào Zustand
   setCurrentOrgId(orgId);

   // 3.3: Lưu vào Cookie cho Middleware đọc
   document.cookie = `currentOrgId=${orgId}; path=/`;

   // 3.4: Load permissions tại org này
   const permissions = await getUserPermissions(user.id);
   setPermissions(permissions);

   // 3.5: Redirect vào Dashboard
   router.push(`/dashboard/${orgId}`);
   ```

3. **Middleware sẽ validate:**
   - Check `currentOrgId` cookie match route param
   - Check `orgId` nằm trong `userOrgIds`
   - Nếu hợp lệ → Allow access
   - Nếu không → Redirect lại `/onboarding/select-org`

**File chính:**

- [useSelectOrg.ts](./features/auth/hooks/useSelectOrg.ts) - Logic chọn org
- [SelectOrgForm.tsx](./features/auth/components/select-org/SelectOrgForm.tsx) - UI
- [SelectOrgPage](<./app/(onboarding)/select-org/page.tsx>) - Page wrapper

---

## 🟠 BƯỚC 4: DUY TRÌ PHIÊN & BẢO MẬT (PERSISTENCE & SECURITY)

**Vị trí:** Middleware + API Interceptor

### 4.1: Middleware (Next.js)

**File:** [middleware.ts](./middleware.ts)

**Chức năng:**

- ✅ Check `access_token` cookie trước khi cho phép access
- ✅ Validate `userRole` để đảm bảo route phù hợp
- ✅ Validate `currentOrgId` cookie match route param
- ✅ Validate `orgId` nằm trong `userOrgIds`

```typescript
// Pseudocode
if (!request.cookies.get("access_token")) {
  redirect("/login"); // Token hết hạn
}

if (pathname.startsWith("/administration")) {
  if (userRole !== "system_admin") {
    redirect("/onboarding/select-org"); // Không có quyền
  }
}

if (pathname.startsWith("/dashboard")) {
  const orgId = extractFromPath(pathname);
  if (currentOrgId !== orgId || !userOrgIds.includes(orgId)) {
    redirect("/onboarding/select-org"); // Org không phù hợp
  }
}
```

### 4.2: API Interceptor (Axios)

**File:** [api-client.ts](./services/api-client.ts)

**Chức năng:**

#### Request Interceptor:

```typescript
apiClient.interceptors.request.use((config) => {
  const { currentOrgId } = useAuthStore.getState();

  // 🔑 Tự động gắn X-Org-Id header
  if (currentOrgId) {
    config.headers["X-Org-Id"] = currentOrgId;
  }

  return config;
});
```

**Mọi API request sẽ tự động có:**

```
POST /api/sales/orders
Header: X-Org-Id: org1
Cookie: access_token=...; refresh_token=...;
```

#### Response Interceptor:

```typescript
// Handle 401 - Token hết hạn
if (status === 401) {
  // Gọi /auth/refresh để lấy token mới
  await axios.post("/auth/refresh", {}, { withCredentials: true });
  // Retry original request với token mới
  return apiClient(originalRequest);
}

// Handle 403 - Không có quyền
if (status === 403) {
  toast.error("Access denied");
}
```

---

## 📊 Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     🟢 BƯỚC 1: AUTHENTICATION                 │
├─────────────────────────────────────────────────────────────┤
│ User: admin@erp.com / user@erp.com / manager@erp.com        │
│ ↓                                                            │
│ Backend: Validate + Set HttpOnly Cookies                    │
│          (access_token, refresh_token)                      │
│ ↓                                                            │
│ Frontend: Store User + Orgs + Permissions in Zustand       │
│           Set Browser Cookies (userRole, userOrgIds, etc)   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                 🟡 BƯỚC 2: ROUTING DECISION                  │
├─────────────────────────────────────────────────────────────┤
│ Is isSystemAdmin === true?                                  │
│    ├─ YES → /administration/organizations (Quản lý)        │
│    └─ NO  → /onboarding/select-org (Chọn tổ chức)         │
└─────────────────────────────────────────────────────────────┘
                              ↓
        ┌─────────────────────┴─────────────────────┐
        │                                           │
     [System Admin]                           [Org User]
        │                                           │
        ↓                                           ↓
   /administration                    /onboarding/select-org
   Dashboard                          SelectOrgForm
   (Skip bước 3)                      │
                            ┌─────────────────────────────┐
                            │  🔵 BƯỚC 3: ORG SELECTION    │
                            ├─────────────────────────────┤
                            │ User chọn Org               │
                            │ setCurrentOrgId(orgId)      │
                            │ Load permissions            │
                            │ Set currentOrgId cookie     │
                            │ → /dashboard/[orgId]        │
                            └─────────────────────────────┘
                                      ↓
┌─────────────────────────────────────────────────────────────┐
│              🟠 BƯỚC 4: PERSISTENCE & SECURITY               │
├─────────────────────────────────────────────────────────────┤
│ Middleware: Validate access_token + currentOrgId + role     │
│ API Interceptor: Auto-attach X-Org-Id header                │
│ Handle 401 (refresh), 403 (permission denied)               │
└─────────────────────────────────────────────────────────────┘
                              ↓
                    🔐 USER SESSION ACTIVE
```

---

## 🧪 Testing Mock Accounts

### System Admin

```
Email: admin@erp.com
Password: admin123
Redirect: /administration/organizations
```

### Org User

```
Email: user@erp.com
Password: user123
Redirect: /onboarding/select-org
```

### Manager

```
Email: manager@erp.com
Password: manager123
Redirect: /onboarding/select-org
```

---

## 🔑 Cookie Structure

### After Login (Bước 1):

```
access_token = jwt-token-xxx
refresh_token = jwt-token-yyy
userRole = system_admin | org_user
userOrgIds = org1,org2,org3
isSystemAdmin = true | false
```

### After Select Org (Bước 3):

```
currentOrgId = org1
```

---

## 🛡️ Security Features

1. **HttpOnly Cookies:** Token không thể truy cập từ JavaScript (XSS protection)
2. **withCredentials:** Browser tự động gửi cookies trong CORS requests
3. **X-Org-Id Header:** Mọi request phải chỉ định org context
4. **Middleware Validation:** Server-side check trước khi render page
5. **Token Refresh:** Auto-refresh token khi hết hạn (401 handling)
6. **RBAC:** Permissions load per-user-per-org

---

## 📁 File Structure

```
src/
├── services/
│   ├── authFlow.ts                    # 🟡 BƯỚC 2: Routing logic
│   ├── api-client.ts                  # 🟠 BƯỚC 4: Interceptor
│   ├── mockPermissions.ts             # Permission mock data
│   └── mockOrganizations.ts           # Organization mock data
├── features/auth/
│   ├── hooks/
│   │   ├── useLogin.ts                # 🟢 BƯỚC 1: Login logic
│   │   └── useSelectOrg.ts            # 🔵 BƯỚC 3: Org selection
│   ├── components/
│   │   ├── login/LoginForm.tsx        # Login UI
│   │   └── select-org/SelectOrgForm.tsx # Org selection UI
│   ├── services/
│   │   └── authService.ts            # Backend API calls
│   └── types/
├── app/
│   ├── (auth)/login/page.tsx          # Login page
│   ├── (onboarding)/select-org/page.tsx # Org selection page
│   ├── (dashboard)/[orgId]/page.tsx   # Dashboard
│   └── administration/page.tsx        # Admin dashboard
├── store/
│   └── use-auth-store.ts              # Zustand store
├── middleware.ts                      # 🟠 BƯỚC 4: Route protection
└── types/
    ├── user.ts                        # User interface + isSystemAdmin
    ├── organization.ts                # Organization interface
    └── auth.ts                        # Auth types
```

---

## ⚠️ Chuyển từ Mock sang Backend

Khi backend sẵn sàng, thực hiện các thay đổi:

### 1. Bỏ mock data trong useLogin.ts

```typescript
// ❌ REMOVE MOCK
// const mockUsers = { ... }

// ✅ CALL REAL BACKEND
const user = await login({ email, password });
```

### 2. Backend sẽ set HttpOnly cookies

```typescript
// Backend Response Headers:
Set-Cookie: access_token=...; HttpOnly; Secure; SameSite=Strict
Set-Cookie: refresh_token=...; HttpOnly; Secure; SameSite=Strict
```

### 3. Frontend sẽ tự động nhận cookies

```typescript
// apiClient với withCredentials: true
// Browser sẽ tự động send cookies trong requests
```

---

## 🐛 Debugging Tips

1. **Check Cookies:**
   - DevTools → Application → Cookies → Check access_token, currentOrgId

2. **Check Zustand Store:**
   - DevTools → Redux → Check user, organizations, currentOrgId, permissions

3. **Check Network:**
   - DevTools → Network → Xem header X-Org-Id được gắn chưa

4. **Check Middleware:**
   - Look at request/response headers trong Network tab
   - Kiểm tra console cho redirect logs

---

## 📚 Related Files

- [useAuthStore](./store/use-auth-store.ts) - State management
- [PermissionGuard](./components/rbac/PermissionGuard.tsx) - RBAC UI control
- [middleware.ts](./middleware.ts) - Route protection
- [constants.ts](./config/constants.ts) - API endpoints & permissions

---

**Status:** ✅ Fully Implemented with Mock Data  
**Last Updated:** 2026-04-17  
**Maintainer:** ERP Platform Team
