"use client";

import { FormEvent } from "react";
import Link from "next/link";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export type LoginFormValues = {
  email: string;
  password: string;
  rememberMe: boolean;
};

type LoginFormProps = {
  onSubmit: (values: LoginFormValues) => Promise<void>;
  isSubmitting?: boolean;
};

const LoginForm = ({ onSubmit, isSubmitting = false }: LoginFormProps) => {
  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const formData = new FormData(event.currentTarget);
    const email = formData.get("email");
    const password = formData.get("password");
    const rememberMe = formData.get("rememberMe") === "on";

    if (typeof email !== "string" || typeof password !== "string") {
      return;
    }

    await onSubmit({
      email,
      password,
      rememberMe,
    });
  };

  return (
    <Card className="rounded-[4px] border border-border bg-card py-0 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] ring-0">
      <CardHeader className="space-y-2 border-b border-border px-6 py-6">
        <p className="text-[12px] leading-[1.4] font-normal tracking-[0.1px] text-muted-foreground">
          ERP Platform
        </p>
        <CardTitle className="text-[24px] leading-[1.15] font-semibold tracking-normal text-foreground">
          Sign in
        </CardTitle>
        <CardDescription className="text-[14px] leading-[1.5] font-normal text-muted-foreground">
          Enter your credentials to continue to your ERP workspace.
        </CardDescription>
      </CardHeader>

      <CardContent className="px-6 py-6">
        <form className="space-y-3" onSubmit={handleSubmit}>
          <div className="space-y-2">
            <Label
              htmlFor="email"
              className="text-[14px] leading-[1.4] font-semibold text-foreground"
            >
              Email
            </Label>
            <Input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              placeholder="you@company.com"
              className="h-10 rounded-[4px] border-input bg-white px-2 py-2 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2"
              required
              disabled={isSubmitting}
            />
          </div>

          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label
                htmlFor="password"
                className="text-[14px] leading-[1.4] font-semibold text-foreground"
              >
                Password
              </Label>
              <Link
                href="#"
                className="text-[12px] leading-[1.4] text-[#0099ff] underline underline-offset-2 hover:text-[#004499]"
              >
                Forgot password?
              </Link>
            </div>
            <Input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              placeholder="••••••••"
              className="h-10 rounded-[4px] border-input bg-white px-2 py-2 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2"
              required
              disabled={isSubmitting}
            />
          </div>

          <div className="flex items-center gap-2 pt-1">
            <Checkbox
              id="rememberMe"
              name="rememberMe"
              className="h-4 w-4 rounded-[2px] border-[#999999] data-[state=checked]:border-primary data-[state=checked]:bg-primary focus-visible:ring-ring/30"
              disabled={isSubmitting}
            />
            <Label
              htmlFor="rememberMe"
              className="text-[12px] leading-[1.4] font-normal tracking-[0.1px] text-muted-foreground"
            >
              Remember me
            </Label>
          </div>

          <Button
            type="submit"
            className="mt-1 h-10 w-full rounded-[4px] bg-primary px-4 py-2 text-[14px] leading-none font-semibold text-primary-foreground hover:bg-[#004499] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Signing in..." : "Sign in"}
          </Button>

          <p className="pt-1 text-center text-[12px] leading-[1.4] text-muted-foreground">
            New to ERP Platform?{" "}
            <Link
              href="/register"
              className="text-[#0099ff] underline underline-offset-2 hover:text-[#004499]"
            >
              Create account
            </Link>
          </p>
        </form>
      </CardContent>
    </Card>
  );
};

export default LoginForm;
