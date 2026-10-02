"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { ArrowLeft, Trash2 } from "lucide-react";
import { AssetForm, statusLabels } from "@/components/asset-form";
import { WorkspaceShell } from "@/components/workspace-shell";
import { ApiError, apiRequest } from "@/lib/api";
import type { Asset, Organization } from "@/lib/types";

function AssetDetail({ organization, assetId }: { organization: Organization; assetId: string }) {
  const router = useRouter();
  const [asset, setAsset] = useState<Asset>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [deleting, setDeleting] = useState(false);
  const canDelete = ["OWNER", "ADMIN"].includes(organization.myRole);

  const loadAsset = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      setAsset(await apiRequest<Asset>(`/organizations/${organization.id}/assets/${assetId}`));
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "자산 정보를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, [assetId, organization.id]);

  useEffect(() => { void loadAsset(); }, [loadAsset]);

  async function remove() {
    if (!asset || !window.confirm(`'${asset.name}' 자산을 비활성화할까요?`)) return;
    setDeleting(true);
    setError("");
    try {
      await apiRequest<null>(`/organizations/${organization.id}/assets/${asset.id}`, { method: "DELETE" });
      router.replace("/app/assets");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "자산을 비활성화하지 못했습니다.");
      setDeleting(false);
    }
  }

  if (loading) return <section className="px-5 py-12 text-sm text-muted md:px-8">자산 정보를 불러오는 중입니다.</section>;
  if (!asset) return <section className="px-5 py-8 md:px-8"><p className="border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800">{error || "자산을 찾을 수 없습니다."}</p><Link className="mt-5 inline-flex text-sm text-muted" href="/app/assets">자산 목록으로 돌아가기</Link></section>;

  return (
    <section className="px-5 py-7 md:px-8 md:py-8">
      <div className="max-w-3xl">
        <Link className="inline-flex items-center gap-2 text-sm text-muted hover:text-ink" href="/app/assets"><ArrowLeft size={16} />자산 목록</Link>
        <div className="mb-7 mt-5 flex flex-col justify-between gap-4 border-b border-line pb-5 sm:flex-row sm:items-end">
          <div><p className="text-sm text-muted">{asset.assetCode}</p><h2 className="mt-1 text-2xl font-semibold">{asset.name}</h2><p className="mt-2 text-xs text-muted">{statusLabels[asset.status]} · 공개 코드 {asset.publicCode}</p></div>
          {canDelete ? <button className="flex h-9 items-center justify-center gap-2 border border-red-200 px-3 text-sm text-red-700 hover:bg-red-50 disabled:opacity-50" disabled={deleting} onClick={() => void remove()} type="button"><Trash2 size={15} />{deleting ? "처리 중" : "비활성화"}</button> : null}
        </div>
        {error ? <p className="mb-5 border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800">{error}</p> : null}
        <AssetForm asset={asset} key={asset.updatedAt} onSaved={setAsset} organization={organization} />
      </div>
    </section>
  );
}

export default function AssetDetailPage() {
  const params = useParams<{ assetId: string }>();
  return <WorkspaceShell eyebrow="ASSETS" title="자산 상세">{(organization) => <AssetDetail assetId={params.assetId} organization={organization} />}</WorkspaceShell>;
}
