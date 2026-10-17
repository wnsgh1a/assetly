"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { ChevronLeft, ChevronRight, PackageSearch, Plus, Search, X } from "lucide-react";
import { LoadError } from "@/components/load-error";
import { statusLabels } from "@/components/asset-form";
import { WorkspaceShell } from "@/components/workspace-shell";
import { ApiError, apiRequest } from "@/lib/api";
import type { AssetCategory, AssetLocation, AssetPage, AssetStatus, Organization } from "@/lib/types";

const statusStyles: Record<AssetStatus, string> = {
  AVAILABLE: "bg-[#eaf4ed] text-[#245b3f]",
  IN_USE: "bg-[#e9f0f5] text-[#315c75]",
  REPAIR: "bg-[#fff1df] text-[#8a4b18]",
  LOST: "bg-red-50 text-red-700",
  DISPOSED: "bg-[#eceeeb] text-[#626963]",
};

type Filters = { keyword: string; status: string; categoryId: string; locationId: string };
const emptyFilters: Filters = { keyword: "", status: "", categoryId: "", locationId: "" };

function AssetList({ organization }: { organization: Organization }) {
  const [result, setResult] = useState<AssetPage>({ items: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
  const [categories, setCategories] = useState<AssetCategory[]>([]);
  const [locations, setLocations] = useState<AssetLocation[]>([]);
  const [draft, setDraft] = useState<Filters>(emptyFilters);
  const [filters, setFilters] = useState<Filters>(emptyFilters);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [optionsError, setOptionsError] = useState("");
  const canCreate = ["OWNER", "ADMIN"].includes(organization.myRole);

  const loadOptions = useCallback(async () => {
    setOptionsError("");
    await Promise.all([
      apiRequest<AssetCategory[]>(`/organizations/${organization.id}/categories`),
      apiRequest<AssetLocation[]>(`/organizations/${organization.id}/locations`),
    ]).then(([categoryItems, locationItems]) => {
      setCategories(categoryItems);
      setLocations(locationItems);
    }).catch(() => setOptionsError("검색 필터를 불러오지 못했습니다."));
  }, [organization.id]);
  useEffect(() => { void loadOptions(); }, [loadOptions]);

  const loadAssets = useCallback(async (signal?: AbortSignal) => {
    setLoading(true);
    setError("");
    const params = new URLSearchParams({ page: String(page), size: "20" });
    Object.entries(filters).forEach(([key, value]) => { if (value) params.set(key, value); });
    try {
      const items = await apiRequest<AssetPage>(`/organizations/${organization.id}/assets?${params}`, { signal });
      if (!signal?.aborted) setResult(items);
    } catch (caught) {
      if (!signal?.aborted) setError(caught instanceof ApiError ? caught.message : "자산 목록을 불러오지 못했습니다.");
    } finally {
      if (!signal?.aborted) setLoading(false);
    }
  }, [filters, organization.id, page]);

  useEffect(() => {
    const controller = new AbortController();
    void loadAssets(controller.signal);
    return () => controller.abort();
  }, [loadAssets]);

  function search(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPage(0);
    setFilters(draft);
  }

  return (
    <section className="px-5 py-7 md:px-8 md:py-8">
      <div className="max-w-6xl">
        <div className="flex flex-col justify-between gap-4 border-b border-line pb-6 sm:flex-row sm:items-end">
          <div><p className="text-sm text-muted">{organization.name}</p><h2 className="mt-1 text-2xl font-semibold">자산 목록</h2></div>
          {canCreate ? <Link className="flex h-10 items-center justify-center gap-2 bg-ink px-4 text-sm font-semibold text-white hover:bg-brand" href="/app/assets/new"><Plus size={16} />자산 등록</Link> : null}
        </div>

        <form className="grid gap-2 border-b border-line py-5 sm:grid-cols-2 xl:grid-cols-[minmax(0,1fr)_130px_140px_140px_auto]" onSubmit={search}>
          <label className="relative"><span className="sr-only">검색어</span><Search className="absolute left-3 top-3 text-muted" size={16} /><input className="h-10 w-full border border-line bg-white pl-9 pr-3 text-sm outline-none focus:border-brand" onChange={(event) => setDraft({ ...draft, keyword: event.target.value })} placeholder="자산명 또는 자산번호" value={draft.keyword} /></label>
          <select aria-label="상태 필터" className="h-10 border border-line bg-white px-3 text-sm outline-none focus:border-brand" onChange={(event) => setDraft({ ...draft, status: event.target.value })} value={draft.status}><option value="">모든 상태</option>{(Object.keys(statusLabels) as AssetStatus[]).map((item) => <option key={item} value={item}>{statusLabels[item]}</option>)}</select>
          <select aria-label="카테고리 필터" className="h-10 border border-line bg-white px-3 text-sm outline-none focus:border-brand" onChange={(event) => setDraft({ ...draft, categoryId: event.target.value })} value={draft.categoryId}><option value="">모든 카테고리</option>{categories.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select>
          <select aria-label="위치 필터" className="h-10 border border-line bg-white px-3 text-sm outline-none focus:border-brand" onChange={(event) => setDraft({ ...draft, locationId: event.target.value })} value={draft.locationId}><option value="">모든 위치</option>{locations.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select>
          <button className="h-10 bg-ink px-4 text-sm font-semibold text-white hover:bg-brand" type="submit">조회</button>
        </form>

        {optionsError ? <LoadError message={optionsError} onRetry={() => void loadOptions()} /> : null}
        {error ? <LoadError message={error} onRetry={() => void loadAssets()} /> : null}

        <div className="mt-6 overflow-x-auto border border-line bg-white">
          <div className="grid min-w-[850px] grid-cols-[minmax(180px,1.3fr)_130px_130px_130px_120px_110px] border-b border-line bg-[#f7f8f5] px-4 py-3 text-xs font-medium text-muted"><span>자산</span><span>자산번호</span><span>카테고리</span><span>위치</span><span>상태</span><span className="text-right">수정일</span></div>
          {loading ? <p className="px-5 py-12 text-center text-sm text-muted" role="status">자산 목록을 불러오는 중입니다.</p> : error ? null : result.items.length === 0 ? (
            <div className="grid min-h-56 place-items-center px-5 py-10 text-center"><div><PackageSearch className="mx-auto text-muted" size={25} /><p className="mt-4 text-sm font-medium">조건에 맞는 자산이 없습니다</p><p className="mt-1 text-xs text-muted">등록된 자산이 없거나 검색 조건을 변경해야 합니다.</p></div></div>
          ) : (
            <div className="divide-y divide-line">{result.items.map((asset) => (
              <Link className="grid min-w-[850px] grid-cols-[minmax(180px,1.3fr)_130px_130px_130px_120px_110px] items-center px-4 py-3.5 text-sm hover:bg-[#f8faf8]" href={`/app/assets/${asset.id}`} key={asset.id}>
                <span className="truncate font-medium">{asset.name}</span><span className="truncate text-muted">{asset.assetCode}</span><span className="truncate text-muted">{asset.category?.name ?? "-"}</span><span className="truncate text-muted">{asset.location?.name ?? "-"}</span><span><span className={`inline-block px-2 py-1 text-xs font-medium ${statusStyles[asset.status]}`}>{statusLabels[asset.status]}</span></span><span className="text-right text-xs text-muted">{new Intl.DateTimeFormat("ko-KR").format(new Date(asset.updatedAt))}</span>
              </Link>
            ))}</div>
          )}
        </div>

        {Object.values(filters).some(Boolean) ? <button className="mt-4 inline-flex min-h-9 items-center gap-2 text-sm text-muted hover:text-ink" onClick={() => { setDraft(emptyFilters); setFilters(emptyFilters); setPage(0); }} type="button"><X size={15} />검색 조건 초기화</button> : null}
        {!error ? <div className="mt-4 flex items-center justify-between text-sm"><span className="text-muted">{loading ? "조회 중" : `총 ${result.totalElements}개`}</span><div className="flex items-center gap-2"><button aria-label="이전 페이지" title="이전 페이지" className="grid h-9 w-9 place-items-center border border-line bg-white disabled:opacity-30" disabled={page === 0 || loading} onClick={() => setPage((value) => value - 1)} type="button"><ChevronLeft size={17} /></button><span className="min-w-16 text-center text-xs text-muted">{result.totalPages ? page + 1 : 0} / {result.totalPages}</span><button aria-label="다음 페이지" title="다음 페이지" className="grid h-9 w-9 place-items-center border border-line bg-white disabled:opacity-30" disabled={page + 1 >= result.totalPages || loading} onClick={() => setPage((value) => value + 1)} type="button"><ChevronRight size={17} /></button></div></div> : null}
      </div>
    </section>
  );
}

export default function AssetsPage() {
  return <WorkspaceShell eyebrow="ASSETS" title="자산">{(organization) => <AssetList key={organization.id} organization={organization} />}</WorkspaceShell>;
}
