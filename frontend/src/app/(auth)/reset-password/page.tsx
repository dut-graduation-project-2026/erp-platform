"use client";

import React, { useState, Suspense } from "react";
import { KeyRound, Eye, EyeOff, CheckCircle2, RefreshCw, AlertTriangle, ArrowLeft } from "lucide-react";
import { resetPasswordApi } from "@/features/auth/services/authService";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useSearchParams } from "next/navigation";
import Link from "next/link";

function ResetPasswordPageContent() {
  const searchParams = useSearchParams();
  
  const token = searchParams.get("token");

  // Form states
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);

  // Toggle visibility
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!token) {
      return toast.error("Mã xác thực (Token) không hợp lệ hoặc đã thiếu.");
    }
    if (!newPassword || newPassword.length < 8) {
      return toast.error("Mật khẩu mới phải chứa ít nhất 8 ký tự.");
    }
    if (newPassword !== confirmPassword) {
      return toast.error("Mật khẩu xác nhận không khớp.");
    }

    setIsLoading(true);
    try {
      await resetPasswordApi({
        token,
        newPassword,
      });

      setIsSuccess(true);
      toast.success("Đặt lại mật khẩu thành công!");
    } catch (err: any) {
      console.error(err);
      const errorMessage = err.response?.data?.message || "Mã xác thực đã hết hạn hoặc không hợp lệ. Vui lòng yêu cầu lại liên kết.";
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="w-full max-w-[420px] bg-white border border-[#e0e0e0] rounded-[4px] p-8 shadow-[0px_4px_16px_rgba(0,0,0,0.08)] font-['Segoe_UI',_sans-serif]">
      {!token ? (
        <div className="text-center space-y-5">
          <div className="w-16 h-16 rounded-full bg-[#fff5f5] border border-[#fee2e2] flex items-center justify-center mx-auto text-[#dc3545]">
            <AlertTriangle className="w-8 h-8" />
          </div>
          
          <div className="space-y-2">
            <h2 className="text-[20px] font-bold text-[#242424]">Invalid Request</h2>
            <p className="text-[13px] text-[#898989] leading-relaxed">
              The password reset token is missing from the URL. Please verify your email link or send a new request.
            </p>
          </div>

          <div className="pt-2 flex flex-col gap-2">
            <Link href="/forgot-password" className="w-full">
              <Button className="w-full bg-[#0066cc] hover:bg-[#004499] text-white h-10 rounded-[4px] font-[600] text-[14px]">
                Request New Reset Link
              </Button>
            </Link>
            <Link 
              href="/login" 
              className="inline-flex items-center justify-center gap-1.5 text-[13px] text-[#898989] font-[600] hover:text-[#242424] mt-2"
            >
              <ArrowLeft className="w-4 h-4" />
              Back to Login
            </Link>
          </div>
        </div>
      ) : isSuccess ? (
        <div className="text-center space-y-5 animate-in fade-in duration-300">
          <div className="w-16 h-16 rounded-full bg-[#e6f4ea] border border-[#ceead6] flex items-center justify-center mx-auto text-green-700">
            <CheckCircle2 className="w-8 h-8" />
          </div>
          
          <div className="space-y-2">
            <h2 className="text-[22px] font-bold text-[#242424]">Password Reset!</h2>
            <p className="text-[13.5px] text-[#898989] leading-relaxed">
              Your password has been successfully updated. You can now log back into the ERP platform with your new credentials.
            </p>
          </div>

          <div className="pt-2">
            <Link href="/login" className="w-full">
              <Button className="w-full bg-[#0066cc] hover:bg-[#004499] text-white h-10 rounded-[4px] font-[600] text-[14px]">
                Proceed to Login
              </Button>
            </Link>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <h2 className="text-[24px] font-bold text-[#242424] tracking-tight">Reset Password</h2>
            <p className="text-[13px] text-[#898989] leading-relaxed">
              Create a new secure password for your account. Make sure it contains at least 8 characters.
            </p>
          </div>

          <hr className="border-[#e0e0e0]" />

          <div className="space-y-5">
            <div>
              <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">New Password</label>
              <div className="relative">
                <KeyRound className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
                <Input
                  type={showNew ? "text" : "password"}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  className="h-10 pl-10 pr-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                />
                <button
                  type="button"
                  onClick={() => setShowNew(!showNew)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-[#898989] hover:text-[#242424]"
                >
                  {showNew ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              <p className="text-[12px] text-[#898989] mt-1">Must be at least 8 characters long.</p>
            </div>

            <div>
              <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Confirm Password</label>
              <div className="relative">
                <KeyRound className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
                <Input
                  type={showConfirm ? "text" : "password"}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  className="h-10 pl-10 pr-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirm(!showConfirm)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-[#898989] hover:text-[#242424]"
                >
                  {showConfirm ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <Button
              type="submit"
              disabled={isLoading}
              className="w-full bg-[#0066cc] hover:bg-[#004499] text-white h-10 rounded-[4px] font-[600] text-[14px] flex items-center justify-center gap-2 transition-colors shadow-sm"
            >
              {isLoading ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" />
                  Updating password...
                </>
              ) : (
                <>
                  <CheckCircle2 className="w-4 h-4" />
                  Reset Password
                </>
              )}
            </Button>
          </div>
        </form>
      )}
    </div>
  );
}

export default function ResetPasswordPage() {
  return (
    <Suspense fallback={
      <div className="w-full max-w-[420px] bg-white border border-[#e0e0e0] rounded-[4px] p-8 shadow-[0px_4px_16px_rgba(0,0,0,0.08)] font-['Segoe_UI',_sans-serif] flex items-center justify-center min-h-[300px]">
        <div className="flex flex-col items-center gap-3">
          <RefreshCw className="w-8 h-8 text-[#0066cc] animate-spin" />
          <p className="text-[13px] text-[#898989]">Loading reset password form...</p>
        </div>
      </div>
    }>
      <ResetPasswordPageContent />
    </Suspense>
  );
}
