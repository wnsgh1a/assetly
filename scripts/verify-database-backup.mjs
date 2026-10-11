import assert from "node:assert/strict";
import { execFileSync } from "node:child_process";
import { createHash, randomBytes, randomUUID } from "node:crypto";
import { mkdtemp, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { postgresContainer, backupDatabase, restoreDatabase } from "./lib/database-backup.mjs";

assert.match(process.env.COMPOSE_PROJECT_NAME ?? "", /^assetly-ci-\d+-\d+$/, "Use an isolated CI Compose project");
const container = postgresContainer();
const database = `assetly_restore_${randomUUID().replaceAll("-", "")}`;
const directory = await mkdtemp(join(tmpdir(), "assetly-restore-"));
const file = join(directory, "verification.dump");
const tables = ["users", "organizations", "organization_members", "locations", "asset_categories", "assets", "asset_histories", "flyway_schema_history"];

function sql(query, target = "") {
  return execFileSync("docker", ["exec", "-i", container, "sh", "-c",
    'exec psql -U "$POSTGRES_USER" -d "${1:-$POSTGRES_DB}" --no-psqlrc --set=ON_ERROR_STOP=1 --tuples-only --no-align',
    "verify", target], { input: query, encoding: "utf8", timeout: 30_000, maxBuffer: 16 * 1024 * 1024 }).trim();
}

function fingerprint(target = "") {
  const hash = createHash("sha256");
  for (const table of tables) {
    const order = table === "flyway_schema_history" ? "installed_rank" : "id";
    const rows = sql(`SELECT COALESCE(jsonb_agg(to_jsonb(t) ORDER BY ${order}), '[]'::jsonb) FROM ${table} t`, target);
    hash.update(table).update(rows);
  }
  return hash.digest("hex");
}

try {
  assert.ok(Number(sql("SELECT count(*) FROM assets")) > 0, "Create persistence fixtures before verifying backup");
  assert.ok(Number(sql("SELECT count(*) FROM asset_histories")) > 0, "History must be present in the backup");
  // Poorly compressible fixture exercises archives larger than a process pipe buffer.
  sql(`INSERT INTO locations (organization_id, name, description, created_at, updated_at)
    SELECT id, 'Backup stream ${randomUUID()}', '${randomBytes(256 * 1024).toString("hex")}', now(), now()
    FROM organizations ORDER BY id LIMIT 1;`);
  const original = fingerprint();
  await backupDatabase({ container, output: file });
  await restoreDatabase({ container, file, database });
  assert.equal(fingerprint(database), original, "Restored rows and Flyway history must exactly match the backup source");
  assert.equal(fingerprint(), original, "Backup and restore must not modify the source database");

  await assert.rejects(restoreDatabase({ container, file, database }), /failed/);
  assert.equal(fingerprint(database), original, "A second restore must not overwrite the existing target");

  const maximum = Number(sql("SELECT COALESCE(max(id), 0) FROM users", database));
  const result = sql("INSERT INTO users (email, password, name, created_at, updated_at) VALUES ('restore-sequence@example.com', 'verification-only', 'Restore check', now(), now()) RETURNING id", database);
  assert.ok(Number(result.split(/\r?\n/)[0]) > maximum, "Identity sequence must allow a new record after restore");
  console.log("PASS: all application rows, QR codes, password hashes, audit history, Flyway history and identity sequence restored; source unchanged; existing target protected.");
} finally {
  // Only a random verification database inside an explicitly isolated CI project is removed.
  execFileSync("docker", ["exec", container, "sh", "-c", 'exec dropdb -U "$POSTGRES_USER" --maintenance-db="$POSTGRES_DB" --if-exists -- "$1"', "cleanup", database], { stdio: "inherit", timeout: 30_000 });
  await rm(directory, { recursive: true, force: true });
}
