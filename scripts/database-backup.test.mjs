import assert from "node:assert/strict";
import { test } from "node:test";
import { mkdtemp, readFile, readdir, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { Readable } from "node:stream";
import { pipeline } from "node:stream/promises";
import { parseArguments, backupDatabase, restoreDatabase } from "./lib/database-backup.mjs";

const archive = Buffer.concat([Buffer.from("PGDMP"), Buffer.from([0, 255, 13, 10, 128])]);

async function fixture(t) {
  const directory = await mkdtemp(join(tmpdir(), "assetly-backup-"));
  t.after(() => rm(directory, { recursive: true, force: true }));
  return { directory, file: join(directory, "backup.dump") };
}

test("CLI validates required options and refuses unsafe database names", () => {
  assert.deepEqual(parseArguments(["backup"]), { command: "backup" });
  assert.equal(parseArguments(["restore", "--file", "a.dump", "--database", "assetly_restore_test"]).database, "assetly_restore_test");
  for (const args of [[], ["backup", "--output"], ["backup", "--file", "a"], ["backup", "--output", "a", "--output", "b"], ["restore", "--file", "a", "--database", "assetly"], ["restore", "--file", "a", "--database", "assetly_restore_test;drop"], ["restore", "--database", "assetly_restore_test"]]) {
    assert.throws(() => parseArguments(args));
  }
});

test("backup preserves binary bytes and publishes only a complete archive", async (t) => {
  const { directory, file } = await fixture(t);
  await backupDatabase({ container: "test", output: file, run: async (args, { output }) => {
    assert.ok(args.includes('exec pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --format=custom --no-acl'));
    await pipeline(Readable.from([archive]), output);
  } });
  assert.deepEqual(await readFile(file), archive);
  assert.deepEqual(await readdir(directory), ["backup.dump"]);
});

test("backup never overwrites an existing file", async (t) => {
  const { directory, file } = await fixture(t);
  await writeFile(file, "keep");
  await assert.rejects(backupDatabase({ container: "test", output: file, run: async (_args, { output }) => pipeline(Readable.from([archive]), output) }), { code: "EEXIST" });
  assert.equal(await readFile(file, "utf8"), "keep");
  assert.deepEqual(await readdir(directory), ["backup.dump"]);
});

test("failed or invalid dumps leave no published or partial files", async (t) => {
  const { directory, file } = await fixture(t);
  for (const failure of [false, true]) {
    await assert.rejects(backupDatabase({ container: "test", output: file, run: async (_args, { output }) => {
      await pipeline(Readable.from([Buffer.from("invalid")]), output);
      if (failure) throw new Error("dump failed");
    } }));
    assert.deepEqual(await readdir(directory), []);
  }
});

test("restore rejects invalid archives before executing Docker", async (t) => {
  const { file } = await fixture(t);
  await writeFile(file, "invalid");
  await assert.rejects(restoreDatabase({ container: "test", file, database: "assetly_restore_test", run: () => assert.fail("Docker must not run") }));
});

test("restore validates archive, creates a new DB and restores transactionally", async (t) => {
  const { file } = await fixture(t);
  await writeFile(file, archive);
  const calls = [];
  await restoreDatabase({ container: "test", file, database: "assetly_restore_test", run: async (args, { input } = {}) => {
    calls.push(args);
    if (input) {
      const chunks = [];
      for await (const chunk of input) chunks.push(chunk);
      assert.deepEqual(Buffer.concat(chunks), archive);
    }
  } });
  assert.equal(calls.length, 3);
  assert.ok(calls[0].includes("exec pg_restore --list"));
  assert.ok(calls[1].some((arg) => arg.includes("createdb")));
  assert.ok(calls[2].some((arg) => arg.includes("--single-transaction")));
  assert.equal(calls[2].at(-1), "assetly_restore_test");
});

test("unreadable archive contents stop before creating the target DB", async (t) => {
  const { file } = await fixture(t);
  await writeFile(file, archive);
  let calls = 0;
  await assert.rejects(restoreDatabase({ container: "test", file, database: "assetly_restore_test", run: async (_args, { input }) => {
    calls++;
    for await (const _chunk of input) { /* Drain validation input. */ }
    throw new Error("invalid archive contents");
  } }), /invalid archive contents/);
  assert.equal(calls, 1);
});

test("existing target DB stops restoration without modifying it", async (t) => {
  const { file } = await fixture(t);
  await writeFile(file, archive);
  let calls = 0;
  await assert.rejects(restoreDatabase({ container: "test", file, database: "assetly_restore_test", run: async (_args, { input } = {}) => {
    calls++;
    if (input) for await (const _chunk of input) { /* Drain archive validation input. */ }
    if (calls === 2) throw new Error("database already exists");
  } }), /already exists/);
  assert.equal(calls, 2);
});

test("restore failure retains the new DB and reports no source change", async (t) => {
  const { file } = await fixture(t);
  await writeFile(file, archive);
  let calls = 0;
  await assert.rejects(restoreDatabase({ container: "test", file, database: "assetly_restore_test", run: async (_args, { input } = {}) => {
    calls++;
    if (input) for await (const _chunk of input) { /* Drain archive input. */ }
    if (calls === 3) throw new Error("restore failed");
  } }), /retained for diagnosis/);
  assert.equal(calls, 3);
});
