"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { PackageSearch } from "lucide-react";
import { statusLabels } from "@/components/asset-form";
import { HistoryList } from "@/components/history-list";
import { WorkspaceShell } from "@/components/workspace-shell";
import { ApiError, apiRequest } from "@/lib/api";
import type { Asset, AssetHistory, Organization } from "@/lib/types";

type Dashboard = {
  totalAssets: number;
  availableAssets: number;
  inUseAssets: number;
  repairAssets: number;
  lostAssets: number;
  recentAssets: Asset[];
  recentHistories: AssetHistory[];
};

const emptyDashboard: Dashboard = {
  totalAssets: 0,
  availableAssets: 0,
  inUseAssets: 0,
  repairAssets: 0,
  lostAssets: 0,
  recentAssets: [],
  recentHistories: [],
};

function DashboardView({ organization }: { organization: Organization }) {
  const [dashboard, setDashboard] = useState(emptyDashboard);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    apiRequest<Dashboard>(`/organizations/${organization.id}/dashboard`)
      .then(setDashboard)
      .catch((caught) => setError(caught instanceof ApiError ? caught.message : "자산 현황을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [organization.id]);

  const metrics = [
    ["전체 자산", dashboard.totalAssets],
    ["사용 가능", dashboard.availableAssets],
    ["사용 중", dashboard.inUseAssets],
    ["수리 중", dashboard.repairAssets],
    ["분실", dashboard.lostAssets],
  ] as const;

  return (
    <section className="px-5 py-7 md:px-8 md:py-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div><p className="text-sm text-muted">{organization.name}</p><h2 className="mt-1 text-2xl font-semibold">자산 현황</h2></div>
        <p className="text-xs text-muted">{organization.myRole}</p>
      </div>

      {error ? <p className="mt-5 border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800">{error}</p> : null}

      <div className="mt-7 grid border-y border-line bg-white sm:grid-cols-2 xl:grid-cols-5">
        {metrics.map(([label, value], index) => (
          <div className={`border-line px-5 py-5 ${index ? "border-t sm:border-l" : ""} ${index % 2 === 0 && index ? "sm:border-l-0" : ""} xl:border-l xl:border-t-0 ${index === 0 ? "xl:border-l-0" : ""}`} key={label}>
            <p className="text-xs font-medium text-muted">{label}</p>
            <p className="mt-2 text-2xl font-semibold tabular-nums">{loading ? "-" : value}</p>
          </div>
        ))}
      </div>

      <div className="mt-8 grid gap-8 xl:grid-cols-[minmax(0,1.6fr)_minmax(280px,0.7fr)]">
        <section>
          <div className="mb-3 flex items-center justify-between"><h3 className="text-sm font-semibold">최근 자산</h3><Link className="text-xs text-muted hover:text-ink" href="/app/assets">전체 보기</Link></div>
          <div className="overflow-x-auto border border-line bg-white">
            <div className="grid min-w-[560px] grid-cols-[minmax(180px,1fr)_130px_120px_100px] border-b border-line bg-[#f7f8f6] px-4 py-2.5 text-xs font-medium text-muted"><span>자산</span><span>위치</span><span>상태</span><span className="text-right">수정일</span></div>
            {loading ? <p className="px-5 py-12 text-center text-sm text-muted">자산 현황을 불러오는 중입니다.</p> : dashboard.recentAssets.length ? (
              <div className="divide-y divide-line">{dashboard.recentAssets.map((asset) => (
                <Link className="grid min-w-[560px] grid-cols-[minmax(180px,1fr)_130px_120px_100px] px-4 py-3 text-sm hover:bg-[#f8faf8]" href={`/app/assets/${asset.id}`} key={asset.id}><span className="truncate font-medium">{asset.name}</span><span className="truncate text-muted">{asset.location?.name ?? "-"}</span><span className="text-muted">{statusLabels[asset.status]}</span><span className="text-right text-xs text-muted">{new Intl.DateTimeFormat("ko-KR").format(new Date(asset.updatedAt))}</span></Link>
              ))}</div>
            ) : (
              <div className="grid min-h-52 place-items-center px-5 py-10 text-center"><div><PackageSearch size={24} className="mx-auto text-muted" /><p className="mt-4 text-sm font-medium">등록된 자산이 없습니다</p><p className="mt-1 text-xs text-muted">자산을 등록하면 최근 항목이 여기에 표시됩니다.</p></div></div>
            )}
          </div>
        </section>
        <section>
          <div className="mb-3 flex items-center justify-between"><h3 className="text-sm font-semibold">최근 활동</h3><Link className="text-xs text-muted hover:text-ink" href="/app/history">전체 보기</Link></div>
          {loading ? <div className="min-h-[246px] border-t border-line bg-white px-5 py-12 text-center text-sm text-muted">활동을 불러오는 중입니다.</div> : <HistoryList items={dashboard.recentHistories} showAsset />}
        </section>
      </div>
    </section>
  );
}

export default function AppPage() {
  return <WorkspaceShell eyebrow="WORKSPACE" title="대시보드">{(organization) => <DashboardView key={organization.id} organization={organization} />}</WorkspaceShell>;
}
