import Link from "next/link";
import { ArrowRight, History } from "lucide-react";
import type { AssetHistory, AssetHistoryAction } from "@/lib/types";

export const historyActionLabels: Record<AssetHistoryAction, string> = {
  CREATED: "등록",
  UPDATED: "정보 변경",
  STATUS_CHANGED: "상태 변경",
  LOCATION_CHANGED: "위치 변경",
  ASSIGNEE_CHANGED: "담당자 변경",
  DELETED: "비활성화",
};

const fieldLabels: Record<string, string> = {
  assetCode: "자산번호",
  name: "자산명",
  description: "설명",
  category: "카테고리",
  location: "위치",
  assignedUser: "담당자",
  status: "상태",
  purchaseDate: "구매일",
  purchasePrice: "구매 가격",
};

function value(value: string | null) {
  return value ?? "미지정";
}

export function HistoryList({ items, showAsset = false }: { items: AssetHistory[]; showAsset?: boolean }) {
  if (!items.length) {
    return (
      <div className="grid min-h-40 place-items-center border-t border-line bg-white px-5 py-10 text-center">
        <div><History size={21} className="mx-auto text-muted" /><p className="mt-3 text-sm font-medium">기록된 활동이 없습니다</p></div>
      </div>
    );
  }

  return (
    <div className="divide-y divide-line border-y border-line bg-white">
      {items.map((item) => (
        <article className="grid gap-2 px-4 py-4 sm:grid-cols-[minmax(130px,0.7fr)_minmax(0,1.5fr)_150px] sm:items-center" key={item.id}>
          <div>
            <p className="text-sm font-medium">{historyActionLabels[item.actionType]}</p>
            {showAsset && item.asset.active ? <Link className="mt-1 block truncate text-xs text-muted hover:text-ink" href={`/app/assets/${item.asset.id}`}>{item.asset.assetCode} · {item.asset.name}</Link> : null}
            {showAsset && !item.asset.active ? <p className="mt-1 truncate text-xs text-muted">{item.asset.assetCode} · {item.asset.name} (비활성)</p> : null}
          </div>
          <div className="min-w-0 text-sm text-muted">
            {item.fieldName ? (
              <div className="flex min-w-0 items-center gap-2">
                <span className="shrink-0 text-xs font-medium text-ink">{fieldLabels[item.fieldName] ?? item.fieldName}</span>
                <span className="truncate">{value(item.beforeValue)}</span>
                <ArrowRight size={13} className="shrink-0" />
                <span className="truncate text-ink">{value(item.afterValue)}</span>
              </div>
            ) : <p>{item.memo ?? historyActionLabels[item.actionType]}</p>}
          </div>
          <div className="text-xs text-muted sm:text-right">
            <p className="truncate">{item.actor.name}</p>
            <time dateTime={item.createdAt}>{new Intl.DateTimeFormat("ko-KR", { dateStyle: "short", timeStyle: "short" }).format(new Date(item.createdAt))}</time>
          </div>
        </article>
      ))}
    </div>
  );
}
