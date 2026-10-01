import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        ink: "#1C211E",
        muted: "#69716B",
        line: "#D9DDD8",
        panel: "#F3F4F1",
        brand: "#256847",
        accent: "#C76B29",
      },
    },
  },
  plugins: [],
};

export default config;
