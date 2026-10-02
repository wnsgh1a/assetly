"use client";

import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { Check, Trash2, UserPlus } from "lucide-react";
import { WorkspaceShell } from "@/components/workspace-shell";
import { ApiError, apiRequest } from "@/lib/api";
import type { MemberRole, Organization, OrganizationMember } from "@/lib/types";

const roleLabels: Record<MemberRole, string> = {
  OWNER: "Owner",
  ADMIN: "Admin",
  MANAGER: "Manager",
  MEMBER: "Member",
};

function Members({ organization }: { organization: Organization }) {
  const [members, setMembers] = useState<OrganizationMember[]>([]);
  const [email, setEmail] = useState("");
  const [role, setRole] = useState<MemberRole>("MEMBER");
  const [draftRoles, setDraftRoles] = useState<Record<number, MemberRole>>({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [busyMemberId, setBusyMemberId] = useState<number>();
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const assignableRoles = useMemo<MemberRole[]>(
    () => organization.myRole === "OWNER" ? ["OWNER", "ADMIN", "MANAGER", "MEMBER"] : ["MANAGER", "MEMBER"],
    [organization.myRole],
  );

  const loadMembers = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const items = await apiRequest<OrganizationMember[]>(`/organizations/${organization.id}/members`);
      setMembers(items);
      setDraftRoles(Object.fromEntries(items.map((member) => [member.id, member.role])));
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "멤버 목록을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, [organization.id]);

  useEffect(() => {
    void loadMembers();
  }, [loadMembers]);

  async function addMember(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    setNotice("");
    try {
      await apiRequest<OrganizationMember>(`/organizations/${organization.id}/members`, {
        method: "POST",
        body: JSON.stringify({ email: email.trim(), role }),
      });
      setEmail("");
      setRole("MEMBER");
      await loadMembers();
      setNotice("멤버를 추가했습니다.");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "멤버를 추가하지 못했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  async function updateRole(member: OrganizationMember) {
    const nextRole = draftRoles[member.id];
    if (!nextRole || nextRole === member.role) return;
    setBusyMemberId(member.id);
    setError("");
    setNotice("");
    try {
      await apiRequest<OrganizationMember>(`/organizations/${organization.id}/members/${member.id}/role`, {
        method: "PATCH",
        body: JSON.stringify({ role: nextRole }),
      });
      await loadMembers();
      setNotice(`${member.name}님의 역할을 변경했습니다.`);
    } catch (caught) {
      setDraftRoles((current) => ({ ...current, [member.id]: member.role }));
      setError(caught instanceof ApiError ? caught.message : "역할을 변경하지 못했습니다.");
    } finally {
      setBusyMemberId(undefined);
    }
  }

  async function removeMember(member: OrganizationMember) {
    if (!window.confirm(`${member.name}님을 조직에서 삭제할까요?`)) return;
    setBusyMemberId(member.id);
    setError("");
    setNotice("");
    try {
      await apiRequest<null>(`/organizations/${organization.id}/members/${member.id}`, { method: "DELETE" });
      await loadMembers();
      setNotice(`${member.name}님을 조직에서 삭제했습니다.`);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "멤버를 삭제하지 못했습니다.");
    } finally {
      setBusyMemberId(undefined);
    }
  }

  function canManage(member: OrganizationMember) {
    return organization.myRole === "OWNER" || ["MANAGER", "MEMBER"].includes(member.role);
  }

  return (
    <section className="px-5 py-7 md:px-8 md:py-8">
      <div className="max-w-5xl">
        <div className="flex flex-col justify-between gap-3 border-b border-line pb-6 sm:flex-row sm:items-end">
          <div>
            <p className="text-sm text-muted">{organization.name}</p>
            <h2 className="mt-1 text-2xl font-semibold">멤버 관리</h2>
          </div>
          <p className="text-sm text-muted">총 {members.length}명</p>
        </div>

        <form className="grid gap-3 border-b border-line py-6 sm:grid-cols-[minmax(0,1fr)_150px_auto]" onSubmit={addMember}>
          <label>
            <span className="mb-2 block text-xs font-medium text-muted">등록된 사용자 이메일</span>
            <input className="h-11 w-full border border-line bg-white px-3 text-sm outline-none focus:border-brand focus:ring-1 focus:ring-brand" type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="member@example.com" required />
          </label>
          <label>
            <span className="mb-2 block text-xs font-medium text-muted">역할</span>
            <select className="h-11 w-full border border-line bg-white px-3 text-sm outline-none focus:border-brand focus:ring-1 focus:ring-brand" value={role} onChange={(event) => setRole(event.target.value as MemberRole)}>
              {assignableRoles.map((item) => <option key={item} value={item}>{roleLabels[item]}</option>)}
            </select>
          </label>
          <button className="mt-auto flex h-11 items-center justify-center gap-2 bg-ink px-4 text-sm font-semibold text-white hover:bg-brand disabled:cursor-not-allowed disabled:opacity-50" disabled={submitting} type="submit">
            <UserPlus size={16} />
            {submitting ? "추가 중" : "멤버 추가"}
          </button>
        </form>

        {error ? <p className="mt-5 border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800" role="alert">{error}</p> : null}
        {notice ? <p className="mt-5 border-l-2 border-brand bg-[#f0f6f2] px-3 py-2 text-sm text-[#245b3f]" role="status">{notice}</p> : null}

        <div className="mt-6 overflow-x-auto border border-line bg-white">
          {loading ? (
            <p className="px-5 py-10 text-center text-sm text-muted">멤버 목록을 불러오는 중입니다.</p>
          ) : members.length === 0 ? (
            <p className="px-5 py-10 text-center text-sm text-muted">등록된 멤버가 없습니다.</p>
          ) : (
            <table className="w-full min-w-[720px] border-collapse text-left text-sm">
              <thead className="border-b border-line bg-[#f7f8f5] text-xs text-muted">
                <tr>
                  <th className="px-4 py-3 font-medium">이름</th>
                  <th className="px-4 py-3 font-medium">이메일</th>
                  <th className="px-4 py-3 font-medium">가입일</th>
                  <th className="px-4 py-3 font-medium">역할</th>
                  <th className="w-24 px-4 py-3 text-right font-medium">작업</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {members.map((member) => {
                  const manageable = canManage(member);
                  const roleOptions: MemberRole[] = organization.myRole === "OWNER" ? ["OWNER", "ADMIN", "MANAGER", "MEMBER"] : ["MANAGER", "MEMBER"];
                  const changed = draftRoles[member.id] !== member.role;
                  return (
                    <tr key={member.id}>
                      <td className="px-4 py-4 font-medium">{member.name}</td>
                      <td className="px-4 py-4 text-muted">{member.email}</td>
                      <td className="px-4 py-4 text-muted">{new Intl.DateTimeFormat("ko-KR").format(new Date(member.joinedAt))}</td>
                      <td className="px-4 py-4">
                        {manageable ? (
                          <select className="h-9 w-32 border border-line bg-white px-2 text-sm outline-none focus:border-brand" value={draftRoles[member.id] ?? member.role} onChange={(event) => setDraftRoles((current) => ({ ...current, [member.id]: event.target.value as MemberRole }))}>
                            {roleOptions.map((item) => <option key={item} value={item}>{roleLabels[item]}</option>)}
                          </select>
                        ) : <span>{roleLabels[member.role]}</span>}
                      </td>
                      <td className="px-4 py-4">
                        {manageable ? (
                          <div className="flex justify-end gap-1">
                            <button aria-label={`${member.name} 역할 저장`} className="grid h-9 w-9 place-items-center text-muted hover:bg-[#edf2ee] hover:text-brand disabled:cursor-not-allowed disabled:opacity-30" disabled={!changed || busyMemberId === member.id} onClick={() => void updateRole(member)} title="역할 저장" type="button"><Check size={17} /></button>
                            <button aria-label={`${member.name} 삭제`} className="grid h-9 w-9 place-items-center text-muted hover:bg-red-50 hover:text-red-700 disabled:cursor-not-allowed disabled:opacity-30" disabled={busyMemberId === member.id} onClick={() => void removeMember(member)} title="멤버 삭제" type="button"><Trash2 size={17} /></button>
                          </div>
                        ) : null}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </section>
  );
}

export default function MembersPage() {
  return (
    <WorkspaceShell eyebrow="WORKSPACE" title="멤버">
      {(organization) => <Members key={organization.id} organization={organization} />}
    </WorkspaceShell>
  );
}
