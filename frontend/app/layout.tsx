import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Assetly",
  description: "QR based asset management SaaS for small teams.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
