"use client";

import { FormEvent, useEffect, useState } from "react";
import { Save } from "lucide-react";
import { ApiError, apiRequest } from "@/lib/api";
import type { Asset, AssetCategory, AssetLocation, AssetStatus, AssignableUser, Organization } from "@/lib/types";

export const statusLabels: Record<AssetStatus, string> = {
  AVAILABLE: "사용 가능",
  IN_USE: "사용 중",
  REPAIR: "수리 중",
  LOST: "분실",
  DISPOSED: "폐기",
};

const statuses = Object.keys(statusLabels) as AssetStatus[];

type AssetFormProps = {
  organization: Organization;
  asset?: Asset;
  onSaved: (asset: Asset) => void;
};

export function AssetForm({ organization, asset, onSaved }: AssetFormProps) {
  const fullEditable = ["OWNER", "ADMIN"].includes(organization.myRole);
  const operationalEditable = fullEditable || organization.myRole === "MANAGER";
  const creating = !asset;
  const [assetCode, setAssetCode] = useState(asset?.assetCode ?? "");
  const [name, setName] = useState(asset?.name ?? "");
  const [description, setDescription] = useState(asset?.description ?? "");
  const [status, setStatus] = useState<AssetStatus>(asset?.status ?? "AVAILABLE");
  const [categoryId, setCategoryId] = useState(asset?.category?.id.toString() ?? "");
  const [locationId, setLocationId] = useState(asset?.location?.id.toString() ?? "");
  const [assignedUserId, setAssignedUserId] = useState(asset?.assignedUser?.id.toString() ?? "");
  const [purchaseDate, setPurchaseDate] = useState(asset?.purchaseDate ?? "");
  const [purchasePrice, setPurchasePrice] = useState(asset?.purchasePrice?.toString() ?? "");
  const [categories, setCategories] = useState<AssetCategory[]>([]);
  const [locations, setLocations] = useState<AssetLocation[]>([]);
  const [assignees, setAssignees] = useState<AssignableUser[]>([]);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  useEffect(() => {
    Promise.all([
      apiRequest<AssetCategory[]>(`/organizations/${organization.id}/categories`),
      apiRequest<AssetLocation[]>(`/organizations/${organization.id}/locations`),
      apiRequest<AssignableUser[]>(`/organizations/${organization.id}/assignees`),
    ])
      .then(([categoryItems, locationItems, userItems]) => {
        setCategories(categoryItems);
        setLocations(locationItems);
        setAssignees(userItems);
      })
      .catch((caught) => setError(caught instanceof ApiError ? caught.message : "선택 항목을 불러오지 못했습니다."))
      .finally(() => setLoadingOptions(false));
  }, [organization.id]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (creating && !fullEditable) return;
    setSubmitting(true);
    setError("");
    setNotice("");
    try {
      const body = {
        assetCode,
        name,
        description,
        status,
        categoryId: categoryId ? Number(categoryId) : null,
        locationId: locationId ? Number(locationId) : null,
        assignedUserId: assignedUserId ? Number(assignedUserId) : null,
        purchaseDate: purchaseDate || null,
        purchasePrice: purchasePrice || null,
      };
      const saved = await apiRequest<Asset>(
        creating ? `/organizations/${organization.id}/assets` : `/organizations/${organization.id}/assets/${asset.id}`,
        { method: creating ? "POST" : "PATCH", body: JSON.stringify(body) },
      );
      setAssetCode(saved.assetCode);
      setName(saved.name);
      setDescription(saved.description ?? "");
      setStatus(saved.status);
      setCategoryId(saved.category?.id.toString() ?? "");
      setLocationId(saved.location?.id.toString() ?? "");
      setAssignedUserId(saved.assignedUser?.id.toString() ?? "");
      setPurchaseDate(saved.purchaseDate ?? "");
      setPurchasePrice(saved.purchasePrice?.toString() ?? "");
      setNotice(creating ? "자산을 등록했습니다." : "자산 정보를 저장했습니다.");
      onSaved(saved);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "자산 정보를 저장하지 못했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  if (creating && !fullEditable) {
    return <p className="border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800">자산 등록 권한이 없습니다.</p>;
  }

  const inputClass = "mt-2 h-10 w-full border border-line bg-white px-3 text-sm outline-none focus:border-brand focus:ring-1 focus:ring-brand disabled:bg-[#eceeeb] disabled:text-muted";

  return (
    <form onSubmit={submit}>
      <div className="grid gap-x-5 gap-y-5 md:grid-cols-2">
        <label><span className="text-sm font-medium">자산번호</span><input className={inputClass} disabled={!fullEditable} maxLength={100} onChange={(event) => setAssetCode(event.target.value)} required value={assetCode} /></label>
        <label><span className="text-sm font-medium">자산명</span><input className={inputClass} disabled={!fullEditable} maxLength={150} onChange={(event) => setName(event.target.value)} required value={name} /></label>
        <label><span className="text-sm font-medium">상태</span><select className={inputClass} disabled={!operationalEditable} onChange={(event) => setStatus(event.target.value as AssetStatus)} value={status}>{statuses.map((item) => <option key={item} value={item}>{statusLabels[item]}</option>)}</select></label>
        <label><span className="text-sm font-medium">카테고리</span><select className={inputClass} disabled={!fullEditable || loadingOptions} onChange={(event) => setCategoryId(event.target.value)} value={categoryId}><option value="">미지정</option>{categories.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
        <label><span className="text-sm font-medium">위치</span><select className={inputClass} disabled={!operationalEditable || loadingOptions} onChange={(event) => setLocationId(event.target.value)} value={locationId}><option value="">미지정</option>{locations.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
        <label><span className="text-sm font-medium">담당자</span><select className={inputClass} disabled={!operationalEditable || loadingOptions} onChange={(event) => setAssignedUserId(event.target.value)} value={assignedUserId}><option value="">미지정</option>{assignees.map((item) => <option key={item.userId} value={item.userId}>{item.name} ({item.email})</option>)}</select></label>
        <label><span className="text-sm font-medium">구매일</span><input className={inputClass} disabled={!fullEditable} onChange={(event) => setPurchaseDate(event.target.value)} type="date" value={purchaseDate} /></label>
        <label><span className="text-sm font-medium">구매 가격</span><input className={inputClass} disabled={!fullEditable} min="0" onChange={(event) => setPurchasePrice(event.target.value)} step="0.01" type="number" value={purchasePrice} /></label>
        <label className="md:col-span-2"><span className="text-sm font-medium">설명</span><textarea className="mt-2 min-h-24 w-full resize-y border border-line bg-white px-3 py-3 text-sm outline-none focus:border-brand focus:ring-1 focus:ring-brand disabled:bg-[#eceeeb] disabled:text-muted" disabled={!fullEditable} maxLength={2000} onChange={(event) => setDescription(event.target.value)} value={description} /></label>
      </div>

      {error ? <p className="mt-5 border-l-2 border-red-600 bg-red-50 px-3 py-2 text-sm text-red-800" role="alert">{error}</p> : null}
      {notice ? <p className="mt-5 border-l-2 border-brand bg-[#f0f6f2] px-3 py-2 text-sm text-[#245b3f]" role="status">{notice}</p> : null}

      {operationalEditable ? (
        <div className="mt-6 flex justify-end border-t border-line pt-5">
          <button className="flex h-10 items-center gap-2 bg-ink px-4 text-sm font-semibold text-white hover:bg-brand disabled:cursor-not-allowed disabled:opacity-50" disabled={submitting || loadingOptions} type="submit"><Save size={16} />{submitting ? "저장 중" : creating ? "자산 등록" : "변경사항 저장"}</button>
        </div>
      ) : <p className="mt-6 text-sm text-muted">자산 정보를 조회할 수 있으며 변경 권한은 없습니다.</p>}
    </form>
  );
}
