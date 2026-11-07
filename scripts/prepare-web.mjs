import { cp, mkdir, rm } from 'node:fs/promises';
import { existsSync } from 'node:fs';

const rootFiles = [
  'index.html',
  'manifest.webmanifest',
  'sw.js',
  'favicon.png'
];

await rm('www', { recursive: true, force: true });
await mkdir('www/icons', { recursive: true });
for (const file of rootFiles) {
  if (!existsSync(file)) throw new Error(`Required web file is missing: ${file}`);
  await cp(file, `www/${file}`);
}
await cp('icons', 'www/icons', { recursive: true });
console.log('Web assets prepared in www/.');
