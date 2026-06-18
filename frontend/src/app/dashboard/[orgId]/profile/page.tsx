"use client";

import React, { useState, useEffect } from "react";
import { User, Mail, Shield, Building2, Save, Info, RefreshCw, CheckCircle2, UserCircle, KeyRound, Eye, EyeOff } from "lucide-react";
import { useAuthStore } from "@/store/use-auth-store";
import { updateProfileApi, changePasswordApi } from "@/features/auth/services/authService";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { useSearchParams, useRouter } from "next/navigation";
import { cn } from "@/lib/utils";

export default function ProfilePage() {
  const { user, setUser, organizations } = useAuthStore();
  const searchParams = useSearchParams();
  const router = useRouter();
  
  // Tabs State (profile | password | organizations)
  const rawTab = searchParams.get("tab");
  const activeTab = rawTab === "password" || rawTab === "organizations" ? rawTab : "profile";

  // Profile Form State
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [isSavingProfile, setIsSavingProfile] = useState(false);

  // Password Form State
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isSavingPassword, setIsSavingPassword] = useState(false);
  
  // Toggle password visibility
  const [showOld, setShowOld] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  // Sync state with store on mount/user change
  useEffect(() => {
    if (user) {
      setFirstName(user.firstName || "");
      setLastName(user.lastName || "");
    }
  }, [user]);

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    if (!firstName.trim() || !lastName.trim()) {
      return toast.error("First name and last name are required.");
    }

    setIsSavingProfile(true);
    try {
      const updated = await updateProfileApi(user.id, {
        firstName: firstName.trim(),
        lastName: lastName.trim(),
      });

      // Update Zustand store
      setUser({
        ...user,
        firstName: updated.firstName,
        lastName: updated.lastName,
      });

      toast.success("Profile details updated successfully!");
    } catch (err) {
      console.error(err);
      toast.error("Failed to update profile settings.");
    } finally {
      setIsSavingProfile(false);
    }
  };

  const handleSavePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    if (!oldPassword) {
      return toast.error("Vui lòng nhập mật khẩu hiện tại.");
    }
    if (!newPassword || newPassword.length < 8) {
      return toast.error("Mật khẩu mới phải chứa ít nhất 8 ký tự.");
    }
    if (newPassword !== confirmPassword) {
      return toast.error("Mật khẩu xác nhận không khớp.");
    }

    setIsSavingPassword(true);
    try {
      await changePasswordApi(user.id, {
        oldPassword,
        newPassword,
      });

      toast.success("Password changed successfully!");
      // Reset form
      setOldPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err: any) {
      console.error(err);
      const errorMessage = err.response?.data?.message || "Failed to change password. Please try again.";
      toast.error(errorMessage);
    } finally {
      setIsSavingPassword(false);
    }
  };

  const handleTabChange = (tabName: string) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set("tab", tabName);
    router.replace(`?${params.toString()}`);
  };

  if (!user) {
    return (
      <div className="flex flex-col justify-center items-center h-full text-[#898989] font-['Segoe_UI'] bg-[#f8f8f8]">
        <RefreshCw className="w-8 h-8 animate-spin text-[#0066cc] mb-3" />
        <span className="text-[14px]">Loading user context...</span>
      </div>
    );
  }

  return (
    <div className="min-h-full bg-[#f8f8f8] flex flex-col font-['Segoe_UI',_sans-serif]">
      {/* Top Banner Control */}
      <div className="border-b border-[#e0e0e0] px-8 py-5 flex flex-col gap-4 bg-white shrink-0 shadow-[0px_1px_2px_rgba(0,0,0,0.05)]">
        <div className="flex items-center text-[12px] text-[#898989] font-[600] tracking-wide uppercase">
          <span className="hover:text-[#0066cc] cursor-pointer">ERP Platform</span>
          <span className="mx-2">/</span>
          <span className="text-[#0066cc] font-[600]">My Profile</span>
        </div>

        <div>
          <h1 className="text-[32px] font-bold text-[#242424] leading-[1.2] tracking-tight">Account Settings</h1>
          <p className="text-[13px] text-[#898989] mt-1">Manage your identity, security credentials, and organization access</p>
        </div>
      </div>

      {/* Profile Layout */}
      <div className="flex-1 max-w-[1100px] w-full mx-auto p-8 flex flex-col md:flex-row gap-8">
        {/* Left Card: Summary & Sidebar */}
        <div className="w-full md:w-[320px] shrink-0 space-y-6">
          <div className="bg-white rounded-[4px] border border-[#e0e0e0] p-6 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] text-center">
            <div className="relative inline-block mb-4">
              <Avatar className="h-24 w-24 mx-auto border-2 border-[#f0f4ff] shadow-sm">
                <AvatarImage src={user.avatarUrl} />
                <AvatarFallback className="bg-[#f0f4ff] text-[#0066cc] text-[28px] font-bold">
                  {user.firstName?.[0]}{user.lastName?.[0]}
                </AvatarFallback>
              </Avatar>
            </div>
            
            <h2 className="text-[18px] font-bold text-[#242424] leading-tight">
              {user.firstName} {user.lastName}
            </h2>
            <p className="text-[13px] text-[#898989] mt-1 mb-4 break-all">{user.email}</p>

            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-[4px] bg-[#f0f4ff] border border-[#dbeafe] text-[#0066cc] text-[12px] font-semibold">
              <Shield className="w-3.5 h-3.5" />
              Verified User Account
            </div>
          </div>

          {/* Sidebar Menu Tabs */}
          <div className="bg-white rounded-[4px] border border-[#e0e0e0] p-2 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] space-y-1">
            <button
              onClick={() => handleTabChange("profile")}
              className={cn(
                "w-full text-left px-4 py-2.5 rounded-[2px] text-[14px] font-[600] flex items-center gap-2.5 transition-all",
                activeTab === "profile"
                  ? "bg-[#f0f4ff] text-[#0066cc]"
                  : "text-[#242424] hover:bg-[#f8f8f8] hover:text-[#0066cc]"
              )}
            >
              <UserCircle className="w-4.5 h-4.5" />
              Personal Details
            </button>
            <button
              onClick={() => handleTabChange("password")}
              className={cn(
                "w-full text-left px-4 py-2.5 rounded-[2px] text-[14px] font-[600] flex items-center gap-2.5 transition-all",
                activeTab === "password"
                  ? "bg-[#f0f4ff] text-[#0066cc]"
                  : "text-[#242424] hover:bg-[#f8f8f8] hover:text-[#0066cc]"
              )}
            >
              <KeyRound className="w-4.5 h-4.5" />
              Change Password
            </button>
            <button
              onClick={() => handleTabChange("organizations")}
              className={cn(
                "w-full text-left px-4 py-2.5 rounded-[2px] text-[14px] font-[600] flex items-center gap-2.5 transition-all",
                activeTab === "organizations"
                  ? "bg-[#f0f4ff] text-[#0066cc]"
                  : "text-[#242424] hover:bg-[#f8f8f8] hover:text-[#0066cc]"
              )}
            >
              <Building2 className="w-4.5 h-4.5" />
              My Organizations
            </button>
          </div>
        </div>

        {/* Right Section: Form Content */}
        <div className="flex-1 bg-white rounded-[4px] border border-[#e0e0e0] p-8 shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
          
          {/* TAB 1: PERSONAL DETAILS */}
          {activeTab === "profile" && (
            <form onSubmit={handleSaveProfile} className="space-y-6">
              <div>
                <h3 className="text-[18px] font-bold text-[#242424] mb-1">Personal Details</h3>
                <p className="text-[13px] text-[#898989]">Update your first name and last name. Changes will sync across the workspace instantly.</p>
              </div>

              <hr className="border-[#e0e0e0]" />

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">First Name</label>
                  <Input
                    type="text"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    placeholder="Enter your first name"
                    required
                    className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                  />
                </div>

                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Last Name</label>
                  <Input
                    type="text"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    placeholder="Enter your last name"
                    required
                    className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                  />
                </div>
              </div>

              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Primary Email Address</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
                  <Input
                    type="email"
                    value={user.email}
                    disabled
                    className="h-10 pl-10 bg-[#f8f8f8] border-[#e0e0e0] text-[#898989] cursor-not-allowed rounded-[4px]"
                  />
                </div>
                <div className="flex gap-1.5 items-start mt-2 text-[12px] text-[#898989]">
                  <Info className="w-3.5 h-3.5 shrink-0 mt-0.5" />
                  <span>Email is your unique account identifier and cannot be modified directly. Contact administrators for support.</span>
                </div>
              </div>

              <hr className="border-[#e0e0e0] pt-2" />

              <div className="flex justify-end pt-2">
                <Button
                  type="submit"
                  disabled={isSavingProfile}
                  className="bg-[#0066cc] hover:bg-[#004499] text-white px-5 h-10 rounded-[4px] font-[600] text-[14px] flex items-center gap-1.5 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] transition-colors"
                >
                  {isSavingProfile ? (
                    <>
                      <RefreshCw className="w-4 h-4 animate-spin" />
                      Saving...
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4" />
                      Save Personal Details
                    </>
                  )}
                </Button>
              </div>
            </form>
          )}

          {/* TAB 2: CHANGE PASSWORD */}
          {activeTab === "password" && (
            <form onSubmit={handleSavePassword} className="space-y-6">
              <div>
                <h3 className="text-[18px] font-bold text-[#242424] mb-1">Change Password</h3>
                <p className="text-[13px] text-[#898989]">Ensure your account is using a long, secure password containing letters, numbers, and symbols.</p>
              </div>

              <hr className="border-[#e0e0e0]" />

              <div className="space-y-5">
                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Current Password</label>
                  <div className="relative">
                    <Input
                      type={showOld ? "text" : "password"}
                      value={oldPassword}
                      onChange={(e) => setOldPassword(e.target.value)}
                      placeholder="••••••••"
                      required
                      className="h-10 pr-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                    />
                    <button
                      type="button"
                      onClick={() => setShowOld(!showOld)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-[#898989] hover:text-[#242424]"
                    >
                      {showOld ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                </div>

                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">New Password</label>
                  <div className="relative">
                    <Input
                      type={showNew ? "text" : "password"}
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      placeholder="••••••••"
                      required
                      className="h-10 pr-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
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
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Confirm New Password</label>
                  <div className="relative">
                    <Input
                      type={showConfirm ? "text" : "password"}
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="••••••••"
                      required
                      className="h-10 pr-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
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
              </div>

              <hr className="border-[#e0e0e0] pt-2" />

              <div className="flex justify-end pt-2">
                <Button
                  type="submit"
                  disabled={isSavingPassword}
                  className="bg-[#0066cc] hover:bg-[#004499] text-white px-5 h-10 rounded-[4px] font-[600] text-[14px] flex items-center gap-1.5 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] transition-colors"
                >
                  {isSavingPassword ? (
                    <>
                      <RefreshCw className="w-4 h-4 animate-spin" />
                      Updating...
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4" />
                      Update Password
                    </>
                  )}
                </Button>
              </div>
            </form>
          )}

          {/* TAB 3: ORGANIZATIONS */}
          {activeTab === "organizations" && (
            <div className="space-y-6">
              <div>
                <h3 className="text-[18px] font-bold text-[#242424] mb-1">Associated Organizations</h3>
                <p className="text-[13px] text-[#898989]">Review organizations and business entities mapped to your credentials</p>
              </div>

              <hr className="border-[#e0e0e0]" />

              <div className="space-y-4">
                <h4 className="text-[14px] font-[600] text-[#242424] flex items-center gap-2">
                  <Building2 className="w-4.5 h-4.5 text-[#0066cc]" />
                  Authorized Organizations ({organizations.length})
                </h4>

                <div className="grid grid-cols-1 gap-3">
                  {organizations.map((org) => (
                    <div 
                      key={org.id} 
                      className="border border-[#e0e0e0] p-4 rounded-[4px] flex justify-between items-center hover:border-[#0066cc] transition-all"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-[4px] bg-[#f0f4ff] flex items-center justify-center text-[#0066cc] border border-[#dbeafe]">
                          <Building2 className="w-5 h-5" />
                        </div>
                        <div>
                          <p className="text-[14px] font-bold text-[#242424]">{org.name}</p>
                          <p className="text-[12px] text-[#898989]">ID: {org.id}</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-1.5 text-[12px] text-green-700 bg-green-50 px-2.5 py-1 border border-green-200 rounded-[4px] font-semibold">
                        <CheckCircle2 className="w-3.5 h-3.5" />
                        Active Member
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <hr className="border-[#e0e0e0] pt-2" />

              <div className="bg-[#f8f8f8] border border-[#e0e0e0] rounded-[4px] p-4 flex gap-3 text-[13px] text-[#898989] leading-relaxed">
                <Info className="w-5 h-5 text-[#0066cc] shrink-0 mt-0.5" />
                <div>
                  <p className="font-bold text-[#242424] mb-1">Identity & Access Control Settings</p>
                  <span>To update passwords, multifactor authentication settings or change organization roles, please check with your workspace administrator or refer to system governance guidelines.</span>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
