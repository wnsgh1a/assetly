"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, usePathname, useRouter } from "next/navigation";
import { ArrowLeft, ScanLine } from "lucide-react";
import { AssetForm, statusLabels } from "@/components/asset-form";
import { HistoryList } from "@/components/history-list";
import { ApiError, apiRequest, clearAccessToken, saveSelectedOrganizationId } from "@/lib/api";
import type { AssetHistoryPage, Organization, PublicAsset } from "@/lib/types";

export default function PublicAssetPage() {
  const params = useParams<{ publicCode: string }>();
  const pathname = usePathname();
  const router = useRouter();
  const [result, setResult] = useState<PublicAsset>();
  const [histories, setHistories] = useState<AssetHistoryPage>();
  const [historyVersion, setHistoryVersion] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    apiRequest<PublicAsset>(`/assets/public/${encodeURIComponent(params.publicCode)}`)
      .then((asset) => {
        saveSelectedOrganizationId(asset.organizationId);
        setResult(asset);
      })
      .catch((caught) => {
        if (caught instanceof ApiError && caught.status === 401) {
          clearAccessToken();
          router.replace(`/login?next=${encodeURIComponent(pathname)}`);
          return;
        }
        setError(caught instanceof ApiError ? caught.message : "자산 정보를 불러오지 못했습니다.");
      })
      .finally(() => setLoading(false));
  }, [params.publicCode, pathname, router]);

  useEffect(() => {
    if (!result) return;
    apiRequest<AssetHistoryPage>(`/organizations/${result.organizationId}/assets/${result.asset.id}/histories?size=10`)
      .then(setHistories)
      .catch(() => setHistories(undefined));
  }, [historyVersion, result]);

  if (loading) return <main className="grid min-h-screen place-items-center bg-panel"><p className="text-sm text-muted">QR 자산 정보를 확인하는 중입니다.</p></main>;
  if (!result) return <main className="grid min-h-screen place-items-center bg-panel px-5"><div className="max-w-sm text-center"><ScanLine size={28} className="mx-auto text-muted" /><h1 className="mt-4 text-lg font-semibold">자산을 열 수 없습니다</h1><p className="mt-2 text-sm text-muted">{error || "QR 코드가 올바른지 확인해 주세요."}</p><Link className="mt-6 inline-flex h-10 items-center bg-ink px-4 text-sm font-semibold text-white" href="/app">워크스페이스로 이동</Link></div></main>;

  const organization: Organization = {
    id: result.organizationId,
    name: result.organizationName,
    description: null,
    myRole: result.myRole,
  };
  const asset = result.asset;

  return (
    <main className="min-h-screen bg-panel">
      <header className="border-b border-line bg-white px-5 py-4">
        <div className="mx-auto flex max-w-3xl items-center justify-between">
          <p className="font-semibold">Assetly<span className="text-[#e68a45]">.</span></p>
          <Link aria-label="워크스페이스로 이동" className="grid h-9 w-9 place-items-center border border-line hover:bg-panel" href="/app" title="워크스페이스로 이동"><ArrowLeft size={17} /></Link>
        </div>
      </header>
      <div className="mx-auto max-w-3xl px-5 py-7 sm:py-10">
        <div className="border-b border-line pb-5">
          <p className="text-xs font-medium text-brand">QR ASSET · {result.organizationName}</p>
          <h1 className="mt-2 text-2xl font-semibold">{asset.name}</h1>
          <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted"><span>{asset.assetCode}</span><span>{statusLabels[asset.status]}</span><span>{asset.location?.name ?? "위치 미지정"}</span></div>
        </div>
        <section className="mt-7 bg-white p-5 sm:p-6">
          <AssetForm asset={asset} onSaved={(saved) => { setResult((current) => current ? { ...current, asset: saved } : current); setHistoryVersion((value) => value + 1); }} organization={organization} />
        </section>
        <section className="mt-8">
          <h2 className="mb-3 text-sm font-semibold">최근 변경 이력</h2>
          <HistoryList items={histories?.items ?? []} />
        </section>
      </div>
    </main>
  );
}
