"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { clearAccessToken } from "./api";

export function useSessionExpiry() {
  const router = useRouter();
  useEffect(() => {
    function expired() {
      const destination = window.location.pathname + window.location.search;
      clearAccessToken();
      router.replace(`/login?next=${encodeURIComponent(destination)}`);
    }
    window.addEventListener("assetly:session-expired", expired);
    return () => window.removeEventListener("assetly:session-expired", expired);
  }, [router]);
}
