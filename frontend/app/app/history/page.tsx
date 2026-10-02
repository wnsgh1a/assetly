"use client";

import { useEffect, useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { HistoryList, historyActionLabels } from "@/components/history-list";
import { WorkspaceShell } from "@/components/workspace-shell";
import { ApiError, apiRequest } from "@/lib/api";
import type { AssetHistoryAction, AssetHistoryPage, Organization } from "@/lib/types";

const actions = Object.keys(historyActionLabels) as AssetHistoryAction[];

function HistoryView({ organization }: { organization: Organization }) {
  const [result, setResult] = useState<AssetHistoryPage>();
  const [action, setAction] = useState<AssetHistoryAction | "">("");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    setLoading(true);
    setError("");
    const query = new URLSearchParams({ page: String(page), size: "20" });
    if (action) query.set("actionType", action);
    apiRequest<AssetHistoryPage>(`/organizations/${organization.id}/histories?${query}`)
      .then(setResult)
      .catch((caught) => setError(caught instanceof ApiError ? caught.message : "변경 이력을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [action, organization.id, page]);

  return (
    <section className="px-5 py-7 md:px-8 md:py-8">
      <div className="max-w-5xl">
        <div className="flex flex-wrap items-end justify-between gap-4 border-b border-line pb-5">
          <div><p className="text-sm text-muted">{organization.name}</p><h2 className="mt-1 text-2xl font-semibold">변경 이력</h2></div>
          <label className="text-xs font-medium text-muted">작업 종류<select className="ml-3 h-9 border border-line bg-white px-3 text-sm text-ink outline-none focus:border-brand" onChange={(event) => { setAction(event.target.value as AssetHistoryAction | ""); setPage(0); }} value={action}><option value="">전체</option>{actions.map((item) => <option key={item} value={item}>{historyActionLabels[item]}</option>)}</select></label>
        </div>
        {error ? <p className="mt-5 border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800">{error}</p> : null}
        <div className="mt-6">
          {loading ? <p className="border-t border-line bg-white px-5 py-12 text-center text-sm text-muted">이력을 불러오는 중입니다.</p> : <HistoryList items={result?.items ?? []} showAsset />}
        </div>
        {result && result.totalPages > 1 ? <div className="mt-4 flex items-center justify-end gap-3"><button aria-label="이전 페이지" className="grid h-9 w-9 place-items-center border border-line bg-white disabled:opacity-40" disabled={page === 0} onClick={() => setPage((current) => current - 1)} type="button"><ChevronLeft size={16} /></button><span className="text-xs text-muted">{page + 1} / {result.totalPages}</span><button aria-label="다음 페이지" className="grid h-9 w-9 place-items-center border border-line bg-white disabled:opacity-40" disabled={page + 1 >= result.totalPages} onClick={() => setPage((current) => current + 1)} type="button"><ChevronRight size={16} /></button></div> : null}
      </div>
    </section>
  );
}

export default function HistoryPage() {
  return <WorkspaceShell eyebrow="AUDIT" title="변경 이력">{(organization) => <HistoryView key={organization.id} organization={organization} />}</WorkspaceShell>;
}
