import Link from "next/link";
import type { ReactNode } from "react";

const matrix = [
  1, 1, 1, 0, 1, 0,
  1, 0, 1, 0, 0, 1,
  1, 1, 1, 0, 1, 1,
  0, 0, 0, 0, 1, 0,
  1, 1, 0, 1, 0, 1,
  0, 1, 1, 0, 1, 1,
];

type AuthShellProps = {
  children: ReactNode;
  eyebrow: string;
  title: string;
  description: string;
};

export function AuthShell({ children, eyebrow, title, description }: AuthShellProps) {
  return (
    <main className="min-h-screen bg-white lg:grid lg:grid-cols-[minmax(250px,32vw)_1fr]">
      <aside className="relative flex min-h-48 overflow-hidden bg-[#17221b] px-6 py-6 text-white lg:min-h-screen lg:flex-col lg:px-9 lg:py-8">
        <Link href="/login" className="relative z-10 text-lg font-semibold">
          Assetly<span className="text-[#e68a45]">.</span>
        </Link>
        <div className="absolute right-6 top-6 grid grid-cols-6 gap-1 opacity-40 lg:left-9 lg:right-auto lg:top-1/2 lg:-translate-y-1/2">
          {matrix.map((cell, index) => (
            <span className={`h-3 w-3 ${cell ? "bg-white" : "border border-white/25"}`} key={index} />
          ))}
        </div>
        <div className="relative z-10 mt-auto hidden lg:block">
          <p className="text-5xl font-semibold leading-none text-white/10">A-01</p>
          <p className="mt-4 text-xs font-semibold uppercase text-white/55">Asset operations</p>
          <p className="mt-1 text-sm text-white/80">QR 기반 자산 운영 시스템</p>
        </div>
      </aside>
      <section className="flex min-h-[calc(100vh-12rem)] items-center px-6 py-12 sm:px-10 lg:min-h-screen lg:px-16">
        <div className="w-full max-w-[420px] lg:ml-[8vw]">
          <p className="text-xs font-semibold uppercase text-brand">{eyebrow}</p>
          <h1 className="mt-3 text-[32px] font-semibold leading-tight text-ink">{title}</h1>
          <p className="mt-3 text-sm leading-6 text-muted">{description}</p>
          <div className="mt-9">{children}</div>
        </div>
      </section>
    </main>
  );
}
