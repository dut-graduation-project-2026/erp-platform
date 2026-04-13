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

const RegisterPage = () => {
  return (
    <Card className="rounded-[4px] border border-border bg-card py-0 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] ring-0">
      <CardHeader className="space-y-2 border-b border-border px-6 py-6">
        <p className="text-[12px] leading-[1.4] font-normal tracking-[0.1px] text-muted-foreground">
          ERP Platform
        </p>
        <CardTitle className="text-[24px] leading-[1.15] font-semibold tracking-normal text-foreground">
          Create account
        </CardTitle>
        <CardDescription className="text-[14px] leading-[1.5] font-normal text-muted-foreground">
          Create your ERP workspace account to get started.
        </CardDescription>
      </CardHeader>

      <CardContent className="px-6 py-6">
        <form className="space-y-3">
          <div className="space-y-2">
            <Label
              htmlFor="fullName"
              className="text-[14px] leading-[1.4] font-semibold text-foreground"
            >
              Full name
            </Label>
            <Input
              id="fullName"
              name="fullName"
              type="text"
              autoComplete="name"
              placeholder="John Doe"
              className="h-10 rounded-[4px] border-input bg-white px-2 py-2 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2"
              required
            />
          </div>

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
            />
          </div>

          <div className="space-y-2">
            <Label
              htmlFor="password"
              className="text-[14px] leading-[1.4] font-semibold text-foreground"
            >
              Password
            </Label>
            <Input
              id="password"
              name="password"
              type="password"
              autoComplete="new-password"
              placeholder="••••••••"
              className="h-10 rounded-[4px] border-input bg-white px-2 py-2 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2"
              required
            />
          </div>

          <div className="space-y-2">
            <Label
              htmlFor="confirmPassword"
              className="text-[14px] leading-[1.4] font-semibold text-foreground"
            >
              Confirm password
            </Label>
            <Input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              autoComplete="new-password"
              placeholder="••••••••"
              className="h-10 rounded-[4px] border-input bg-white px-2 py-2 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2"
              required
            />
          </div>

          <div className="flex items-center gap-2 pt-1">
            <Checkbox
              id="acceptTerms"
              name="acceptTerms"
              className="h-4 w-4 rounded-[2px] border-[#999999] data-[state=checked]:border-primary data-[state=checked]:bg-primary focus-visible:ring-ring/30"
              required
            />
            <Label
              htmlFor="acceptTerms"
              className="text-[12px] leading-[1.4] font-normal tracking-[0.1px] text-muted-foreground"
            >
              I agree to the Terms and Privacy Policy
            </Label>
          </div>

          <Button
            type="submit"
            className="mt-1 h-10 w-full rounded-[4px] bg-primary px-4 py-2 text-[14px] leading-none font-semibold text-primary-foreground hover:bg-[#004499] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          >
            Create account
          </Button>

          <p className="pt-1 text-center text-[12px] leading-[1.4] text-muted-foreground">
            Already have an account?{" "}
            <Link
              href="/login"
              className="text-[#0099ff] underline underline-offset-2 hover:text-[#004499]"
            >
              Sign in
            </Link>
          </p>
        </form>
      </CardContent>
    </Card>
  );
};

export default RegisterPage;
