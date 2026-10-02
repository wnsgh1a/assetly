"use client";

import { useEffect, useState } from "react";
import { Check, Copy, Download } from "lucide-react";
import { QRCodeSVG } from "qrcode.react";

export function AssetQr({ publicCode, assetCode }: { publicCode: string; assetCode: string }) {
  const [url, setUrl] = useState("");
  const [copied, setCopied] = useState(false);
  const qrId = `asset-qr-${publicCode}`;

  useEffect(() => setUrl(`${window.location.origin}/a/${publicCode}`), [publicCode]);

  async function copy() {
    await navigator.clipboard.writeText(url);
    setCopied(true);
    window.setTimeout(() => setCopied(false), 1500);
  }

  function download() {
    const svg = document.getElementById(qrId);
    if (!svg) return;
    const blob = new Blob([new XMLSerializer().serializeToString(svg)], { type: "image/svg+xml" });
    const anchor = document.createElement("a");
    anchor.href = URL.createObjectURL(blob);
    anchor.download = `${assetCode}-qr.svg`;
    anchor.click();
    URL.revokeObjectURL(anchor.href);
  }

  return (
    <section className="border-t border-line bg-white py-5">
      <div className="flex items-start gap-5">
        <div className="grid h-36 w-36 shrink-0 place-items-center border border-line bg-white p-2">
          {url ? <QRCodeSVG id={qrId} value={url} size={124} level="M" marginSize={1} /> : null}
        </div>
        <div className="min-w-0 pt-1">
          <h3 className="text-sm font-semibold">현장 접근 QR</h3>
          <p className="mt-2 break-all text-xs leading-5 text-muted">{url}</p>
          <div className="mt-4 flex gap-2">
            <button aria-label="QR 링크 복사" className="grid h-9 w-9 place-items-center border border-line hover:bg-panel disabled:opacity-50" disabled={!url} onClick={() => void copy()} title="QR 링크 복사" type="button">{copied ? <Check size={16} /> : <Copy size={16} />}</button>
            <button aria-label="QR SVG 다운로드" className="grid h-9 w-9 place-items-center border border-line hover:bg-panel disabled:opacity-50" disabled={!url} onClick={download} title="QR SVG 다운로드" type="button"><Download size={16} /></button>
          </div>
        </div>
      </div>
    </section>
  );
}
