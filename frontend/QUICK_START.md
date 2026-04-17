# 🚀 Quick Start Guide - ERP Platform Frontend

## 📋 Prerequisites

```bash
# Required:
- Node.js 18+
- pnpm (or npm/yarn)
- Git
```

## ⚡ Setup (5 Minutes)

```bash
# 1. Install dependencies
cd frontend
pnpm install

# 2. Start development server
pnpm dev

# 3. Open browser
open http://localhost:3000
```

---

## 🔐 Login with Test Accounts

### Option 1: System Admin

```
URL: http://localhost:3000/login
Email: admin@erp.com
Password: admin123

✓ Redirects to: /administration/organizations
✓ Full system access
```

### Option 2: Org User

```
URL: http://localhost:3000/login
Email: user@erp.com
Password: user123

✓ Redirects to: /onboarding/select-org
✓ Then select organization → /dashboard/org1
```

### Option 3: Manager

```
URL: http://localhost:3000/login
Email: manager@erp.com
Password: manager123

✓ Same as Org User
✓ Different permissions
```

---

## 🔍 Verify Implementation

### Check 1: Browser Cookies

```
1. Open DevTools (F12)
2. Go to Application → Cookies
3. Check for: access_token, currentOrgId, userRole

Expected:
✓ access_token = "mock-jwt-token-..."
✓ userRole = "system_admin" or "org_user"
✓ userOrgIds = "org1,org2"
```

### Check 2: Zustand Store

```
1. Open DevTools → Redux (if extension installed)
2. Look for useAuthStore

Expected state:
{
  user: { id, firstName, lastName, email, role, isSystemAdmin },
  organizations: [ { id, name, description, role, permissions } ],
  permissions: [ "sales:read", "inventory:write", ... ],
  currentOrgId: "org1"
}
```

### Check 3: Network Requests

```
1. DevTools → Network tab
2. Look for API requests
3. Check headers for X-Org-Id

Expected:
✓ GET /api/sales/orders
  Header: X-Org-Id: org1
```

---

## 🧪 Test Scenarios

### Scenario: System Admin Flow

```
1. Go to http://localhost:3000/login
2. Login: admin@erp.com / admin123
3. Auto-redirect to /administration/organizations
4. See organization management dashboard
5. Can add/edit/delete organizations
6. Can manage roles and users
7. Can define permissions matrix
```

### Scenario: Org User Flow

```
1. Go to http://localhost:3000/login
2. Login: user@erp.com / user123
3. Auto-redirect to /onboarding/select-org
4. Click "Select" on an organization
5. Auto-redirect to /dashboard/org1
6. See org-specific dashboard
7. Can access only assigned modules
```

### Scenario: Logout & Redirect

```
1. Click logout button (if implemented)
2. Cookies cleared
3. Zustand store cleared
4. Auto-redirect to /login
```

### Scenario: Token Refresh

```
1. Login successfully
2. Keep page open for >5 minutes (simulate token expiration)
3. Click any action that requires API call
4. Should auto-refresh token (401 handling)
5. Request should succeed
```

---

## 📊 File Structure Overview

```
frontend/src/
├── features/auth/
│   ├── hooks/
│   │   ├── useLogin.ts (🟢 BƯỚC 1: Authentication)
│   │   └── useSelectOrg.ts (🔵 BƯỚC 3: Org Selection)
│   └── components/
│       ├── login/LoginForm.tsx
│       └── select-org/SelectOrgForm.tsx
├── services/
│   ├── authFlow.ts (🟡 BƯỚC 2: Routing)
│   └── api-client.ts (🟠 BƯỚC 4: Interceptor)
├── store/
│   └── use-auth-store.ts (Zustand)
├── middleware.ts (🟠 BƯỚC 4: Route Protection)
└── types/
    └── user.ts (includes isSystemAdmin)
```

---

## 🛠️ Development Commands

```bash
# Start dev server
pnpm dev

# Build for production
pnpm build

# Run production build
pnpm start

# Lint code
pnpm lint

# Type check
pnpm tsc --noEmit

# Format code
pnpm format
```

---

## 🔧 Configuration Files

### Environment Variables

**File:** `frontend/.env.local`

```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

### API Endpoints

**File:** `src/config/constants.ts`

```typescript
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: "/auth/login",
    ORGANIZATIONS: "/auth/organizations",
    ...
  },
  ...
}
```

---

## 📈 Feature Flags & Toggles

### Mock Data Status

**File:** `src/features/auth/hooks/useLogin.ts` (Line 45)

Currently: ✅ Using Mock Data

- Remove mock data object when backend ready
- Uncomment backend API call

### Log Level

**File:** `src/config/env.ts`

Set DEBUG=true for verbose logging during development

---

## 🐛 Common Issues & Solutions

### Issue: "Can't login"

```
Solution:
1. Check email is exact: admin@erp.com (case-sensitive)
2. Check password is exact: admin123
3. Check useLogin.ts has mock user data
```

### Issue: "Stuck on login page after click"

```
Solution:
1. Check DevTools Console for errors
2. Check middleware.ts is not blocking
3. Check if currentOrgId cookie is set after select org
```

### Issue: "API requests failing"

```
Solution:
1. Check X-Org-Id header is present (Network tab)
2. Check currentOrgId is set in Zustand
3. Check backend mock service is available
```

### Issue: "Token not refreshing"

```
Solution:
1. Check refresh_token cookie exists
2. Check /auth/refresh endpoint is available
3. Check token refresh logic in api-client.ts
```

---

## 🎨 Design System

### Color Scheme

```
Primary: #0099ff (Blue)
Secondary: #F5F5F5 (Light Gray)
Danger: #FF4444 (Red)
Success: #44AA44 (Green)
```

### Typography

```
Headings: font-semibold, tracking-normal
Body: font-normal, text-[14px]
Captions: text-[12px], text-muted-foreground
```

### Spacing

```
xs: 0.25rem
sm: 0.5rem
md: 1rem
lg: 1.5rem
xl: 2rem
```

---

## 📚 Documentation

### Detailed Guides:

1. **[AUTH_FLOW_IMPLEMENTATION.md](./src/AUTH_FLOW_IMPLEMENTATION.md)**
   - Complete 4-step flow explanation
   - Code examples
   - Security features

2. **[ARCHITECTURE.md](./ARCHITECTURE.md)**
   - Full directory structure
   - File organization
   - Dependencies

3. **[DESIGN.md](./DESIGN.md)**
   - UI/UX specifications
   - Component guidelines
   - Responsive design

---

## 🔄 Backend Integration

### When Backend is Ready:

**Step 1:** Update API Base URL

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

**Step 2:** Remove Mock Data

```typescript
// DELETE from useLogin.ts
const mockUsers = { ... };
```

**Step 3:** Uncomment Backend Call

```typescript
// UNCOMMENT in useLogin.ts
const user = await login({ email, password });
```

**Step 4:** Test Everything

```bash
pnpm dev
# Test all scenarios with real backend
```

---

## 🚨 Security Checklist

- ✅ HttpOnly Cookies (access_token, refresh_token)
- ✅ X-Org-Id header on all API requests
- ✅ Middleware route protection
- ✅ Zustand state persistence
- ✅ Auto token refresh on 401
- ✅ RBAC permission checking
- ✅ Cross-org access prevention

---

## 📞 Need Help?

1. Check **AUTH_FLOW_IMPLEMENTATION.md** for detailed flow
2. Check **ARCHITECTURE.md** for file organization
3. Look at test accounts above
4. Check browser DevTools console for errors
5. Check Network tab for API requests

---

## ✨ What's Implemented

### Authentication (Bước 1) ✅

- Login form with email/password
- Mock user data
- User state storage

### Routing Decision (Bước 2) ✅

- System Admin → /administration
- Org User → /onboarding/select-org

### Org Selection (Bước 3) ✅

- Organization list display
- Selection logic
- Dashboard redirect

### Persistence & Security (Bước 4) ✅

- Middleware route protection
- X-Org-Id header injection
- Auto token refresh
- Permission checking

---

## 🎯 Next Steps

1. **Run the app:** `pnpm dev`
2. **Test login:** Use test accounts above
3. **Check DevTools:** Verify cookies & store
4. **Review code:** Read AUTH_FLOW_IMPLEMENTATION.md
5. **Understand flow:** Follow 4-step diagram
6. **Integrate backend:** When API ready

---

**Status:** 🟢 Ready to Use  
**Last Updated:** 2026-04-17  
**Support:** See documentation files above
