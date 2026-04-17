import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Check if accessing org-specific routes
  const orgRouteMatch = pathname.match(/^\/([^\/]+)(\/.*)?$/);
  if (orgRouteMatch && !pathname.startsWith('/login') && !pathname.startsWith('/register') && !pathname.startsWith('/select-org')) {
    const orgId = orgRouteMatch[1];

    // Check if access token exists (HttpOnly cookie)
    const accessToken = request.cookies.get('accessToken');
    if (!accessToken) {
      return NextResponse.redirect(new URL('/login', request.url));
    }

    // For org routes, check if currentOrgId is set
    const currentOrgId = request.cookies.get('currentOrgId');
    if (!currentOrgId || currentOrgId.value !== orgId) {
      return NextResponse.redirect(new URL('/select-org', request.url));
    }
  }

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
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
};
