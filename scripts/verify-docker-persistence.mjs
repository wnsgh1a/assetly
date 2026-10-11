import assert from "node:assert/strict";
import { execFileSync } from "node:child_process";
import { randomUUID } from "node:crypto";
import { fileURLToPath } from "node:url";

const project = process.env.COMPOSE_PROJECT_NAME;
assert.match(
  project ?? "",
  /^assetly-ci-\d+-\d+$/,
  "Use an isolated CI Compose project",
);
const baseUrl = process.env.PLAYWRIGHT_BASE_URL ?? "http://127.0.0.1:3100";
const workspace = fileURLToPath(new URL("../", import.meta.url));
const suffix = randomUUID();
const email = `persistence-${suffix}@example.com`;
const password = `Assetly!${suffix}`;
let token;

async function api(path, method = "GET", body) {
  const response = await fetch(new URL(`/api${path}`, baseUrl), {
    method,
    headers: {
      "Content-Type": "application/json",
      // The server is recreated mid-test, so no socket may survive across requests.
      Connection: "close",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(30_000),
  });
  assert.equal(response.ok, true, `${method} ${path}: HTTP ${response.status}`);
  return (await response.json()).data;
}

function compose(...args) {
  execFileSync("docker", ["compose", ...args], {
    cwd: workspace,
    stdio: "inherit",
    timeout: 240_000,
  });
}

await api("/auth/signup", "POST", {
  email,
  password,
  name: "Persistence Owner",
});
token = (await api("/auth/login", "POST", { email, password })).accessToken;
const organization = await api("/organizations", "POST", {
  name: `Persistence ${suffix}`,
  description: "Container recreation verification",
});
const location = await api(
  `/organizations/${organization.id}/locations`,
  "POST",
  {
    name: "Storage",
    description: "Persistent location",
  },
);
const asset = await api(`/organizations/${organization.id}/assets`, "POST", {
  assetCode: `PERSIST-${suffix}`,
  name: "Persistent Camera",
  description: "Must survive container recreation",
  categoryId: null,
  locationId: location.id,
  assignedUserId: null,
  status: "AVAILABLE",
  purchaseDate: null,
  purchasePrice: null,
});

console.log(
  "Created account, organization, location and asset; recreating containers.",
);
compose("down");
compose("up", "--no-build", "--wait", "--wait-timeout", "180");

// A fresh login proves that the user record survives too.
token = undefined;
token = (await api("/auth/login", "POST", { email, password })).accessToken;
const organizations = await api("/organizations");
assert.equal(organizations.length, 1);
assert.equal(organizations[0].id, organization.id);
assert.equal(organizations[0].name, organization.name);
assert.equal(organizations[0].myRole, "OWNER");
const saved = await api(`/organizations/${organization.id}/assets/${asset.id}`);
assert.equal(saved.name, asset.name);
assert.equal(saved.assetCode, asset.assetCode);
assert.equal(saved.publicCode, asset.publicCode);
assert.equal(saved.location.id, location.id);
assert.equal(saved.status, "AVAILABLE");
const dashboard = await api(`/organizations/${organization.id}/dashboard`);
assert.equal(dashboard.totalAssets, 1);
assert.equal(dashboard.availableAssets, 1);
console.log(
  "PASS: account, Owner membership, location, asset and dashboard survived recreation.",
);
