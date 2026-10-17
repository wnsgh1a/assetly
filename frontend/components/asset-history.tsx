"use client";

import { useEffect, useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { ApiError, apiRequest } from "@/lib/api";
import type { AssetHistoryPage } from "@/lib/types";
import { HistoryList } from "./history-list";
import { LoadError } from "./load-error";

export function AssetHistory({ organizationId, assetId, revision }: { organizationId: number; assetId: number; revision: number }) {
  const [result, setResult] = useState<AssetHistoryPage>();
  const [page, setPage] = useState(0);
  const [attempt, setAttempt] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => { setPage(0); }, [revision]);
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError("");
    apiRequest<AssetHistoryPage>(`/organizations/${organizationId}/assets/${assetId}/histories?page=${page}&size=20`, { signal: controller.signal })
      .then(setResult)
      .catch((caught) => { if (!controller.signal.aborted) setError(caught instanceof ApiError ? caught.message : "변경 이력을 불러오지 못했습니다."); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [assetId, attempt, organizationId, page, revision]);

  if (loading) return <p className="border-t border-line bg-white px-5 py-10 text-center text-sm text-muted" aria-live="polite">이력을 불러오는 중입니다.</p>;
  if (error) return <LoadError message={error} onRetry={() => setAttempt((value) => value + 1)} />;
  return <>
    <HistoryList items={result?.items ?? []} />
    {result && result.totalPages > 1 ? <div className="mt-4 flex items-center justify-end gap-3">
      <button aria-label="자산 이력 이전 페이지" className="grid h-9 w-9 place-items-center border border-line bg-white disabled:opacity-40" disabled={page === 0} onClick={() => setPage((value) => value - 1)} type="button" title="이전 페이지"><ChevronLeft size={16} /></button>
      <span className="text-xs text-muted">{page + 1} / {result.totalPages}</span>
      <button aria-label="자산 이력 다음 페이지" className="grid h-9 w-9 place-items-center border border-line bg-white disabled:opacity-40" disabled={page + 1 >= result.totalPages} onClick={() => setPage((value) => value + 1)} type="button" title="다음 페이지"><ChevronRight size={16} /></button>
    </div> : null}
  </>;
}
