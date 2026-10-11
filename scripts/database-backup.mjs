import { parseArguments, postgresContainer, backupDatabase, restoreDatabase } from "./lib/database-backup.mjs";

try {
  const options = parseArguments(process.argv.slice(2));
  const container = postgresContainer();
  if (options.command === "backup") {
    const file = await backupDatabase({ container, output: options.output });
    console.log(`Backup saved: ${file}`);
  } else {
    const database = await restoreDatabase({ container, ...options });
    console.log(`Restored into new database: ${database}. Application database was not switched.`);
  }
} catch (error) {
  console.error(error.message);
  process.exitCode = 1;
}
