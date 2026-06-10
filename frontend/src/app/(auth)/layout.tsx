import Link from "next/link";
import Image from "next/image";

const AuthLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <main className="min-h-screen w-screen overflow-x-hidden">
      <div className="grid min-h-screen w-full grid-cols-1 lg:grid-cols-2">
        {/* Left Section - Brand & Hero */}
        <section className="relative hidden min-h-screen flex-col justify-between overflow-hidden bg-gradient-to-br from-blue-900 via-purple-900 to-indigo-900 px-6 py-8 text-white lg:flex lg:px-12 lg:py-16">
          <Image
            src="/auth-bg.jpg"
            alt="ERP Platform Background"
            fill
            priority
            sizes="(max-width: 1024px) 0vw, 50vw" // Sửa lỗi missing sizes
            className="object-cover" // Đảm bảo ảnh không bị méo
          />
          {/* Overlay */}
          <div
            className="absolute inset-0 bg-[rgba(17,17,17,0.65)]"
            aria-hidden="true"
          />

          {/* Header with Logo */}
          <div className="relative z-10">
            <Link
              href="/"
              className="inline-flex items-center gap-2 transition-opacity duration-200 hover:opacity-80 focus:outline-none focus-visible:ring-2 focus-visible:ring-white/50 rounded-lg px-2 py-1"
              aria-label="Home"
            >
              <span className="text-sm font-semibold text-white tracking-wide hidden sm:inline">
                ERP Platform
              </span>
            </Link>
          </div>

          {/* Content */}
          <div className="relative z-10 space-y-8 max-w-2xl">
            <div className="space-y-4">
              <p className="text-xs font-semibold uppercase tracking-widest text-white/60">
                Welcome Back
              </p>
              <h1 className="text-4xl lg:text-5xl font-bold text-white leading-tight">
                Enterprise Resource Planning Platform
              </h1>
              <p className="text-base leading-relaxed text-white/75">
                Access your unified workspace for finance, inventory, and
                operations with a consistent, data-first interface.
              </p>
            </div>
          </div>

          {/* Footer */}
          <div className="relative z-10 pt-8 border-t border-white/10">
            <p className="text-xs text-white/50">
              © 2026 ERP Platform. All rights reserved.
            </p>
          </div>
        </section>

        {/* Right Section - Form */}
        <section className="flex min-h-screen flex-col items-center justify-center bg-secondary px-6 py-8 sm:py-12 lg:px-12 lg:py-16">
          <div className="w-full max-w-sm">{children}</div>
        </section>
      </div>
    </main>
  );
};

export default AuthLayout;
