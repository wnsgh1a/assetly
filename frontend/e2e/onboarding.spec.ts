import { expect, test } from "@playwright/test";

const password = "Assetly!234";

test("신규 사용자가 가입하고 첫 조직을 만들어 빈 대시보드에 진입한다", async ({
  page,
}, testInfo) => {
  const suffix = `${testInfo.project.name}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  const email = `onboarding-${suffix}@example.com`;
  const organizationName = `Onboarding ${testInfo.project.name} ${suffix.slice(-6)}`;

  await page.goto("/signup");
  await page.getByLabel("이름").fill("Onboarding Owner");
  await page.getByLabel("이메일").fill(email);
  await page.getByLabel("비밀번호").fill(password);
  await page.getByRole("button", { name: "계정 만들기" }).click();

  await expect(page).toHaveURL(/\/login\?registered=1$/);
  await expect(page.getByRole("status")).toHaveText(
    "가입이 완료되었습니다. 로그인해 주세요.",
  );

  await page.getByLabel("이메일").fill(email);
  await page.getByLabel("비밀번호").fill(password);
  await page.getByRole("button", { name: "로그인" }).click();

  await expect(page).toHaveURL(/\/onboarding\/organization$/);
  await expect(
    page.getByRole("heading", { name: "관리할 조직을 등록하세요" }),
  ).toBeVisible();
  await expect
    .poll(() => page.evaluate(() => localStorage.getItem("assetly_token")))
    .not.toBeNull();

  await page.getByLabel("조직 이름").fill(organizationName);
  await page.getByLabel("설명 선택").fill("Playwright 신규 사용자 온보딩 조직");
  await page.getByRole("button", { name: "워크스페이스 만들기" }).click();

  await expect(page).toHaveURL(/\/app$/);
  await expect(page.getByRole("heading", { name: "자산 현황" })).toBeVisible();
  await expect(
    page.getByLabel("조직 선택").locator("option:checked"),
  ).toHaveText(`${organizationName} · OWNER`);
  await expect(
    page
      .getByText("전체 자산", { exact: true })
      .locator("..")
      .getByText("0", { exact: true }),
  ).toBeVisible();
  await expect(page.getByText("등록된 자산이 없습니다")).toBeVisible();
  await expect
    .poll(() =>
      page.evaluate(() => localStorage.getItem("assetly_organization_id")),
    )
    .not.toBeNull();

  await page.getByRole("button", { name: "로그아웃" }).click();
  await expect(page).toHaveURL(/\/login$/);
  await expect
    .poll(() => page.evaluate(() => localStorage.getItem("assetly_token")))
    .toBeNull();
  await expect
    .poll(() =>
      page.evaluate(() => localStorage.getItem("assetly_organization_id")),
    )
    .toBeNull();

  await page.goto("/app");
  await expect(page).toHaveURL(/\/login\?next=%2Fapp$/);
});
