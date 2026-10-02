import { expect, test, type APIRequestContext, type Page } from "@playwright/test";

const password = "Assetly!234";

type ApiEnvelope<T> = { data: T };

type QrFixture = {
  accessToken: string;
  email: string;
  organizationId: number;
  publicCode: string;
};

async function responseData<T>(response: Awaited<ReturnType<APIRequestContext["post"]>>) {
  expect(response.ok()).toBeTruthy();
  return ((await response.json()) as ApiEnvelope<T>).data;
}

async function createQrFixture(request: APIRequestContext): Promise<QrFixture> {
  const suffix = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  const email = `qr-e2e-${suffix}@example.com`;

  await responseData(await request.post("/api/auth/signup", {
    data: { email, password, name: "QR Owner" },
  }));

  const login = await responseData<{ accessToken: string }>(await request.post("/api/auth/login", {
    data: { email, password },
  }));
  const headers = { Authorization: `Bearer ${login.accessToken}` };

  const organization = await responseData<{ id: number }>(await request.post("/api/organizations", {
    data: { name: "QR Operations", description: "QR browser workflow" },
    headers,
  }));
  const firstLocation = await responseData<{ id: number }>(await request.post(`/api/organizations/${organization.id}/locations`, {
    data: { name: "Storage", description: "Initial location" },
    headers,
  }));
  await responseData(await request.post(`/api/organizations/${organization.id}/locations`, {
    data: { name: "Repair Desk", description: "Target location" },
    headers,
  }));
  const asset = await responseData<{ publicCode: string }>(await request.post(`/api/organizations/${organization.id}/assets`, {
    data: {
      assetCode: `QR-${suffix}`,
      name: "Field Camera",
      description: "QR workflow asset",
      categoryId: null,
      locationId: firstLocation.id,
      assignedUserId: null,
      status: "AVAILABLE",
      purchaseDate: null,
      purchasePrice: null,
    },
    headers,
  }));

  return {
    accessToken: login.accessToken,
    email,
    organizationId: organization.id,
    publicCode: asset.publicCode,
  };
}

async function authenticate(page: Page, accessToken: string) {
  await page.addInitScript((token) => localStorage.setItem("assetly_token", token), accessToken);
}

test("비로그인 QR 접근 후 로그인하면 원래 자산 화면으로 복귀한다", async ({ page, request }, testInfo) => {
  test.skip(testInfo.project.name !== "desktop", "데스크톱 프로젝트에서만 실행");
  const fixture = await createQrFixture(request);
  const assetPath = `/a/${fixture.publicCode}`;

  await page.goto(assetPath);
  await expect(page).toHaveURL(new RegExp(`/login\\?next=%2Fa%2F${fixture.publicCode}$`));
  await page.getByLabel("이메일").fill(fixture.email);
  await page.getByLabel("비밀번호").fill(password);
  await page.getByRole("button", { name: "로그인" }).click();

  await expect(page).toHaveURL(new RegExp(`${assetPath}$`));
  await expect(page.getByRole("heading", { name: "Field Camera" })).toBeVisible();
  await expect(page.getByText("QR ASSET · QR Operations")).toBeVisible();
  await expect.poll(() => page.evaluate(() => localStorage.getItem("assetly_organization_id")))
    .toBe(String(fixture.organizationId));
});

test("모바일 QR 화면에서 상태·위치·담당자를 변경하고 이력을 확인한다", async ({ page, request }, testInfo) => {
  test.skip(testInfo.project.name !== "mobile", "모바일 프로젝트에서만 실행");
  const fixture = await createQrFixture(request);
  await authenticate(page, fixture.accessToken);
  await page.goto(`/a/${fixture.publicCode}`);

  await expect(page.getByRole("heading", { name: "Field Camera" })).toBeVisible();
  await page.getByLabel("상태").selectOption("REPAIR");
  await page.getByLabel("위치").selectOption({ label: "Repair Desk" });
  await page.getByLabel("담당자").selectOption({ label: `QR Owner (${fixture.email})` });
  await page.getByRole("button", { name: "변경사항 저장" }).click();

  await expect(page.getByRole("status")).toHaveText("자산 정보를 저장했습니다.");
  await expect(page.getByLabel("상태")).toHaveValue("REPAIR");
  await expect(page.getByLabel("위치").locator("option:checked")).toHaveText("Repair Desk");
  await expect(page.getByLabel("담당자").locator("option:checked")).toContainText("QR Owner");
  await expect(page.getByText("상태 변경", { exact: true })).toBeVisible();
  await expect(page.getByText("위치 변경", { exact: true })).toBeVisible();
  await expect(page.getByText("담당자 변경", { exact: true })).toBeVisible();
});
