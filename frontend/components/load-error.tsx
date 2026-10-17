import { RotateCw } from "lucide-react";

export function LoadError({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <div className="my-5 flex flex-wrap items-center justify-between gap-3 border-l-2 border-red-600 bg-red-50 px-3 py-3 text-sm text-red-800" role="alert">
      <p className="min-w-0 break-words">{message}</p>
      <button className="inline-flex min-h-9 shrink-0 items-center gap-2 border border-red-200 bg-white px-3 font-medium hover:bg-red-50" onClick={onRetry} type="button"><RotateCw size={15} />다시 시도</button>
    </div>
  );
}
