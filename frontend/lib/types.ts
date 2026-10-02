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

export type AssetStatus = "AVAILABLE" | "IN_USE" | "REPAIR" | "LOST" | "DISPOSED";

export type AssignableUser = {
  userId: number;
  name: string;
  email: string;
};

export type Asset = {
  id: number;
  publicCode: string;
  assetCode: string;
  name: string;
  description: string | null;
  status: AssetStatus;
  category: { id: number; name: string } | null;
  location: { id: number; name: string } | null;
  assignedUser: { id: number; name: string; email: string } | null;
  purchaseDate: string | null;
  purchasePrice: number | null;
  createdAt: string;
  updatedAt: string;
};

export type AssetPage = {
  items: Asset[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type LoginResponse = {
  accessToken: string;
  user: { id: number; email: string; name: string };
};
