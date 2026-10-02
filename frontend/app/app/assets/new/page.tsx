"use client";

import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import Link from "next/link";
import { AssetForm } from "@/components/asset-form";
import { WorkspaceShell } from "@/components/workspace-shell";

export default function NewAssetPage() {
  const router = useRouter();
  return (
    <WorkspaceShell eyebrow="ASSETS" title="자산 등록">
      {(organization) => (
        <section className="px-5 py-7 md:px-8 md:py-8">
          <div className="max-w-3xl">
            <Link className="inline-flex items-center gap-2 text-sm text-muted hover:text-ink" href="/app/assets"><ArrowLeft size={16} />자산 목록</Link>
            <div className="mb-7 mt-5 border-b border-line pb-5"><p className="text-sm text-muted">{organization.name}</p><h2 className="mt-1 text-2xl font-semibold">새 자산 등록</h2></div>
            <AssetForm organization={organization} onSaved={(asset) => router.replace(`/app/assets/${asset.id}`)} />
          </div>
        </section>
      )}
    </WorkspaceShell>
  );
}
