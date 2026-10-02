export type MemberRole = "OWNER" | "ADMIN" | "MANAGER" | "MEMBER";

export type Organization = {
  id: number;
  name: string;
  description: string | null;
  myRole: MemberRole;
};

export type OrganizationMember = {
  id: number;
  userId: number;
  email: string;
  name: string;
  role: MemberRole;
  joinedAt: string;
};

export type AssetCategory = {
  id: number;
  name: string;
  createdAt: string;
  updatedAt: string;
};

export type AssetLocation = AssetCategory & {
  description: string | null;
};

export type LoginResponse = {
  accessToken: string;
  user: { id: number; email: string; name: string };
};
