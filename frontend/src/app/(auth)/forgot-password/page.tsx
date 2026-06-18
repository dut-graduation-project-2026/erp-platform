"use client";

import React, { useState } from "react";
import { Mail, ArrowLeft, Send, CheckCircle2, RefreshCw } from "lucide-react";
import { forgotPasswordApi } from "@/features/auth/services/authService";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Link from "next/link";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSent, setIsSent] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) {
      return toast.error("Please enter your email address.");
    }

    setIsLoading(true);
    try {
      await forgotPasswordApi({ email: email.trim() });
      setIsSent(true);
      toast.success("Reset password request has been sent!");
    } catch (err: any) {
      console.error(err);
      const errorMessage = err.response?.data?.message || "Failed to send reset password request. Please check your email.";
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="w-full max-w-[420px] bg-white border border-[#e0e0e0] rounded-[4px] p-8 shadow-[0px_4px_16px_rgba(0,0,0,0.08)] font-['Segoe_UI',_sans-serif]">
      {isSent ? (
        <div className="text-center space-y-5 animate-in fade-in duration-300">
          <div className="w-16 h-16 rounded-full bg-[#f0f4ff] border border-[#dbeafe] flex items-center justify-center mx-auto text-[#0066cc]">
            <CheckCircle2 className="w-8 h-8" />
          </div>
          
          <div className="space-y-2">
            <h2 className="text-[22px] font-bold text-[#242424]">Check your inbox</h2>
            <p className="text-[13.5px] text-[#898989] leading-relaxed">
              We have sent a secure password reset link to <strong className="text-[#242424]">{email}</strong>. Please click the link inside the email to set your new password.
            </p>
          </div>

          <div className="pt-2">
            <Link href="/login" className="w-full">
              <Button className="w-full bg-[#0066cc] hover:bg-[#004499] text-white h-10 rounded-[4px] font-[600] text-[14px]">
                Return to Login
              </Button>
            </Link>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <h2 className="text-[24px] font-bold text-[#242424] tracking-tight">Forgot Password?</h2>
            <p className="text-[13px] text-[#898989] leading-relaxed">
              Enter the email address registered with your account and we will email you a verification link to reset your password.
            </p>
          </div>

          <hr className="border-[#e0e0e0]" />

          <div className="space-y-4">
            <div>
              <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
                <Input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@example.com"
                  required
                  className="h-10 pl-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                />
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
                  Sending email...
                </>
              ) : (
                <>
                  <Send className="w-4 h-4" />
                  Send Reset Link
                </>
              )}
            </Button>
          </div>

          <div className="text-center pt-2">
            <Link 
              href="/login" 
              className="inline-flex items-center gap-1.5 text-[13px] text-[#0066cc] font-[600] hover:underline"
            >
              <ArrowLeft className="w-4 h-4" />
              Back to Login
            </Link>
          </div>
        </form>
      )}
    </div>
  );
}
