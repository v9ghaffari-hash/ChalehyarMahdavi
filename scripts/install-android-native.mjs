import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import path from 'node:path';

const config = JSON.parse(await readFile('capacitor.config.json', 'utf8'));
if (!config.appId || !/^[A-Za-z][A-Za-z0-9_]*(\.[A-Za-z][A-Za-z0-9_]*)+$/.test(config.appId)) {
  throw new Error('capacitor.config.json appId is missing or invalid.');
}
const packagePath = config.appId.replaceAll('.', '/');
const destination = path.join('android', 'app', 'src', 'main', 'java', packagePath);
if (!existsSync(path.join('android', 'app', 'src', 'main'))) {
  throw new Error('Android project not found. Run npx cap add android before this script.');
}
await mkdir(destination, { recursive: true });
for (const [template, output] of [
  ['native/android/MainActivity.java.tpl', 'MainActivity.java'],
  ['native/android/NativeFileSaverPlugin.java.tpl', 'NativeFileSaverPlugin.java']
]) {
  const source = await readFile(template, 'utf8');
  await writeFile(path.join(destination, output), source.replaceAll('__PACKAGE__', config.appId), 'utf8');
}
console.log(`Installed NativeFileSaver Android plugin for ${config.appId}.`);
