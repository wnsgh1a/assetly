"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { Check, MapPin, Pencil, Plus, Tags, Trash2, X } from "lucide-react";
import { WorkspaceShell } from "@/components/workspace-shell";
import { ApiError, apiRequest } from "@/lib/api";
import type { AssetCategory, AssetLocation, Organization } from "@/lib/types";

type Tab = "locations" | "categories";
type ReferenceItem = AssetCategory | AssetLocation;

const tabConfig = {
  locations: { label: "위치", singular: "위치", icon: MapPin },
  categories: { label: "카테고리", singular: "카테고리", icon: Tags },
} as const;

function ClassificationManager({ organization }: { organization: Organization }) {
  const [activeTab, setActiveTab] = useState<Tab>("locations");
  const [items, setItems] = useState<ReferenceItem[]>([]);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [editingId, setEditingId] = useState<number>();
  const [editingName, setEditingName] = useState("");
  const [editingDescription, setEditingDescription] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const editable = ["OWNER", "ADMIN"].includes(organization.myRole);
  const current = tabConfig[activeTab];

  const loadItems = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const result = await apiRequest<ReferenceItem[]>(`/organizations/${organization.id}/${activeTab}`);
      setItems(result);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : `${current.singular} 목록을 불러오지 못했습니다.`);
    } finally {
      setLoading(false);
    }
  }, [activeTab, current.singular, organization.id]);

  useEffect(() => {
    setEditingId(undefined);
    setNotice("");
    void loadItems();
  }, [loadItems]);

  async function createItem(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    setNotice("");
    try {
      const body = activeTab === "locations" ? { name, description } : { name };
      await apiRequest<ReferenceItem>(`/organizations/${organization.id}/${activeTab}`, {
        method: "POST",
        body: JSON.stringify(body),
      });
      setName("");
      setDescription("");
      await loadItems();
      setNotice(`${current.singular}를 추가했습니다.`);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : `${current.singular}를 추가하지 못했습니다.`);
    } finally {
      setSubmitting(false);
    }
  }

  function beginEdit(item: ReferenceItem) {
    setEditingId(item.id);
    setEditingName(item.name);
    setEditingDescription("description" in item ? item.description ?? "" : "");
    setError("");
    setNotice("");
  }

  async function updateItem(item: ReferenceItem) {
    setSubmitting(true);
    setError("");
    setNotice("");
    try {
      const body = activeTab === "locations"
        ? { name: editingName, description: editingDescription }
        : { name: editingName };
      await apiRequest<ReferenceItem>(`/organizations/${organization.id}/${activeTab}/${item.id}`, {
        method: "PATCH",
        body: JSON.stringify(body),
      });
      setEditingId(undefined);
      await loadItems();
      setNotice(`${current.singular} 정보를 변경했습니다.`);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : `${current.singular}를 수정하지 못했습니다.`);
    } finally {
      setSubmitting(false);
    }
  }

  async function deleteItem(item: ReferenceItem) {
    if (!window.confirm(`'${item.name}' ${current.singular}를 삭제할까요?`)) return;
    setSubmitting(true);
    setError("");
    setNotice("");
    try {
      await apiRequest<null>(`/organizations/${organization.id}/${activeTab}/${item.id}`, { method: "DELETE" });
      await loadItems();
      setNotice(`${current.singular}를 삭제했습니다.`);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : `${current.singular}를 삭제하지 못했습니다.`);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="px-5 py-7 md:px-8 md:py-8">
      <div className="max-w-4xl">
        <div className="border-b border-line pb-6">
          <p className="text-sm text-muted">{organization.name}</p>
          <h2 className="mt-1 text-2xl font-semibold">기준 정보</h2>
        </div>

        <div className="mt-6 flex border-b border-line" role="tablist" aria-label="기준 정보 종류">
          {(Object.keys(tabConfig) as Tab[]).map((tab) => {
            const config = tabConfig[tab];
            const Icon = config.icon;
            const active = tab === activeTab;
            return (
              <button aria-selected={active} className={`flex h-11 items-center gap-2 border-b-2 px-4 text-sm font-medium ${active ? "border-brand text-ink" : "border-transparent text-muted hover:text-ink"}`} key={tab} onClick={() => setActiveTab(tab)} role="tab" type="button">
                <Icon size={16} />
                {config.label}
              </button>
            );
          })}
        </div>

        {editable ? (
          <form className={`grid gap-3 border-b border-line py-5 ${activeTab === "locations" ? "sm:grid-cols-[minmax(160px,0.8fr)_minmax(220px,1.2fr)_auto]" : "sm:grid-cols-[minmax(0,1fr)_auto]"}`} onSubmit={createItem}>
            <label>
              <span className="mb-2 block text-xs font-medium text-muted">{current.singular} 이름</span>
              <input className="h-10 w-full border border-line bg-white px-3 text-sm outline-none focus:border-brand focus:ring-1 focus:ring-brand" maxLength={100} onChange={(event) => setName(event.target.value)} required value={name} />
            </label>
            {activeTab === "locations" ? (
              <label>
                <span className="mb-2 block text-xs font-medium text-muted">설명</span>
                <input className="h-10 w-full border border-line bg-white px-3 text-sm outline-none focus:border-brand focus:ring-1 focus:ring-brand" maxLength={1000} onChange={(event) => setDescription(event.target.value)} value={description} />
              </label>
            ) : null}
            <button className="mt-auto flex h-10 items-center justify-center gap-2 bg-ink px-4 text-sm font-semibold text-white hover:bg-brand disabled:cursor-not-allowed disabled:opacity-50" disabled={submitting} type="submit">
              <Plus size={16} />
              추가
            </button>
          </form>
        ) : (
          <p className="border-b border-line py-4 text-sm text-muted">목록 조회만 가능합니다. 변경 권한은 Owner와 Admin에게 있습니다.</p>
        )}

        {error ? <p className="mt-5 border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800" role="alert">{error}</p> : null}
        {notice ? <p className="mt-5 border-l-2 border-brand bg-[#f0f6f2] px-3 py-2 text-sm text-[#245b3f]" role="status">{notice}</p> : null}

        <div className="mt-6 overflow-x-auto border border-line bg-white">
          <div className={`grid ${activeTab === "locations" ? "min-w-[560px] grid-cols-[minmax(130px,0.8fr)_minmax(180px,1.2fr)_100px]" : "min-w-[360px] grid-cols-[minmax(180px,1fr)_100px]"} border-b border-line bg-[#f7f8f5] px-4 py-3 text-xs font-medium text-muted`}>
            <span>이름</span>
            {activeTab === "locations" ? <span>설명</span> : null}
            <span className="text-right">작업</span>
          </div>

          {loading ? (
            <p className="px-5 py-10 text-center text-sm text-muted">목록을 불러오는 중입니다.</p>
          ) : items.length === 0 ? (
            <p className="px-5 py-10 text-center text-sm text-muted">등록된 {current.singular}가 없습니다.</p>
          ) : (
            <div className="divide-y divide-line">
              {items.map((item) => {
                const editing = item.id === editingId;
                return (
                  <div className={`grid ${activeTab === "locations" ? "min-w-[560px] grid-cols-[minmax(130px,0.8fr)_minmax(180px,1.2fr)_100px]" : "min-w-[360px] grid-cols-[minmax(180px,1fr)_100px]"} items-center gap-3 px-4 py-3 text-sm`} key={item.id}>
                    {editing ? (
                      <input aria-label="이름 수정" className="h-9 min-w-0 border border-line px-2 outline-none focus:border-brand" maxLength={100} onChange={(event) => setEditingName(event.target.value)} required value={editingName} />
                    ) : <span className="truncate font-medium">{item.name}</span>}
                    {activeTab === "locations" ? (
                      editing ? (
                        <input aria-label="설명 수정" className="h-9 min-w-0 border border-line px-2 outline-none focus:border-brand" maxLength={1000} onChange={(event) => setEditingDescription(event.target.value)} value={editingDescription} />
                      ) : <span className="truncate text-muted">{"description" in item && item.description ? item.description : "-"}</span>
                    ) : null}
                    <div className="flex justify-end gap-1">
                      {editable && editing ? (
                        <>
                          <button aria-label="변경 저장" className="grid h-8 w-8 place-items-center text-muted hover:bg-[#edf2ee] hover:text-brand disabled:opacity-40" disabled={submitting || !editingName.trim()} onClick={() => void updateItem(item)} title="변경 저장" type="button"><Check size={16} /></button>
                          <button aria-label="수정 취소" className="grid h-8 w-8 place-items-center text-muted hover:bg-[#eceeeb] hover:text-ink" onClick={() => setEditingId(undefined)} title="수정 취소" type="button"><X size={16} /></button>
                        </>
                      ) : editable ? (
                        <>
                          <button aria-label="수정" className="grid h-8 w-8 place-items-center text-muted hover:bg-[#edf2ee] hover:text-brand" onClick={() => beginEdit(item)} title="수정" type="button"><Pencil size={15} /></button>
                          <button aria-label="삭제" className="grid h-8 w-8 place-items-center text-muted hover:bg-red-50 hover:text-red-700 disabled:opacity-40" disabled={submitting} onClick={() => void deleteItem(item)} title="삭제" type="button"><Trash2 size={16} /></button>
                        </>
                      ) : <span className="text-xs text-muted">조회</span>}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </section>
  );
}

export default function ClassificationPage() {
  return (
    <WorkspaceShell eyebrow="WORKSPACE" title="위치·카테고리">
      {(organization) => <ClassificationManager key={organization.id} organization={organization} />}
    </WorkspaceShell>
  );
}
