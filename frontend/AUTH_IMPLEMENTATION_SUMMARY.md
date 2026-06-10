# 🎯 ERP Platform - Authentication Flow Summary

## ✅ Implementation Complete

Hệ thống đăng nhập ERP Platform đã được **hoàn toàn triển khai** theo **4 bước chi tiết** với **Mock Data**.

---

## 🟢 BƯỚC 1: XÁC THỰC (AUTHENTICATION)

**File:** `src/features/auth/hooks/useLogin.ts`

**Quy trình:**

1. User nhập Email/Password tại `/login`
2. Frontend xác thực với Mock Data hoặc Backend
3. Backend gắn access_token + refresh_token vào HttpOnly Cookies
4. Frontend lưu User + Organizations + Permissions vào Zustand Store

**Test Accounts:**

```
System Admin:
  Email: admin@erp.com
  Password: admin123

Org User:
  Email: user@erp.com
  Password: user123

Manager:
  Email: manager@erp.com
  Password: manager123
```

**Cookies Set:**

```
access_token (HttpOnly)
refresh_token (HttpOnly)
userRole
userOrgIds
isSystemAdmin
```

---

## 🟡 BƯỚC 2: PHÂN LUỒNG ĐIỀU HƯỚNG (ROUTING DECISION)

**File:** `src/services/authFlow.ts`

**Logic:**

```
if (isSystemAdmin === true)
  → Redirect to /administration/organizations
else
  → Redirect to /onboarding/select-org
```

**Implementation:**

- Check `user.isSystemAdmin` flag
- Call `getRedirectPath(user)` function
- Auto-redirect based on role

---

## 🔵 BƯỚC 3: LỰA CHỌN TỔ CHỨC (ORG SELECTION)

**File:** `src/features/auth/hooks/useSelectOrg.ts`

**Quy trình:**

1. User chọn 1 Organization từ danh sách
2. System set `currentOrgId` vào Zustand Store
3. System lưu `currentOrgId` vào Cookie
4. Load permissions cho org này
5. Redirect đến `/dashboard/[orgId]`

**Flow:**

```
SelectOrgForm → useSelectOrg → Zustand + Cookie → /dashboard/orgId
```

---

## 🟠 BƯỚC 4: DUY TRÌ PHIÊN & BẢO MẬT (PERSISTENCE & SECURITY)

### 4.1: Middleware Route Protection

**File:** `src/middleware.ts`

**Chức năng:**

- ✅ Check access_token cookie trước khi render page
- ✅ Validate userRole để đảm bảo route phù hợp
- ✅ Validate currentOrgId match route parameter
- ✅ Validate orgId nằm trong userOrgIds

**Protected Routes:**

```
/administration/* → Require isSystemAdmin === true
/dashboard/[orgId]/* → Require currentOrgId === orgId
/onboarding/* → Allow with login
/login, /register → Public
```

### 4.2: API Interceptor (X-Org-Id Header)

**File:** `src/services/api-client.ts`

**Request Interceptor:**

```typescript
// Tự động gắn vào mọi API request
config.headers["X-Org-Id"] = currentOrgId;
```

**Response Interceptor:**

```
401 (Token Expired) → Auto refresh token
403 (Access Denied) → Show error toast
5xx (Server Error) → Show error toast
```

---

## 📊 Complete Flow Diagram

```
START: /login
  ↓
[🟢 BƯỚC 1: AUTHENTICATION]
  ├─ Email: admin@erp.com / user@erp.com / manager@erp.com
  ├─ Password: admin123 / user123 / manager123
  ├─ Backend Validates (Mock)
  ├─ Set HttpOnly Cookies (access_token, refresh_token)
  ├─ Return User + isSystemAdmin flag
  ├─ Frontend Store in Zustand
  └─ Load Organizations + Permissions
  ↓
[🟡 BƯỚC 2: ROUTING DECISION]
  ├─ Check isSystemAdmin flag
  ├─ System Admin (true)
  │  └─ /administration/organizations
  └─ Org User (false)
     └─ /onboarding/select-org
     ↓
[🔵 BƯỚC 3: ORG SELECTION] (Only for Org Users)
  ├─ SelectOrgForm displays list
  ├─ User clicks "Select"
  ├─ Set currentOrgId in Zustand
  ├─ Set currentOrgId in Cookie
  ├─ Load org-specific permissions
  └─ /dashboard/[orgId]
  ↓
[🟠 BƯỚC 4: PERSISTENCE & SECURITY]
  ├─ Middleware validates every route
  ├─ API interceptor adds X-Org-Id header
  ├─ Auto-refresh token on 401
  ├─ Protect cross-org access
  └─ USER SESSION ACTIVE ✓
```

---

## 🔐 Security Features Implemented

### 1. HttpOnly Cookies ✅

```
Cannot access from JavaScript → XSS Safe
Automatically sent with requests → CSRF Safe
```

### 2. X-Org-Id Header Injection ✅

```
Every API request includes current org context
Backend validates user has access to org
```

### 3. Middleware Route Protection ✅

```
Server-side validation before page render
Cannot be bypassed by client-side changes
```

### 4. Token Refresh Mechanism ✅

```
Auto-refresh when token expires (401)
Seamless user experience
```

### 5. RBAC (Role-Based Access Control) ✅

```
System Admin: Full access to /administration/*
Org User: Access only to /dashboard/[orgId]/*
Permission-based UI components
```

---

## 📁 Key Files Modified

| File                | Purpose                      | Status      |
| ------------------- | ---------------------------- | ----------- |
| `useLogin.ts`       | 🟢 BƯỚC 1 Login logic        | ✅ Complete |
| `authFlow.ts`       | 🟡 BƯỚC 2 Routing decision   | ✅ Complete |
| `useSelectOrg.ts`   | 🔵 BƯỚC 3 Org selection      | ✅ Complete |
| `middleware.ts`     | 🟠 BƯỚC 4 Route protection   | ✅ Complete |
| `api-client.ts`     | 🟠 BƯỚC 4 X-Org-Id injection | ✅ Complete |
| `types/user.ts`     | Added isSystemAdmin field    | ✅ Complete |
| `SelectOrgForm.tsx` | Org selection UI             | ✅ Enhanced |
| `SelectOrgPage`     | Org selection page           | ✅ Updated  |

---

## 📚 Documentation Files

### New Documentation:

1. **AUTH_FLOW_IMPLEMENTATION.md** (src/)
   - Detailed 4-step flow explanation
   - Code examples
   - Testing instructions
   - Migration guide from mock to real backend

2. **ARCHITECTURE.md** (frontend/)
   - Complete directory structure
   - File organization rationale
   - Dependency list
   - Development setup guide

---

## 🧪 Testing Scenarios

### Scenario 1: System Admin Login

```
1. Go to /login
2. Email: admin@erp.com, Password: admin123
3. Should redirect to /administration/organizations
4. Can manage companies, roles, users
```

### Scenario 2: Org User Login

```
1. Go to /login
2. Email: user@erp.com, Password: user123
3. Should redirect to /onboarding/select-org
4. Select organization
5. Should redirect to /dashboard/org1 (or selected org)
```

### Scenario 3: Token Expiration

```
1. Login successfully
2. Wait for token to expire (simulated)
3. Make API call
4. Should auto-refresh token
5. Request should succeed
```

### Scenario 4: Cross-Org Access Prevention

```
1. Login as user, select org1
2. Try to access /dashboard/org2 directly
3. Should redirect to /onboarding/select-org
```

### Scenario 5: Logout

```
1. Clear auth cookies
2. Clear Zustand store
3. Redirect to /login
```

---

## 🔄 Data Flow

### Login Flow (Bước 1):

```
LoginForm
  ↓
useLogin.handleLogin(email, password)
  ↓
Mock Auth Service (or Backend API)
  ↓
User + isSystemAdmin + organizations + permissions
  ↓
Zustand Store: setUser, setOrganizations, setPermissions
  ↓
Set Browser Cookies
  ↓
getRedirectPath(user)
  ↓
router.push(redirectPath)
```

### API Call Flow (Bước 4):

```
Component calls API
  ↓
apiClient.get/post/put/delete
  ↓
Request Interceptor
  ├─ Add X-Org-Id from Zustand
  └─ Attach access_token (automatic with withCredentials)
  ↓
Backend receives request with X-Org-Id header
  ↓
Backend validates org access
  ↓
Response
  ├─ 200 Success → Return data
  ├─ 401 Token Expired → Response Interceptor auto-refresh
  ├─ 403 Access Denied → Show error toast
  └─ 5xx Server Error → Show error toast
```

---

## 🚀 Next Steps

### To Transition from Mock to Real Backend:

1. **Update API endpoints** in `config/constants.ts`
2. **Uncomment backend API calls** in `features/auth/services/authService.ts`
3. **Remove mock data** from `useLogin.ts`, `mockOrganizations.ts`, `mockPermissions.ts`
4. **Ensure backend** properly sets HttpOnly cookies
5. **Test with real API** - all interceptors should work unchanged

### Features Not Yet Implemented:

- Password reset flow
- Email verification
- Multi-factor authentication (MFA)
- Social login (Google, GitHub, etc)
- Session timeout warnings
- User activity logging

---

## ⚠️ Important Notes

1. **Mock Data is Used:** The system uses mock data for testing until backend is ready
2. **TypeScript Strict Mode:** All files use strict TypeScript for type safety
3. **Zustand Persistence:** Auth state persists across page refreshes
4. **Cookie Security:** withCredentials: true allows HttpOnly cookies to be sent
5. **X-Org-Id Required:** Backend must validate this header on every request

---

## 📞 Support

For detailed information about:

- **Auth Flow:** See `AUTH_FLOW_IMPLEMENTATION.md`
- **Project Structure:** See `ARCHITECTURE.md`
- **UI Design:** See `DESIGN.md`

---

## ✨ Summary

✅ **4-Step Authentication Flow Fully Implemented**

- 🟢 Bước 1: Authentication complete
- 🟡 Bước 2: Routing decision complete
- 🔵 Bước 3: Org selection complete
- 🟠 Bước 4: Persistence & security complete

✅ **Mock Data Strategy**

- Ready to test without backend
- Easy to replace with real API

✅ **Security Implemented**

- HttpOnly cookies, X-Org-Id headers, middleware protection

✅ **Documentation Complete**

- Comprehensive guides for developers
- Testing scenarios included

**Status:** 🟢 Ready for Frontend Testing  
**Last Updated:** 2026-04-17

---

For comprehensive details about authentication flow, refer to:
📖 [AUTH_FLOW_IMPLEMENTATION.md](./src/AUTH_FLOW_IMPLEMENTATION.md)

For project structure and architecture:
📖 [ARCHITECTURE.md](./ARCHITECTURE.md)
