# ERP Platform Frontend Architecture & Design

## 📋 Project Overview

**System:** Enterprise Resource Planning (ERP) Platform cho SMEs  
**Frontend:** Next.js 14+ (App Router) + TypeScript + Tailwind CSS + Shadcn/UI  
**State Management:** Zustand + Persist Middleware  
**Backend Integration:** Mock Data → Eventually Spring Boot API

**Key Features:**

- ✅ Multi-organization support (SaaS Model)
- ✅ Role-Based Access Control (RBAC) - Dynamic
- ✅ Real-time permission checking
- ✅ HttpOnly Cookie-based Authentication
- ✅ Automatic X-Org-Id header injection for API calls
- ✅ Sales, Inventory, Finance, HR, Blockchain modules

---

## 🏗️ Directory Structure (Feature-Based Architecture)

```
frontend/
├── src/
│   ├── app/                           # Next.js App Router
│   │   ├── (auth)/                    # Auth layout group
│   │   │   ├── layout.tsx             # Auth layout (2-column design)
│   │   │   ├── login/
│   │   │   │   └── page.tsx           # 🟢 BƯỚC 1: Login Page
│   │   │   └── register/
│   │   │       └── page.tsx           # Register page
│   │   │
│   │   ├── (onboarding)/              # Onboarding layout group
│   │   │   └── select-org/
│   │   │       └── page.tsx           # 🔵 BƯỚC 3: Org Selection
│   │   │
│   │   ├── (dashboard)/               # Dashboard layout group
│   │   │   └── [orgId]/
│   │   │       ├── layout.tsx         # Dashboard layout
│   │   │       └── page.tsx           # Dashboard home (per-org)
│   │   │
│   │   ├── administration/            # System Admin section
│   │   │   └── page.tsx               # Admin dashboard
│   │   │
│   │   ├── api/                       # API routes
│   │   │   └── login/                 # Mock login endpoint
│   │   │
│   │   ├── layout.tsx                 # Root layout
│   │   ├── page.tsx                   # Root page
│   │   └── globals.css                # Global styles + Tailwind
│   │
│   ├── features/                      # Feature-based modules 🎯 MAIN LOGIC
│   │   ├── auth/                      # 🟢 🟡 Authentication & routing
│   │   │   ├── components/
│   │   │   │   ├── login/
│   │   │   │   │   └── LoginForm.tsx  # Login form UI
│   │   │   │   └── select-org/
│   │   │   │       └── SelectOrgForm.tsx # Org selection UI
│   │   │   ├── hooks/
│   │   │   │   ├── useLogin.ts        # 🟢 BƯỚC 1 logic
│   │   │   │   └── useSelectOrg.ts    # 🔵 BƯỚC 3 logic
│   │   │   ├── services/
│   │   │   │   └── authService.ts     # Backend API calls
│   │   │   ├── types/                 # Auth-specific types
│   │   │   └── utils/
│   │   │
│   │   ├── administration/            # 👨‍💼 System Admin features
│   │   │   ├── components/
│   │   │   │   ├── DefineRole.tsx     # Role matrix editor
│   │   │   │   ├── OrganizationForm.tsx
│   │   │   │   ├── OrganizationList.tsx
│   │   │   │   ├── RoleMatrix.tsx     # Permission matrix
│   │   │   │   ├── UserForm.tsx
│   │   │   │   └── UserList.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useOrganizations.ts
│   │   │   │   ├── useRoles.ts
│   │   │   │   └── useUsers.ts
│   │   │   ├── services/              # Admin API calls
│   │   │   ├── types/
│   │   │   │   └── index.ts
│   │   │   └── mock-data.ts           # Admin mock data
│   │   │
│   │   ├── sales/                     # 💰 Sales & CRM
│   │   │   ├── components/            # Sales components
│   │   │   ├── hooks/                 # Sales hooks
│   │   │   ├── services/              # Sales API
│   │   │   ├── types/                 # Sales types
│   │   │   └── utils/
│   │   │
│   │   ├── inventory/                 # 📦 Inventory & Supply Chain
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   ├── services/
│   │   │   ├── types/
│   │   │   └── utils/
│   │   │
│   │   └── blockchain/                # ⛓️ Audit & Traceability (MVP+)
│   │       ├── components/
│   │       ├── hooks/
│   │       ├── services/
│   │       ├── types/
│   │       └── utils/
│   │
│   ├── components/                    # Shared UI components
│   │   ├── rbac/
│   │   │   └── PermissionGuard.tsx    # RBAC component wrapper
│   │   ├── ui/                        # Shadcn/UI components
│   │   │   ├── badge.tsx
│   │   │   ├── button.tsx
│   │   │   ├── card.tsx
│   │   │   ├── checkbox.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── input.tsx
│   │   │   ├── label.tsx
│   │   │   ├── select.tsx
│   │   │   ├── table.tsx
│   │   │   ├── tabs.tsx
│   │   │   ├── textarea.tsx
│   │   │   └── sonner.tsx             # Toast notifications
│   │   └── shared/                    # Shared layout components
│   │       ├── Navbar/
│   │       └── Sidebar/
│   │
│   ├── config/                        # Configuration
│   │   ├── env.ts                     # Environment variables
│   │   ├── constants.ts               # API endpoints, permissions, etc
│   │   ├── site.ts                    # Site metadata
│   │   └── axios.ts                   # Axios config
│   │
│   ├── services/                      # Global services (API integration)
│   │   ├── api-client.ts              # 🟠 BƯỚC 4: Axios instance + interceptors
│   │   ├── authFlow.ts                # 🟡 BƯỚC 2: Routing logic
│   │   ├── authService.ts             # Backend auth API calls
│   │   ├── org-service.ts             # Organization API
│   │   ├── mockPermissions.ts         # Mock RBAC data
│   │   ├── mockOrganizations.ts       # Mock org data
│   │   └── apiRequest.ts              # Utility function
│   │
│   ├── store/                         # Zustand store
│   │   └── use-auth-store.ts          # Auth state (user, orgs, permissions, currentOrgId)
│   │
│   ├── types/                         # Global TypeScript types
│   │   ├── auth.ts                    # Auth types
│   │   ├── base.ts                    # Base entity types
│   │   ├── common.ts                  # Common types
│   │   ├── organization.ts            # Organization types
│   │   └── user.ts                    # User types (with isSystemAdmin)
│   │
│   ├── hooks/                         # Global custom hooks
│   │   ├── use-debounce.ts
│   │   ├── use-permissions.ts         # Check permissions
│   │   └── useToast.ts                # Toast notification hook
│   │
│   ├── lib/                           # Utilities
│   │   ├── blockchain-provider.ts
│   │   └── utils.ts
│   │
│   ├── utils/                         # Utility functions
│   │   └── apiRequest.ts
│   │
│   ├── middleware.ts                  # 🟠 BƯỚC 4: Route protection middleware
│   ├── AUTH_FLOW_IMPLEMENTATION.md    # This file - complete flow documentation
│   └── contexts/
│       └── AuthContext.tsx            # Auth context (legacy, consider removing)
│
├── public/                            # Static assets
│   └── auth-bg.jpg                    # Login page background
│
├── node_modules/                      # Dependencies
├── .next/                             # Build output
├── .gitignore
├── package.json                       # Dependencies
├── tsconfig.json                      # TypeScript config
├── next.config.ts                     # Next.js config
├── tailwind.config.ts                 # Tailwind CSS config
├── postcss.config.mjs                 # PostCSS config
├── eslint.config.mjs                  # ESLint config
├── components.json                    # Shadcn/UI config
├── DESIGN.md                          # UI/UX design specs
├── README.md                          # Project README
└── pnpm-lock.yaml                     # Dependency lock file
```

---

## 🔐 Authentication Flow Summary

### Quick Reference

| Step | Name             | File                              | Purpose                                |
| ---- | ---------------- | --------------------------------- | -------------------------------------- |
| 🟢   | Authentication   | `useLogin.ts`                     | Verify email/password, load user data  |
| 🟡   | Routing Decision | `authFlow.ts`                     | Determine redirect path based on role  |
| 🔵   | Org Selection    | `useSelectOrg.ts`                 | Select org to work with, set context   |
| 🟠   | Persistence      | `middleware.ts` + `api-client.ts` | Protect routes, inject X-Org-Id header |

### Test Accounts (Mock Data)

```
🔑 System Admin
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Email: admin@erp.com
Password: admin123
Role: system_admin (isSystemAdmin = true)
Redirect: /administration/organizations
Permissions: Full access to system administration

🔑 Org User #1
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Email: user@erp.com
Password: user123
Role: org_user (isSystemAdmin = false)
Redirect: /onboarding/select-org
Permissions: Based on selected org

🔑 Manager
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Email: manager@erp.com
Password: manager123
Role: org_user (isSystemAdmin = false)
Redirect: /onboarding/select-org
Permissions: Based on selected org
```

---

## 📊 State Management (Zustand)

### useAuthStore

```typescript
{
  // User information
  user: {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    role: 'system_admin' | 'org_user';
    isSystemAdmin: boolean; // 🟡 Used for routing decision
  };

  // Organization list this user has access to
  organizations: {
    id: string;
    name: string;
    description: string;
    role: 'admin' | 'user';
    permissions: string[];
  }[];

  // Permissions for current org/user
  permissions: string[]; // e.g., ['sales:read', 'inventory:write']

  // Currently selected organization
  currentOrgId: string | null;

  // Setters
  setUser(user)
  setOrganizations(orgs)
  setPermissions(perms)
  setCurrentOrgId(orgId)
  clearAuth() // Logout
}
```

---

## 🔒 Security Mechanisms

### 1. HttpOnly Cookies (Backend-Set)

```
Set-Cookie: access_token=...; HttpOnly; Secure; SameSite=Strict
Set-Cookie: refresh_token=...; HttpOnly; Secure; SameSite=Strict
```

- ✅ Cannot be accessed from JavaScript (XSS safe)
- ✅ Automatically sent with every request (CSRF safe with SameSite)
- ✅ Backend validates on every request

### 2. Request Interceptor (X-Org-Id Header)

```typescript
// Every API request automatically includes:
config.headers["X-Org-Id"] = currentOrgId;
```

- ✅ Backend validates user has access to this org
- ✅ Prevents cross-org data access

### 3. Middleware Route Protection

```typescript
// Validate before rendering page
- Check access_token exists
- Check userRole matches route
- Check currentOrgId matches param
```

- ✅ Server-side validation (cannot be bypassed)
- ✅ Prevents unauthorized access to pages

### 4. Response Interceptor (Auto Refresh)

```typescript
if (status === 401) {
  // Refresh token using refresh_token cookie
  // Retry original request
}
```

- ✅ Seamless token refresh
- ✅ User doesn't notice token expiration

---

## 🎯 Mock Data Strategy

### Why Mock Data?

- Backend may not be ready when frontend development starts
- Frontend can progress independently
- Easy to test UI without API dependency
- Can be quickly replaced with real API calls

### Mock Data Locations

1. **Users:** `useLogin.ts` hardcoded objects
2. **Organizations:** `services/mockOrganizations.ts`
3. **Permissions:** `services/mockPermissions.ts`
4. **Admin data:** `features/administration/mock-data.ts`

### Transition to Real Backend

**Step 1:** Uncomment backend API call

```typescript
// ✅ CHANGE THIS
const user = await login({ email, password }); // Backend call
```

**Step 2:** Remove mock data

```typescript
// ❌ DELETE THIS
const mockUsers = { ... };
```

**Step 3:** Ensure backend sets cookies properly

```
Backend should set: access_token, refresh_token (HttpOnly)
Frontend will automatically send them
```

---

## 📦 Key Dependencies

| Package        | Purpose                   | Version |
| -------------- | ------------------------- | ------- |
| `next`         | React framework + routing | 14+     |
| `react`        | UI library                | 19+     |
| `typescript`   | Type safety               | 5+      |
| `tailwindcss`  | Utility CSS framework     | 4+      |
| `shadcn/ui`    | Component library         | Latest  |
| `zustand`      | State management          | Latest  |
| `axios`        | HTTP client               | Latest  |
| `sonner`       | Toast notifications       | Latest  |
| `lucide-react` | Icons                     | Latest  |

---

## 🚀 Deployment Checklist

- [ ] Remove mock data / enable backend API calls
- [ ] Set `env.API_BASE_URL` to production backend
- [ ] Ensure `withCredentials: true` in axios config
- [ ] Test HttpOnly cookies are being set
- [ ] Test X-Org-Id header is included in requests
- [ ] Test middleware route protection
- [ ] Test token refresh flow
- [ ] Test logout clears auth store & cookies
- [ ] Set proper CORS headers on backend

---

## 📚 Related Documentation

- [AUTH_FLOW_IMPLEMENTATION.md](./AUTH_FLOW_IMPLEMENTATION.md) - Detailed 4-step auth flow
- [DESIGN.md](../DESIGN.md) - UI/UX design specifications
- [Backend Documentation](#) - Spring Boot API specs
- [RBAC Configuration](#) - Permission matrix setup

---

## 🤝 Contributing

When adding new features:

1. **Follow feature-based structure** - Create feature folder with components/hooks/services/types
2. **Add proper TypeScript types** - No `any` types
3. **Use Zustand for state** - Not Context API or Redux
4. **Check permissions** - Use `<PermissionGuard>` wrapper
5. **Handle errors** - Use toast notifications
6. **Test with mock data** - Before backend integration
7. **Document changes** - Update this file if needed

---

## 🔧 Development Setup

```bash
# Install dependencies
pnpm install

# Start dev server
pnpm dev

# Open browser
open http://localhost:3000

# Login with test account
# Email: admin@erp.com / user@erp.com
# Password: admin123 / user123

# Check browser DevTools
# → Application → Cookies (check for access_token, currentOrgId)
# → Redux (check Zustand store)
# → Network (check X-Org-Id header)
```

---

## 🐛 Troubleshooting

**Problem:** Can't login

- Solution: Check mock user exists in useLogin.ts

**Problem:** After login, stuck on login page

- Solution: Check middleware.ts - might be redirecting due to missing cookies

**Problem:** 403 error on API calls

- Solution: Check X-Org-Id header is being sent (Network tab)

**Problem:** currentOrgId not persisting

- Solution: Check Zustand persist middleware is working

**Problem:** Token not refreshing automatically

- Solution: Check refresh_token cookie exists, and /auth/refresh endpoint works

---

## 📞 Support

For questions about this architecture or implementation details, refer to:

- [AUTH_FLOW_IMPLEMENTATION.md](./AUTH_FLOW_IMPLEMENTATION.md)
- Project team documentation
- Backend API documentation

---

**Status:** ✅ Fully Implemented (Mock Data)  
**Last Updated:** 2026-04-17  
**Architecture Pattern:** Feature-Based + MVC  
**State Management:** Zustand + Persist  
**Type Safety:** Full TypeScript
