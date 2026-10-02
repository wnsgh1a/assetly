"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowRight, Building2 } from "lucide-react";
import { ApiError, apiRequest, saveSelectedOrganizationId } from "@/lib/api";
import type { Organization } from "@/lib/types";

const fieldClass = "mt-2 w-full border border-line bg-white px-3 text-sm outline-none transition-colors focus:border-brand focus:ring-1 focus:ring-brand";

export default function OrganizationOnboardingPage() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const organization = await apiRequest<Organization>("/organizations", {
        method: "POST",
        body: JSON.stringify({ name, description }),
      });
      saveSelectedOrganizationId(organization.id);
      router.replace("/app");
    } catch (caught) {
      if (caught instanceof ApiError && caught.status === 401) {
        return router.replace("/login?next=/onboarding/organization");
      }
      setError(caught instanceof ApiError ? caught.message : "조직 생성 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="min-h-screen bg-panel">
      <header className="flex h-16 items-center justify-between border-b border-line bg-white px-6 lg:px-10">
        <p className="text-lg font-semibold">Assetly<span className="text-accent">.</span></p>
        <p className="text-xs font-medium text-muted">설정 1 / 1</p>
      </header>
      <div className="mx-auto grid w-full max-w-5xl gap-12 px-6 py-12 lg:grid-cols-[minmax(0,1fr)_320px] lg:px-10 lg:py-20">
        <section className="max-w-xl">
          <p className="text-xs font-semibold uppercase text-brand">Workspace setup</p>
          <h1 className="mt-3 text-3xl font-semibold leading-tight">관리할 조직을 등록하세요</h1>
          <p className="mt-3 text-sm leading-6 text-muted">조직명은 로그인 후 상단과 자산 기록에 표시됩니다.</p>
          <form className="mt-10 space-y-6" onSubmit={submit}>
            <label className="block">
              <span className="text-sm font-medium">조직 이름</span>
              <input className={`${fieldClass} h-11`} value={name} onChange={(event) => setName(event.target.value)} placeholder="조직 이름 입력" required maxLength={100} autoFocus />
            </label>
            <label className="block">
              <span className="text-sm font-medium">설명 <span className="font-normal text-muted">선택</span></span>
              <textarea className={`${fieldClass} min-h-28 resize-y py-3`} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="조직을 구분할 수 있는 간단한 설명" maxLength={1000} />
              <span className="mt-1.5 block text-right text-xs text-muted">{description.length} / 1000</span>
            </label>
            {error ? <p className="border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800" role="alert">{error}</p> : null}
            <button className="flex h-11 items-center justify-center gap-2 bg-ink px-5 text-sm font-semibold text-white transition-colors hover:bg-brand disabled:cursor-not-allowed disabled:opacity-50" disabled={submitting} type="submit">
              {submitting ? "조직 생성 중..." : "워크스페이스 만들기"}
              {!submitting ? <ArrowRight size={16} /> : null}
            </button>
          </form>
        </section>
        <aside className="border-t border-line pt-7 lg:mt-16 lg:border-l lg:border-t-0 lg:pl-7 lg:pt-0">
          <Building2 size={20} className="text-brand" />
          <p className="mt-5 text-sm font-semibold">생성 후 기본 권한</p>
          <p className="mt-2 text-sm leading-6 text-muted">조직을 만든 계정은 Owner로 등록됩니다.</p>
          <div className="mt-6 border-t border-line pt-5 text-xs text-muted">위치와 카테고리는 워크스페이스에서 등록합니다.</div>
        </aside>
      </div>
    </main>
  );
}
