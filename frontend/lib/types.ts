export type Organization = {
  id: number;
  name: string;
  description: string | null;
  myRole: "OWNER" | "ADMIN" | "MANAGER" | "MEMBER";
};

export type LoginResponse = {
  accessToken: string;
  user: { id: number; email: string; name: string };
};
