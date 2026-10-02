"use client";

import { FormEvent, useState } from "react";
import { Save } from "lucide-react";
import { WorkspaceShell } from "@/components/workspace-shell";
import { ApiError, apiRequest } from "@/lib/api";
import type { Organization } from "@/lib/types";

function OrganizationSettings({
  organization,
  onUpdated,
}: {
  organization: Organization;
  onUpdated: (organization: Organization) => void;
}) {
  const editable = organization.myRole === "OWNER";
  const [name, setName] = useState(organization.name);
  const [description, setDescription] = useState(organization.description ?? "");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!editable) return;
    setError("");
    setSuccess("");
    setSubmitting(true);
    try {
      const updated = await apiRequest<Organization>(`/organizations/${organization.id}`, {
        method: "PATCH",
        body: JSON.stringify({ name, description }),
      });
      onUpdated(updated);
      setSuccess("조직 정보가 저장되었습니다.");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "조직 정보를 저장하지 못했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="px-5 py-7 md:px-8 md:py-8">
      <div className="max-w-2xl">
        <p className="text-sm text-muted">{organization.name}</p>
        <h2 className="mt-1 text-2xl font-semibold">조직 정보</h2>
        <p className="mt-3 text-sm leading-6 text-muted">현재 조직의 이름과 설명을 관리합니다.</p>

        <form className="mt-8 border-t border-line pt-7" onSubmit={submit}>
          <div className="grid gap-6 sm:grid-cols-[160px_1fr] sm:items-start">
            <div>
              <p className="text-sm font-medium">기본 정보</p>
              <p className="mt-1 text-xs text-muted">역할: {organization.myRole}</p>
            </div>
            <div className="space-y-5">
              <label className="block">
                <span className="text-sm font-medium">조직 이름</span>
                <input className="mt-2 h-11 w-full border border-line bg-white px-3 text-sm outline-none focus:border-brand focus:ring-1 focus:ring-brand disabled:bg-[#eceeeb] disabled:text-muted" value={name} onChange={(event) => setName(event.target.value)} required maxLength={100} disabled={!editable} />
              </label>
              <label className="block">
                <span className="text-sm font-medium">설명</span>
                <textarea className="mt-2 min-h-28 w-full resize-y border border-line bg-white px-3 py-3 text-sm outline-none focus:border-brand focus:ring-1 focus:ring-brand disabled:bg-[#eceeeb] disabled:text-muted" value={description} onChange={(event) => setDescription(event.target.value)} maxLength={1000} disabled={!editable} />
                <span className="mt-1.5 block text-right text-xs text-muted">{description.length} / 1000</span>
              </label>
            </div>
          </div>

          {error ? <p className="mt-6 border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800" role="alert">{error}</p> : null}
          {success ? <p className="mt-6 border-l-2 border-brand bg-[#f0f6f2] px-3 py-2 text-sm text-[#245b3f]" role="status">{success}</p> : null}
          {!editable ? <p className="mt-6 text-sm text-muted">조직 정보는 Owner만 수정할 수 있습니다.</p> : null}

          {editable ? (
            <div className="mt-7 flex justify-end border-t border-line pt-5">
              <button className="flex h-10 items-center gap-2 bg-ink px-4 text-sm font-semibold text-white transition-colors hover:bg-brand disabled:cursor-not-allowed disabled:opacity-50" disabled={submitting} type="submit">
                <Save size={15} />
                {submitting ? "저장 중..." : "변경사항 저장"}
              </button>
            </div>
          ) : null}
        </form>
      </div>
    </section>
  );
}

export default function OrganizationSettingsPage() {
  return (
    <WorkspaceShell eyebrow="WORKSPACE" title="조직 설정">
      {(organization, updateOrganization) => (
        <OrganizationSettings key={organization.id} organization={organization} onUpdated={updateOrganization} />
      )}
    </WorkspaceShell>
  );
}
