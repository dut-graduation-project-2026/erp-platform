import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

/**
 * Middleware for Authentication & Authorization
 * 🟠 BƯỚC 4: DUY TRÌ PHIÊN & BẢO MẬT (MIDDLEWARE)
 * 
 * Chức năng:
 * 1. Check access_token cookie trước khi cho phép access
 * 2. Validate userRole cookie để đảm bảo đúng route cho role
 * 3. Validate currentOrgId cho org-specific routes
 * 4. Validate orgId nằm trong userOrgIds của user
 */
export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const isInvitationRoute = /^\/organizations\/[^\/]+\/invitations\/[^\/]+$/.test(pathname);

  // Public routes - không cần auth
  if (
    pathname.startsWith('/login') ||
    pathname.startsWith('/register') ||
    pathname.startsWith('/forgot-password') ||
    pathname.startsWith('/reset-password') ||
    isInvitationRoute
  ) {
    return NextResponse.next();
  }

  // Middleware không nên chặn quá sớm khi access_token vừa hết hạn/mất,
  // để frontend có cơ hội gọi /auth/refresh và khôi phục phiên.
  const clientSession = request.cookies.get('clientSession')?.value;
  const hasClientSessionContext = clientSession === 'true';
  const userOrgIdsCookie = request.cookies.get('userOrgIds')?.value;

  // Nếu không còn ngữ cảnh phiên tối thiểu từ client thì coi như chưa đăng nhập.
  if (!hasClientSessionContext) {
    // Tránh loop vô tận nếu đã ở /login
    if (pathname === '/') {
      return NextResponse.redirect(new URL('/login', request.nextUrl));
    }
    const loginUrl = new URL('/login', request.nextUrl);
    loginUrl.searchParams.set('redirect', pathname + request.nextUrl.search);
    return NextResponse.redirect(loginUrl);
  }

  // Nếu đã đăng nhập và truy cập root '/', chuyển hướng vào luồng hệ thống
  if (pathname === '/') {
    return NextResponse.redirect(new URL('/select-org', request.nextUrl));
  }

  // 🟠 BƯỚC 4.3: Onboarding routes - /select-org
  if (pathname.startsWith('/select-org')) {
    return NextResponse.next();
  }

  // 🟠 BƯỚC 4.4: Dashboard routes - /dashboard/[orgId]/...
  if (pathname.startsWith('/dashboard')) {
    const orgRouteMatch = pathname.match(/^\/dashboard\/([^\/]+)(\/.*)?$/);
    
    if (orgRouteMatch) {
      const orgId = orgRouteMatch[1];
      
      // Check nếu currentOrgId cookie match với route param
      const currentOrgId = request.cookies.get('currentOrgId');
      if (!currentOrgId || currentOrgId.value !== orgId) {
        return NextResponse.redirect(new URL('/select-org', request.nextUrl));
      }

      // Check nếu orgId nằm trong userOrgIds của user
      const userOrgIds = userOrgIdsCookie?.split('_') || [];
      if (!userOrgIds.includes(orgId)) {
        return NextResponse.redirect(new URL('/select-org', request.nextUrl));
      }
      
      return NextResponse.next();
    }
  }

  // Default: allow access if token exists
  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public assets (png, jpg, svg, etc)
     */
    '/((?!api|_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
  ],
};
