"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowRight } from "lucide-react";
import { AuthShell } from "@/components/auth-shell";
import { ApiError, apiRequest } from "@/lib/api";

const fieldClass = "mt-2 h-11 w-full border border-line bg-white px-3 text-sm outline-none transition-colors focus:border-brand focus:ring-1 focus:ring-brand";

export default function SignupPage() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await apiRequest("/auth/signup", {
        method: "POST",
        body: JSON.stringify({ name, email, password }),
      });
      router.replace("/login?registered=1");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "회원가입 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthShell eyebrow="Create account" title="Assetly 시작하기" description="계정을 만든 다음 첫 조직을 설정합니다.">
      <form className="space-y-5" onSubmit={submit}>
        <label className="block">
          <span className="text-sm font-medium">이름</span>
          <input className={fieldClass} autoComplete="name" value={name} onChange={(event) => setName(event.target.value)} required maxLength={50} />
        </label>
        <label className="block">
          <span className="text-sm font-medium">이메일</span>
          <input className={fieldClass} type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
        </label>
        <label className="block">
          <span className="text-sm font-medium">비밀번호</span>
          <input className={fieldClass} type="password" autoComplete="new-password" value={password} onChange={(event) => setPassword(event.target.value)} required minLength={8} />
          <span className="mt-1.5 block text-xs text-muted">8자 이상</span>
        </label>
        {error ? <p className="border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800" role="alert">{error}</p> : null}
        <button className="flex h-11 w-full items-center justify-center gap-2 bg-ink px-4 text-sm font-semibold text-white transition-colors hover:bg-brand disabled:cursor-not-allowed disabled:opacity-50" disabled={submitting} type="submit">
          {submitting ? "계정 생성 중..." : "계정 만들기"}
          {!submitting ? <ArrowRight size={16} /> : null}
        </button>
      </form>
      <p className="mt-7 text-sm text-muted">이미 계정이 있나요? <Link href="/login" className="font-semibold text-ink underline decoration-line underline-offset-4 hover:decoration-ink">로그인</Link></p>
    </AuthShell>
  );
}
