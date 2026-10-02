import { expect, test, type APIRequestContext, type Page } from "@playwright/test";

const password = "Assetly!234";

async function createWorkspaceFixture(request: APIRequestContext) {
  const suffix = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  const email = `e2e-${suffix}@example.com`;

  await expect((await request.post("/api/auth/signup", {
    data: { email, password, name: "E2E User" },
  })).ok()).toBeTruthy();

  const loginResponse = await request.post("/api/auth/login", { data: { email, password } });
  expect(loginResponse.ok()).toBeTruthy();
  const loginBody = await loginResponse.json();
  const headers = { Authorization: `Bearer ${loginBody.data.accessToken}` };

  for (const name of ["Alpha Lab", "Beta Office"]) {
    const response = await request.post("/api/organizations", {
      data: { name, description: `${name} workspace` },
      headers,
    });
    expect(response.ok()).toBeTruthy();
  }

  return { accessToken: loginBody.data.accessToken as string, email };
}

async function openWorkspace(page: Page, accessToken: string) {
  await page.addInitScript((token) => localStorage.setItem("assetly_token", token), accessToken);
  await page.goto("/app");
  await expect(page).toHaveURL(/\/app$/);
}

test("조직을 전환하고 새로고침 후에도 선택을 유지한다", async ({ page, request }) => {
  const { accessToken } = await createWorkspaceFixture(request);
  await openWorkspace(page, accessToken);

  const switcher = page.getByLabel("조직 선택");
  await expect(switcher).toContainText("Alpha Lab · OWNER");
  await expect(switcher).toContainText("Beta Office · OWNER");

  await switcher.selectOption({ label: "Beta Office · OWNER" });
  await expect(page.getByText("Beta Office", { exact: true })).toBeVisible();
  await page.reload();
  await expect(switcher).toHaveValue(/\d+/);
  await expect(switcher.locator("option:checked")).toHaveText("Beta Office · OWNER");

  await switcher.selectOption("new");
  await expect(page).toHaveURL(/\/onboarding\/organization$/);
  await expect(page.getByRole("heading", { name: "관리할 조직을 등록하세요" })).toBeVisible();
});

test("모바일에서 조직 선택과 주요 내비게이션을 사용할 수 있다", async ({ page, request }, testInfo) => {
  test.skip(testInfo.project.name !== "mobile", "모바일 프로젝트에서만 실행");
  const { accessToken } = await createWorkspaceFixture(request);
  await openWorkspace(page, accessToken);

  await expect(page.getByLabel("조직 선택")).toBeVisible();
  await expect(page.getByRole("link", { name: "자산", exact: true })).toBeVisible();
  await expect(page.getByRole("link", { name: "변경 이력" })).toBeVisible();
  await expect(page.getByRole("button", { name: "로그아웃" })).toBeVisible();
});

test("로그인하고 로그아웃하면 보호 화면 접근을 차단한다", async ({ page, request }, testInfo) => {
  test.skip(testInfo.project.name !== "desktop", "데스크톱 프로젝트에서만 실행");
  const { email } = await createWorkspaceFixture(request);

  await page.goto("/login");
  await page.getByLabel("이메일").fill(email);
  await page.getByLabel("비밀번호").fill(password);
  await page.getByRole("button", { name: "로그인" }).click();
  await expect(page).toHaveURL(/\/app$/);
  await expect(page.getByLabel("조직 선택")).toBeVisible();

  await page.getByRole("button", { name: "로그아웃" }).click();
  await expect.poll(() => page.evaluate(() => localStorage.getItem("assetly_token"))).toBeNull();
  await expect(page).toHaveURL(/\/login$/, { timeout: 15_000 });
  await page.goto("/app");
  await expect(page).toHaveURL(/\/login\?next=%2Fapp$/);
});
