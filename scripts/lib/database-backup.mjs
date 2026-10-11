import { spawn, execFileSync } from "node:child_process";
import { createReadStream, createWriteStream } from "node:fs";
import { mkdir, open, link, unlink, stat } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { randomUUID } from "node:crypto";
import { pipeline } from "node:stream/promises";
import { fileURLToPath } from "node:url";

const workspace = fileURLToPath(new URL("../../", import.meta.url));
const restoreName = /^assetly_restore_[a-z0-9_]{1,40}$/;

export function parseArguments(args) {
  const [command, ...options] = args;
  if (!["backup", "restore"].includes(command)) {
    throw new Error("Usage: database-backup.mjs backup [--output FILE] | restore --file FILE --database assetly_restore_NAME");
  }
  const result = { command };
  const allowed = command === "backup" ? ["--output"] : ["--file", "--database"];
  for (let index = 0; index < options.length; index += 2) {
    const key = options[index];
    const value = options[index + 1];
    if (!allowed.includes(key) || !value || value.startsWith("--") || result[key.slice(2)] !== undefined) {
      throw new Error("Invalid, duplicate or missing command option");
    }
    result[key.slice(2)] = value;
  }
  if (command === "restore") {
    if (!result.file) throw new Error("--file is required");
    validateRestoreName(result.database);
  }
  return result;
}

export function validateRestoreName(database) {
  if (!restoreName.test(database ?? "")) {
    throw new Error("Restore requires a new database named assetly_restore_NAME (lowercase letters, digits and underscores only)");
  }
}

export function postgresContainer() {
  const containers = execFileSync("docker", ["compose", "ps", "-q", "postgres"], {
    cwd: workspace, encoding: "utf8", timeout: 30_000,
  }).trim().split(/\s+/).filter(Boolean);
  if (containers.length !== 1 || !/^[a-f0-9]{12,64}$/.test(containers[0])) {
    throw new Error("Exactly one running Compose postgres container is required");
  }
  return containers[0];
}

export async function runDocker(args, { input, output } = {}) {
  const child = spawn("docker", args, {
    cwd: workspace, stdio: [input ? "pipe" : "ignore", output ? "pipe" : "ignore", "inherit"],
    timeout: 900_000,
  });
  const completed = new Promise((resolveResult, reject) => {
    child.once("error", reject);
    child.once("close", (code, signal) => {
      if (code === 0) resolveResult();
      else reject(new Error(`Docker database command failed (exit ${code}, signal ${signal ?? "none"})`));
    });
  });
  const tasks = [completed];
  if (input) tasks.push(pipeline(input, child.stdin));
  if (output) tasks.push(pipeline(child.stdout, output));
  try {
    await Promise.all(tasks);
  } catch (error) {
    child.kill();
    await Promise.allSettled(tasks);
    throw error;
  }
}

async function validateArchive(file) {
  if (!(await stat(file)).isFile()) throw new Error("Archive must be a regular file");
  const handle = await open(file, "r");
  try {
    const header = Buffer.alloc(5);
    const { bytesRead } = await handle.read(header, 0, 5, 0);
    if (bytesRead !== 5 || header.toString("ascii") !== "PGDMP") {
      throw new Error("Expected a PostgreSQL custom-format archive");
    }
  } finally {
    await handle.close();
  }
}

export async function backupDatabase({ container, output, run = runDocker }) {
  const file = resolve(output ?? `backups/assetly-${new Date().toISOString().replace(/[:.]/g, "-")}-${randomUUID()}.dump`);
  await mkdir(dirname(file), { recursive: true, mode: 0o700 });
  const temporary = `${file}.${randomUUID()}.partial`;
  try {
    await run(["exec", container, "sh", "-c", 'exec pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --format=custom --no-acl'], {
      output: createWriteStream(temporary, { flags: "wx", mode: 0o600 }),
    });
    await validateArchive(temporary);
    // Publishing with a hard link is atomic and refuses an existing destination.
    await link(temporary, file);
    return file;
  } finally {
    await unlink(temporary).catch((error) => { if (error.code !== "ENOENT") throw error; });
  }
}

export async function restoreDatabase({ container, file, database, run = runDocker }) {
  validateRestoreName(database);
  const archive = resolve(file);
  await validateArchive(archive);
  // Check the archive before creating anything. Existing databases are never reused.
  await run(["exec", "-i", container, "sh", "-c", 'exec pg_restore --list'], { input: createReadStream(archive) });
  await run(["exec", container, "sh", "-c", 'exec createdb -U "$POSTGRES_USER" --maintenance-db="$POSTGRES_DB" --template=template0 -- "$1"', "restore", database]);
  try {
    await run(["exec", "-i", container, "sh", "-c", 'exec pg_restore -U "$POSTGRES_USER" --dbname="$1" --no-owner --no-privileges --exit-on-error --single-transaction', "restore", database], {
      input: createReadStream(archive),
    });
  } catch (error) {
    throw new Error(`Restore failed; the newly created database ${database} is retained for diagnosis. The source database was not changed.`, { cause: error });
  }
  return database;
}
