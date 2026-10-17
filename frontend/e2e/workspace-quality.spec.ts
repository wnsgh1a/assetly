import { expect, test, type APIRequestContext, type Page } from "@playwright/test";

async function fixture(request: APIRequestContext) {
  const suffix = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
  const email = `quality-${suffix}@example.com`;
  async function api<T>(path: string, data?: object, token?: string): Promise<T> {
    const response = await request.post(`/api${path}`, {
      data, headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    expect(response.ok()).toBeTruthy();
    return (await response.json()).data as T;
  }
  await api("/auth/signup", { email, password: "Assetly!234", name: "Quality Owner" });
  const { accessToken } = await api<{ accessToken: string }>("/auth/login", { email, password: "Assetly!234" });
  const organization = await api<{ id: number }>("/organizations", { name: `Quality ${suffix}`, description: "Browser quality checks" }, accessToken);
  const asset = await api<{ id: number; publicCode: string }>(`/organizations/${organization.id}/assets`, {
    name: "Quality Camera", assetCode: `QA-${suffix}`, description: "Test asset",
    status: "AVAILABLE", categoryId: null, locationId: null, assignedUserId: null,
    purchaseDate: null, purchasePrice: null,
  }, accessToken);
  const second = await api<{ id: number }>("/organizations", { name: `Second ${suffix}` }, accessToken);
  return { accessToken, organizationId: organization.id, secondId: second.id, asset };
}

async function login(page: Page, data: Awaited<ReturnType<typeof fixture>>) {
  await page.addInitScript(({ accessToken, organizationId }) => {
    localStorage.setItem("assetly_token", accessToken);
    localStorage.setItem("assetly_organization_id", String(organizationId));
  }, data);
}

test("자산 목록 오류를 빈 목록과 구분하고 재시도와 검색 초기화가 동작한다", async ({ page, request }) => {
  const data = await fixture(request);
  await login(page, data);
  let failing = true;
  await page.route(`**/api/organizations/${data.organizationId}/assets?*`, (route) => failing
    ? route.fulfill({ status: 503, json: { error: { message: "목록 조회 실패" } } }) : route.continue());
  await page.goto("/app/assets");
  await expect(page.getByRole("alert").filter({ hasText: "목록 조회 실패" })).toBeVisible();
  await expect(page.getByText("조건에 맞는 자산이 없습니다")).toHaveCount(0);
  failing = false;
  await page.getByRole("button", { name: "다시 시도" }).click();
  await expect(page.getByRole("link", { name: /Quality Camera/ })).toBeVisible();
  await page.getByRole("textbox", { name: "검색어" }).fill("no-such-asset");
  await page.getByRole("button", { name: "조회", exact: true }).click();
  await expect(page.getByText("조건에 맞는 자산이 없습니다")).toBeVisible();
  await page.getByRole("button", { name: "검색 조건 초기화" }).click();
  await expect(page.getByRole("link", { name: /Quality Camera/ })).toBeVisible();
});

test("선택 항목 오류 시 저장을 차단하고 재시도 후 입력과 저장을 유지한다", async ({ page, request }) => {
  const data = await fixture(request);
  await login(page, data);
  let failing = true;
  await page.route(`**/api/organizations/${data.organizationId}/categories`, (route) => failing
    ? route.fulfill({ status: 503, json: { error: { message: "선택 항목 조회 실패" } } }) : route.continue());
  await page.goto(`/app/assets/${data.asset.id}`);
  await expect(page.getByRole("alert").filter({ hasText: "선택 항목 조회 실패" })).toBeVisible();
  await expect(page.getByRole("button", { name: "변경사항 저장" })).toBeDisabled();
  await page.getByLabel("자산명", { exact: true }).fill("Draft Camera");
  failing = false;
  await page.getByRole("button", { name: "다시 시도" }).click();
  await expect(page.getByRole("button", { name: "변경사항 저장" })).toBeEnabled();
  await expect(page.getByLabel("자산명", { exact: true })).toHaveValue("Draft Camera");
  await page.getByLabel("자산명", { exact: true }).fill("   ");
  await page.getByRole("button", { name: "변경사항 저장" }).click();
  await expect(page.getByRole("alert").filter({ hasText: "공백만 입력할 수 없습니다" })).toBeVisible();
  await page.getByLabel("자산명", { exact: true }).fill("Saved Camera");
  await page.getByRole("button", { name: "변경사항 저장" }).click();
  await expect(page.getByRole("status")).toHaveText("자산 정보를 저장했습니다.");
});

test("이력 실패는 빈 기록으로 표시하지 않으며 QR 화면에서도 재시도한다", async ({ page, request }) => {
  const data = await fixture(request);
  await login(page, data);
  let failing = true;
  await page.route("**/api/organizations/*/assets/*/histories?*", (route) => failing
    ? route.fulfill({ status: 503, json: { error: { message: "이력 조회 실패" } } }) : route.continue());
  await page.goto(`/a/${data.asset.publicCode}`);
  await expect(page.getByRole("alert").filter({ hasText: "이력 조회 실패" })).toBeVisible();
  await expect(page.getByText("기록된 활동이 없습니다")).toHaveCount(0);
  failing = false;
  await page.getByRole("button", { name: "다시 시도" }).click();
  await expect(page.getByText("등록", { exact: true })).toBeVisible();
});

test("대시보드 조회 오류를 자산 0개로 표시하지 않는다", async ({ page, request }) => {
  const data = await fixture(request);
  await login(page, data);
  let failing = true;
  await page.route("**/api/organizations/*/dashboard", (route) => failing
    ? route.fulfill({ status: 503, json: { error: { message: "현황 조회 실패" } } }) : route.continue());
  await page.goto("/app");
  await expect(page.getByRole("alert").filter({ hasText: "현황 조회 실패" })).toBeVisible();
  await expect(page.getByText("전체 자산", { exact: true })).toHaveCount(0);
  await expect(page.getByText("등록된 자산이 없습니다")).toHaveCount(0);
  failing = false;
  await page.getByRole("button", { name: "다시 시도" }).click();
  await expect(page.getByText("전체 자산", { exact: true })).toBeVisible();
});

test("자산 상세에서 다른 조직으로 전환하면 이전 자산 폼을 제거한다", async ({ page, request }) => {
  const data = await fixture(request);
  await login(page, data);
  await page.goto(`/app/assets/${data.asset.id}`);
  await expect(page.getByLabel("자산명", { exact: true })).toHaveValue("Quality Camera");
  await page.getByLabel("조직 선택").selectOption(String(data.secondId));
  await expect(page.getByRole("alert").filter({ has: page.getByRole("button", { name: "다시 시도" }) })).toBeVisible();
  await expect(page.getByLabel("자산명", { exact: true })).toHaveCount(0);
});

test("작업 도중 인증이 만료되면 원래 주소를 보존하고 로그인으로 이동한다", async ({ page, request }) => {
  const data = await fixture(request);
  await login(page, data);
  await page.goto(`/app/assets/${data.asset.id}`);
  await expect(page.getByRole("button", { name: "변경사항 저장" })).toBeEnabled();
  await page.route(`**/api/organizations/${data.organizationId}/assets/${data.asset.id}`, (route) => route.request().method() === "PATCH"
    ? route.fulfill({ status: 401, json: { error: { message: "인증 만료" } } }) : route.continue());
  await page.getByRole("button", { name: "변경사항 저장" }).click();
  await expect(page).toHaveURL(new RegExp(`/login\\?next=%2Fapp%2Fassets%2F${data.asset.id}$`));
  expect(await page.evaluate(() => localStorage.getItem("assetly_token"))).toBeNull();
});

test("긴 자산명과 주요 화면은 모바일·태블릿·데스크톱에서 페이지 폭을 넘지 않는다", async ({ page, request }, testInfo) => {
  test.skip(testInfo.project.name !== "desktop", "한 프로젝트에서 네 가지 화면 폭을 점검");
  test.setTimeout(120_000);
  const data = await fixture(request);
  const updated = await request.patch(`/api/organizations/${data.organizationId}/assets/${data.asset.id}`, {
    headers: { Authorization: `Bearer ${data.accessToken}` },
    data: { name: "Equipment".repeat(16), assetCode: "ASSET".repeat(20), status: "AVAILABLE" },
  });
  expect(updated.ok()).toBeTruthy();
  await login(page, data);
  const paths = ["/app", "/app/assets", "/app/assets/new", `/app/assets/${data.asset.id}`, "/app/history", "/app/classification", "/app/members", "/app/settings", `/a/${data.asset.publicCode}`];
  for (const width of [360, 768, 1024, 1440]) {
    await page.setViewportSize({ width, height: 900 });
    for (const path of paths) {
      await page.goto(path);
      await expect(page.getByRole("heading").first()).toBeVisible();
      await expect.poll(() => page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth), { message: `${path} must fit ${width}px` }).toBeTruthy();
      if (path === `/app/assets/${data.asset.id}`) {
        await expect(page.getByLabel("자산명", { exact: true })).toHaveValue("Equipment".repeat(16));
        await expect.poll(() => page.getByRole("button", { name: "비활성화", exact: true }).evaluate((element) => element.scrollHeight <= element.clientHeight && element.getBoundingClientRect().width >= 100)).toBeTruthy();
        await expect(page.getByText("이력을 불러오는 중입니다.", { exact: true })).toHaveCount(0);
        await page.screenshot({ path: testInfo.outputPath(`asset-${width}.png`), fullPage: true });
      }
    }
  }
});

test("자산 이력을 20개 넘게 생성하면 이전 기록까지 페이지로 조회한다", async ({ page, request }) => {
  const data = await fixture(request);
  const headers = { Authorization: `Bearer ${data.accessToken}` };
  const path = `/api/organizations/${data.organizationId}/assets/${data.asset.id}`;
  const current = (await (await request.get(path, { headers })).json()).data;
  for (let index = 0; index < 21; index++) {
    const response = await request.patch(path, { headers, data: { ...current, name: `History revision ${index}`, categoryId: null, locationId: null, assignedUserId: null } });
    expect(response.ok()).toBeTruthy();
  }
  await login(page, data);
  await page.goto(`/app/assets/${data.asset.id}`);
  await expect(page.getByText("등록", { exact: true })).toHaveCount(0);
  await page.getByRole("button", { name: "자산 이력 다음 페이지" }).click();
  await expect(page.getByText("등록", { exact: true })).toBeVisible();
  await page.getByRole("button", { name: "자산 이력 이전 페이지" }).click();
  await expect(page.getByText("등록", { exact: true })).toHaveCount(0);
});

test("기준 정보와 멤버 목록 실패를 빈 상태와 구분하고 복구한다", async ({ page, request }) => {
  const data = await fixture(request);
  await login(page, data);
  for (const [endpoint, path, emptyText] of [
    ["locations", "/app/classification", "등록된 위치가 없습니다."],
    ["members", "/app/members", "등록된 멤버가 없습니다."],
    ["histories?*", "/app/history", "기록된 활동이 없습니다"],
  ]) {
    let failing = true;
    await page.route(`**/api/organizations/${data.organizationId}/${endpoint}`, (route) => failing
      ? route.fulfill({ status: 503, json: { error: { message: "목록 일시 중단" } } }) : route.continue());
    await page.goto(path);
    await expect(page.getByRole("alert").filter({ hasText: "목록 일시 중단" })).toBeVisible();
    await expect(page.getByText(emptyText, { exact: true })).toHaveCount(0);
    failing = false;
    await page.getByRole("button", { name: "다시 시도" }).click();
    await expect(page.getByRole("alert").filter({ hasText: "목록 일시 중단" })).toHaveCount(0);
    if (endpoint === "locations") await expect(page.getByText(emptyText, { exact: true })).toBeVisible();
    if (endpoint === "members") await expect(page.getByRole("cell", { name: "Quality Owner", exact: true })).toBeVisible();
  }
});

test("화면에서 자산을 등록·수정하고 QR을 내려받은 뒤 비활성화한다", async ({ page, request }) => {
  const data = await fixture(request);
  await login(page, data);
  const code = `UI-${Date.now()}`;
  await page.goto("/app/assets/new");
  await page.getByLabel("자산번호", { exact: true }).fill(code);
  await page.getByLabel("자산명", { exact: true }).fill("UI Test Monitor");
  await page.getByRole("button", { name: "자산 등록" }).click();
  await expect(page).toHaveURL(/\/app\/assets\/\d+$/);
  await expect(page.getByLabel("자산명", { exact: true })).toHaveValue("UI Test Monitor");
  const downloadPromise = page.waitForEvent("download");
  await page.getByRole("button", { name: "QR SVG 다운로드" }).click();
  const download = await downloadPromise;
  expect(download.suggestedFilename()).toBe(`${code}-qr.svg`);
  const stream = await download.createReadStream();
  const chunks = [];
  for await (const chunk of stream) chunks.push(chunk);
  expect(Buffer.concat(chunks).toString()).toContain("<svg");
  await page.getByLabel("자산명", { exact: true }).fill("Updated UI Monitor");
  await page.getByRole("button", { name: "변경사항 저장" }).click();
  await expect(page.getByRole("status")).toHaveText("자산 정보를 저장했습니다.");
  await page.reload();
  await expect(page.getByLabel("자산명", { exact: true })).toHaveValue("Updated UI Monitor");
  page.once("dialog", (dialog) => dialog.accept());
  await page.getByRole("button", { name: "비활성화", exact: true }).click();
  await expect(page).toHaveURL(/\/app\/assets$/);
  await expect(page.getByRole("link", { name: /Updated UI Monitor/ })).toHaveCount(0);
});
