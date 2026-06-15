"use client";

import React, { useState, useEffect, use } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { CheckCircle2, XCircle, Loader2, MailOpen, ArrowRight, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import { apiClient } from "@/services/api-client";
import { API_ENDPOINTS } from "@/config/constants";
import { useAuthStore } from "@/store/use-auth-store";

interface InvitationPageProps {
  params: Promise<{
    orgId: string;
    invitationId: string;
  }>;
}

export default function InvitationPage({ params }: InvitationPageProps) {
  const unwrappedParams = use(params);
  const orgId = unwrappedParams.orgId;
  const invitationId = unwrappedParams.invitationId;
  
  const router = useRouter();
  const searchParams = useSearchParams();
  const autoAccept = searchParams.get("accept") === "true";
  const invitationEmail = searchParams.get("email") || "";
  const orgName = searchParams.get("orgName") || "Organization";

  const { user, logout } = useAuthStore();
  const [mounted, setMounted] = useState(false);
  const [status, setStatus] = useState<"loading" | "unauthenticated" | "wrong_account" | "deciding" | "success" | "error">("loading");
  const [errorMessage, setErrorMessage] = useState("");
  const [isProcessing, setIsProcessing] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const handleResponse = async (accepted: boolean) => {
    setIsProcessing(true);
    if (!autoAccept) {
      setStatus("loading");
    }
    
    try {
      await apiClient.patch(
        API_ENDPOINTS.ORGANIZATIONS.INVITATION_DETAIL(orgId, invitationId),
        { accepted }
      );
      
      setStatus("success");
      toast.success(accepted ? "Invitation accepted successfully!" : "Invitation declined.");
      
      // Redirect to select-org page after 2.5 seconds to refresh organizations list
      setTimeout(() => {
        router.push("/select-org");
      }, 2500);
    } catch (err: any) {
      setStatus("error");
      const errMsg = err.response?.data?.message || "Cannot process the request. Please check again.";
      setErrorMessage(errMsg);
      toast.error(errMsg);
    } finally {
      setIsProcessing(false);
    }
  };

  useEffect(() => {
    if (!mounted) return;

    // 1. User not logged in
    if (!user) {
      setStatus("unauthenticated");
      return;
    }

    // 2. User logged in, but invitationEmail is present and different from user's email
    if (invitationEmail && user.email.toLowerCase() !== invitationEmail.toLowerCase()) {
      setStatus("wrong_account");
      return;
    }

    // 3. User logged in with the correct email (or no email specified in link, fallback as correct)
    if (autoAccept) {
      handleResponse(true);
    } else {
      setStatus("deciding");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mounted, user, invitationEmail, autoAccept, orgId, invitationId]);

  const handleLogout = () => {
    logout();
    setStatus("unauthenticated");
  };

  const currentUrl = typeof window !== 'undefined' ? window.location.pathname + window.location.search : '';
  const loginUrl = `/login?email=${encodeURIComponent(invitationEmail)}&redirect=${encodeURIComponent(currentUrl)}`;
  const registerUrl = `/register?email=${encodeURIComponent(invitationEmail)}&redirect=${encodeURIComponent(currentUrl)}`;

  return (
    <div className="min-h-screen bg-[#f8f8f8] flex flex-col items-center justify-center p-6 font-['Segoe_UI',_sans-serif]">
      <div className="w-full max-w-[460px] bg-white border border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] rounded-[4px] p-8">
        
        {/* LOGO & HEADING */}
        <div className="flex flex-col items-center text-center mb-6">
          <div className="w-12 h-12 rounded-[4px] bg-[#0066cc]/10 border border-[#0066cc]/20 flex items-center justify-center text-[#0066cc] mb-4">
            <MailOpen className="w-6 h-6" />
          </div>
          <p className="text-[12px] font-semibold uppercase tracking-wider text-[#898989] mb-1">
            ERP Platform
          </p>
        </div>

        {/* LOADING STATE */}
        {status === "loading" && (
          <div className="text-center py-6 flex flex-col items-center justify-center gap-4">
            <Loader2 className="w-8 h-8 text-[#0066cc] animate-spin" />
            <h2 className="text-[18px] font-semibold text-[#111111]">Processing</h2>
            <p className="text-[#898989] text-[13px]">Please wait a moment...</p>
          </div>
        )}

        {/* UNAUTHENTICATED STATE */}
        {status === "unauthenticated" && (
          <div>
            <h2 className="text-[20px] font-semibold text-[#111111] text-center mb-3">Organization Invitation</h2>
            <p className="text-[#242424] text-[14px] leading-relaxed mb-4 text-center">
              You have been invited to join the organization <strong className="text-[#111111]">{orgName}</strong> on the **ERP Platform**.
            </p>

            {invitationEmail && (
              <div className="bg-[#f8f8f8] border border-[#e0e0e0] p-4 rounded-[4px] mb-6 text-[13px] text-[#242424] leading-relaxed">
                <p className="mb-1 font-semibold text-[#111111]">🔒 Security Requirement:</p>
                This invitation was sent directly to: <strong className="text-[#0066cc]">{invitationEmail}</strong>. You need to log in or register a new account using exactly this email to continue.
              </div>
            )}

            <div className="flex flex-col gap-2.5">
              <button
                onClick={() => router.push(loginUrl)}
                className="w-full h-10 bg-[#0066cc] hover:bg-[#004499] text-white text-[14px] font-semibold rounded-[4px] transition-colors flex items-center justify-center gap-2"
              >
                Log in
                <ArrowRight className="w-4 h-4" />
              </button>

              <button
                onClick={() => router.push(registerUrl)}
                className="w-full h-10 bg-white border border-[#d0d0d0] hover:bg-[#f8f8f8] text-[#242424] text-[14px] font-semibold rounded-[4px] transition-colors"
              >
                Register new account
              </button>
            </div>
          </div>
        )}

        {/* WRONG ACCOUNT LOGGED IN */}
        {status === "wrong_account" && (
          <div>
            <div className="flex justify-center mb-3 text-[#dc3545]">
              <AlertTriangle className="w-10 h-10" />
            </div>
            <h2 className="text-[20px] font-semibold text-[#dc3545] text-center mb-3">Wrong account</h2>
            
            <p className="text-[#242424] text-[14px] leading-relaxed mb-4 text-center">
              You are logged in as: <strong className="text-[#111111]">{user?.email}</strong>.
            </p>
            
            <div className="bg-[#f8f8f8] border border-[#e0e0e0] p-4 rounded-[4px] mb-6 text-[13px] text-[#242424] leading-relaxed">
              The invitation to join <strong className="text-[#111111]">{orgName}</strong> is exclusively for the email: <strong className="text-[#0066cc]">{invitationEmail}</strong>. Please log out and sign in with the correct account.
            </div>

            <button
              onClick={handleLogout}
              className="w-full h-10 bg-[#0066cc] hover:bg-[#004499] text-white text-[14px] font-semibold rounded-[4px] transition-colors"
            >
              Log out to switch accounts
            </button>
          </div>
        )}

        {/* DECIDING STATE */}
        {status === "deciding" && (
          <div className="text-center">
            <h2 className="text-[20px] font-semibold text-[#111111] mb-2">Organization Invitation</h2>
            <p className="text-[#242424] text-[14px] leading-relaxed mb-6">
              Hello <strong className="text-[#111111]">{user?.firstName} {user?.lastName}</strong>! You have been invited to join <strong className="text-[#0066cc]">{orgName}</strong>. Please respond to your invitation.
            </p>

            <div className="flex flex-col gap-2.5">
              <button
                onClick={() => handleResponse(true)}
                disabled={isProcessing}
                className="w-full h-10 bg-[#0066cc] hover:bg-[#004499] text-white text-[14px] font-semibold rounded-[4px] transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
              >
                Accept invitation
                <ArrowRight className="w-4 h-4" />
              </button>
              
              <button
                onClick={() => handleResponse(false)}
                disabled={isProcessing}
                className="w-full h-10 bg-white border border-[#d0d0d0] hover:bg-[#f8f8f8] text-[#242424] text-[14px] font-semibold rounded-[4px] transition-colors disabled:opacity-50"
              >
                Decline invitation
              </button>
            </div>
          </div>
        )}

        {/* SUCCESS STATE */}
        {status === "success" && (
          <div className="text-center py-6 flex flex-col items-center justify-center gap-4">
            <div className="w-12 h-12 rounded-full bg-[#28a745]/10 border border-[#28a745]/20 flex items-center justify-center text-[#28a745]">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <h2 className="text-[20px] font-semibold text-[#111111]">Accepted successfully!</h2>
            <p className="text-[#898989] text-[13px] leading-relaxed">
              You have successfully joined <strong className="text-[#242424]">{orgName}</strong>. Redirecting you to the management dashboard...
            </p>
            <Loader2 className="w-4 h-4 text-[#28a745] animate-spin mt-2" />
          </div>
        )}

        {/* ERROR STATE */}
        {status === "error" && (
          <div className="text-center py-6 flex flex-col items-center justify-center gap-4">
            <div className="w-12 h-12 rounded-full bg-[#dc3545]/10 border border-[#dc3545]/20 flex items-center justify-center text-[#dc3545]">
              <XCircle className="w-6 h-6" />
            </div>
            <h2 className="text-[20px] font-semibold text-[#dc3545]">Cannot proceed</h2>
            <p className="text-[#898989] text-[13px] leading-relaxed">
              {errorMessage || "The invitation has expired, is invalid, or you do not have permission to access it."}
            </p>
            
            <button
              onClick={() => router.push("/select-org")}
              className="mt-4 w-full h-10 bg-white border border-[#d0d0d0] hover:bg-[#f8f8f8] text-[#242424] text-[14px] font-semibold rounded-[4px] transition-colors"
            >
              Back to organizations list
            </button>
          </div>
        )}

      </div>
    </div>
  );
}
