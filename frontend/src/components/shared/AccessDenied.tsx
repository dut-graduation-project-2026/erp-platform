'use client';

import { useRouter, useParams } from 'next/navigation';
import { ShieldAlert, ArrowLeft, Home } from 'lucide-react';

interface AccessDeniedProps {
  title?: string;
  description?: string;
}

export function AccessDenied({ 
  title = 'Access Denied', 
  description = 'Tài khoản của bạn không được cấp quyền để truy cập vào phân hệ hoặc chức năng này. Vui lòng liên hệ quản trị viên để biết thêm chi tiết.'
}: AccessDeniedProps) {
  const router = useRouter();
  const params = useParams();
  const orgId = params.orgId as string;

  return (
    <div className="absolute inset-0 z-50 flex items-center justify-center p-6 bg-gradient-to-br from-[#f8fafc] to-[#f1f5f9] font-['Segoe_UI']">
      <div className="max-w-md w-full bg-white rounded-2xl border border-[#e2e8f0] p-8 shadow-[0_8px_30px_rgba(0,0,0,0.04)] text-center transition-all duration-300 hover:shadow-[0_12px_40px_rgba(0,0,0,0.06)]">
        {/* Animated warning icon */}
        <div className="mx-auto w-16 h-16 bg-[#fef2f2] rounded-2xl flex items-center justify-center mb-6 text-[#ef4444] border border-[#fee2e2] animate-pulse">
          <ShieldAlert className="w-8 h-8" />
        </div>

        {/* Title */}
        <h2 className="text-lg font-bold text-[#1e293b] mb-3 tracking-tight">
          {title}
        </h2>

        {/* Description */}
        <p className="text-sm text-[#64748b] leading-relaxed mb-8">
          {description}
        </p>

        {/* Buttons */}
        <div className="flex flex-col sm:flex-row gap-3 justify-center">
          <button
            onClick={() => router.back()}
            className="flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl border border-[#e2e8f0] text-sm font-semibold text-[#475569] hover:bg-[#f8fafc] active:bg-[#f1f5f9] transition-all duration-200 cursor-pointer shadow-sm"
          >
            <ArrowLeft className="w-4 h-4" />
            Quay lại
          </button>
          
          <button
            onClick={() => router.push(`/dashboard/${orgId}`)}
            className="flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-[#0066cc] text-sm font-semibold text-white hover:bg-[#0052a3] active:bg-[#003d7a] transition-all duration-200 cursor-pointer shadow-[0_2px_10px_rgba(0,102,204,0.15)]"
          >
            <Home className="w-4 h-4" />
            Về Trang Chủ
          </button>
        </div>
      </div>
    </div>
  );
}
