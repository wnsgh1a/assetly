"use client";

import { History, PackageSearch } from "lucide-react";
import { WorkspaceShell } from "@/components/workspace-shell";

const metrics = [
  ["전체 자산", "0"],
  ["사용 중", "0"],
  ["수리 중", "0"],
  ["확인 필요", "0"],
] as const;

export default function AppPage() {
  return (
    <WorkspaceShell eyebrow="WORKSPACE" title="대시보드">
      {(organization) => (
        <section className="px-5 py-7 md:px-8 md:py-8">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="text-sm text-muted">{organization.name}</p>
              <h2 className="mt-1 text-2xl font-semibold">자산 현황</h2>
            </div>
            <p className="text-xs text-muted">{organization.myRole}</p>
          </div>

          <div className="mt-7 grid border-y border-line bg-white sm:grid-cols-2 xl:grid-cols-4">
            {metrics.map(([label, value], index) => (
              <div className={`px-5 py-5 ${index ? "border-t border-line sm:border-l sm:border-t-0" : ""} ${index === 2 ? "sm:border-t xl:border-t-0" : ""}`} key={label}>
                <p className="text-xs font-medium text-muted">{label}</p>
                <p className="mt-2 text-2xl font-semibold tabular-nums">{value}</p>
              </div>
            ))}
          </div>

          <div className="mt-8 grid gap-8 xl:grid-cols-[minmax(0,1.6fr)_minmax(280px,0.7fr)]">
            <section>
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-sm font-semibold">최근 자산</h3>
              </div>
              <div className="overflow-hidden border border-line bg-white">
                <div className="grid grid-cols-[minmax(140px,1fr)_110px_100px] border-b border-line bg-[#f7f8f6] px-4 py-2.5 text-xs font-medium text-muted sm:grid-cols-[minmax(180px,1.2fr)_1fr_110px_100px]">
                  <span>자산</span>
                  <span className="hidden sm:block">위치</span>
                  <span>상태</span>
                  <span className="text-right">수정일</span>
                </div>
                <div className="grid min-h-52 place-items-center px-5 py-10 text-center">
                  <div>
                    <PackageSearch size={24} className="mx-auto text-muted" />
                    <p className="mt-4 text-sm font-medium">등록된 자산이 없습니다</p>
                    <p className="mt-1 text-xs text-muted">현재 조직에 연결된 자산 데이터가 없습니다.</p>
                  </div>
                </div>
              </div>
            </section>
            <section>
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-sm font-semibold">최근 활동</h3>
                <History size={16} className="text-muted" />
              </div>
              <div className="min-h-[246px] border-t border-line bg-white px-5 py-6">
                <div className="grid min-h-44 place-items-center text-center">
                  <div>
                    <History size={21} className="mx-auto text-muted" />
                    <p className="mt-3 text-sm font-medium">기록된 활동이 없습니다</p>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </section>
      )}
    </WorkspaceShell>
  );
}
