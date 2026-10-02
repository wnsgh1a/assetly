"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useCallback, useEffect, useState, type ReactNode } from "react";
import { Boxes, LogOut, MapPinned, Settings, Users } from "lucide-react";
import { ApiError, apiRequest, clearAccessToken } from "@/lib/api";
import type { Organization } from "@/lib/types";

type WorkspaceShellProps = {
  eyebrow: string;
  title: string;
  children: (
    organization: Organization,
    updateOrganization: (organization: Organization) => void,
  ) => ReactNode;
};

const navigation = [
  { href: "/app", label: "대시보드", icon: Boxes, restricted: false },
  { href: "/app/classification", label: "위치·카테고리", icon: MapPinned, restricted: false },
  { href: "/app/members", label: "멤버", icon: Users, restricted: true },
  { href: "/app/settings", label: "조직 설정", icon: Settings, restricted: false },
] as const;

export function WorkspaceShell({ eyebrow, title, children }: WorkspaceShellProps) {
  const pathname = usePathname();
  const router = useRouter();
  const [organization, setOrganization] = useState<Organization>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadOrganization = useCallback(() => {
    setLoading(true);
    setError("");
    apiRequest<Organization[]>("/organizations")
      .then((items) => {
        if (!items.length) return router.replace("/onboarding/organization");
        setOrganization(items[0]);
      })
      .catch((caught) => {
        if (caught instanceof ApiError && caught.status === 401) {
          clearAccessToken();
          return router.replace(`/login?next=${encodeURIComponent(pathname)}`);
        }
        setError(caught instanceof ApiError ? caught.message : "워크스페이스를 불러오지 못했습니다.");
      })
      .finally(() => setLoading(false));
  }, [pathname, router]);

  useEffect(() => {
    loadOrganization();
  }, [loadOrganization]);

  if (loading) {
    return (
      <main className="grid min-h-screen place-items-center bg-panel">
        <div className="flex items-center gap-3 text-sm text-muted">
          <span className="h-2 w-2 animate-pulse bg-brand" />
          워크스페이스 불러오는 중
        </div>
      </main>
    );
  }

  if (error || !organization) {
    return (
      <main className="grid min-h-screen place-items-center bg-panel px-6 text-center">
        <div>
          <p className="text-sm font-medium">워크스페이스를 불러오지 못했습니다</p>
          <p className="mt-2 text-sm text-muted">{error}</p>
          <button className="mt-5 border border-line bg-white px-4 py-2 text-sm font-medium" onClick={loadOrganization} type="button">다시 시도</button>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-panel md:grid md:grid-cols-[216px_1fr]">
      <aside className="relative border-b border-line bg-[#202622] text-white md:min-h-screen md:border-b-0 md:border-r md:border-r-black/20">
        <div className="flex h-16 items-center border-b border-white/10 px-5">
          <p className="text-lg font-semibold">Assetly<span className="text-[#e68a45]">.</span></p>
        </div>
        <div className="border-b border-white/10 px-5 py-4">
          <span className="block truncate text-sm font-medium">{organization.name}</span>
          <span className="mt-0.5 block text-[11px] text-white/50">{organization.myRole}</span>
        </div>
        <nav className="flex gap-1 overflow-x-auto p-3 md:block md:space-y-1 md:overflow-visible">
          {navigation
            .filter((item) => !item.restricted || ["OWNER", "ADMIN"].includes(organization.myRole))
            .map(({ href, label, icon: Icon }) => {
              const active = pathname === href;
              return (
                <Link className={`flex shrink-0 items-center gap-3 px-3 py-2.5 text-sm transition-colors md:w-full ${active ? "bg-white/10 font-medium text-white" : "text-white/55 hover:bg-white/5 hover:text-white"}`} href={href} key={href}>
                  <Icon size={17} />
                  {label}
                </Link>
              );
            })}
        </nav>
        <button className="m-3 mt-0 flex items-center gap-3 px-3 py-2.5 text-sm text-white/55 transition-colors hover:text-white md:absolute md:bottom-3 md:left-0 md:w-[192px]" onClick={() => { clearAccessToken(); router.replace("/login"); }} type="button">
          <LogOut size={17} />
          로그아웃
        </button>
      </aside>

      <div className="min-w-0">
        <header className="flex h-16 items-center justify-between border-b border-line bg-white px-5 md:px-8">
          <div>
            <p className="text-xs font-medium text-muted">{eyebrow}</p>
            <h1 className="mt-0.5 text-base font-semibold">{title}</h1>
          </div>
          <div className="grid h-9 w-9 place-items-center bg-[#dce9df] text-xs font-semibold text-[#24573d]" title={organization.myRole}>
            {organization.name.slice(0, 1)}
          </div>
        </header>
        {children(organization, setOrganization)}
      </div>
    </main>
  );
}
