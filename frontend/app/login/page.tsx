"use client";

import { FormEvent, Suspense, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { ArrowRight } from "lucide-react";
import { AuthShell } from "@/components/auth-shell";
import { ApiError, apiRequest, saveAccessToken } from "@/lib/api";
import type { LoginResponse, Organization } from "@/lib/types";

const fieldClass = "mt-2 h-11 w-full border border-line bg-white px-3 text-sm outline-none transition-colors focus:border-brand focus:ring-1 focus:ring-brand";

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const registered = searchParams.get("registered") === "1";

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const result = await apiRequest<LoginResponse>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      saveAccessToken(result.accessToken);
      const next = searchParams.get("next");
      if (next?.startsWith("/") && !next.startsWith("//")) return router.replace(next);
      const organizations = await apiRequest<Organization[]>("/organizations");
      router.replace(organizations.length ? "/app" : "/onboarding/organization");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "로그인 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthShell eyebrow="Workspace access" title="다시 만나서 반갑습니다" description="계정으로 로그인해 소속 조직의 자산을 관리하세요.">
      {registered ? (
        <p className="mb-5 border-l-2 border-brand bg-[#f0f6f2] px-3 py-2.5 text-sm text-[#245b3f]" role="status">
          가입이 완료되었습니다. 로그인해 주세요.
        </p>
      ) : null}
      <form className="space-y-5" onSubmit={submit}>
        <label className="block">
          <span className="text-sm font-medium">이메일</span>
          <input className={fieldClass} type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
        </label>
        <label className="block">
          <span className="text-sm font-medium">비밀번호</span>
          <input className={fieldClass} type="password" autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} required />
        </label>
        {error ? <p className="border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800" role="alert">{error}</p> : null}
        <button className="flex h-11 w-full items-center justify-center gap-2 bg-ink px-4 text-sm font-semibold text-white transition-colors hover:bg-brand disabled:cursor-not-allowed disabled:opacity-50" disabled={submitting} type="submit">
          {submitting ? "확인 중..." : "로그인"}
          {!submitting ? <ArrowRight size={16} /> : null}
        </button>
      </form>
      <p className="mt-7 text-sm text-muted">계정이 없나요? <Link href="/signup" className="font-semibold text-ink underline decoration-line underline-offset-4 hover:decoration-ink">회원가입</Link></p>
    </AuthShell>
  );
}

export default function LoginPage() {
  return <Suspense><LoginForm /></Suspense>;
}
