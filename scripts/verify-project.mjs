import { readFile, stat } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const required = [
  'index.html', 'manifest.webmanifest', 'sw.js', 'favicon.png',
  'icons/icon-192.png', 'icons/icon-512.png', 'icons/icon-512-maskable.png',
  'assets/icon-only.png', 'assets/icon-foreground.png', 'assets/icon-background.png', 'assets/splash.png', 'capacitor.config.json',
  'scripts/install-android-native.mjs', 'native/android/MainActivity.java.tpl', 'native/android/NativeFileSaverPlugin.java.tpl'
];
for (const file of required) {
  if (!existsSync(path.join(root, file))) throw new Error(`Missing required file: ${file}`);
}

function pngSize(file) {
  const b = file;
  if (b.length < 24 || b.subarray(0, 8).toString('hex') !== '89504e470d0a1a0a') {
    throw new Error('Not a valid PNG file');
  }
  return { width: b.readUInt32BE(16), height: b.readUInt32BE(20) };
}

for (const [file, min] of [
  ['icons/icon-192.png', 192], ['icons/icon-512.png', 512],
  ['icons/icon-512-maskable.png', 512], ['assets/icon-only.png', 1024],
  ['assets/icon-foreground.png', 1024], ['assets/icon-background.png', 1024], ['assets/splash.png', 2732]
]) {
  const b = await readFile(path.join(root, file));
  const { width, height } = pngSize(b);
  if (width < min || height < min) throw new Error(`${file} must be at least ${min}x${min}, found ${width}x${height}`);
  if (width !== height) throw new Error(`${file} must be square`);
}

const html = await readFile(path.join(root, 'index.html'), 'utf8');
if (!/<html[^>]*lang=["']fa["'][^>]*dir=["']rtl["']/i.test(html)) {
  throw new Error('index.html must declare Persian language and RTL direction');
}
if (!html.includes('manifest.webmanifest')) throw new Error('PWA manifest link not found in index.html');
if (!html.includes('saveBlobWithNativePicker') || !html.includes('NativeFileSaver')) throw new Error('Native Android save-picker integration is missing from index.html');
if (!html.includes('isCapacitorNative') || !html.includes('unregister()')) throw new Error('PWA service worker must be disabled and cleaned up inside the native Android app');
const nativePlugin = await readFile(path.join(root, 'native/android/NativeFileSaverPlugin.java.tpl'), 'utf8');
if (!nativePlugin.includes('androidx.activity.result.ActivityResult') || !nativePlugin.includes('Intent.ACTION_CREATE_DOCUMENT')) throw new Error('Native Android save picker template is incomplete');
const androidWorkflow = await readFile(path.join(root, '.github/workflows/android-apk.yml'), 'utf8');
if (!androidWorkflow.includes('scripts/install-android-native.mjs')) throw new Error('Android workflow does not install the native save-file plugin');
// Parse embedded scripts only; this checks JavaScript syntax without executing app code.
const scripts = [...html.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)].map(m => m[1]).filter(s => s.trim());
for (let i = 0; i < scripts.length; i++) {
  try { new Function(scripts[i]); }
  catch (error) { throw new Error(`JavaScript syntax error in inline script ${i + 1}: ${error.message}`); }
}

const manifest = JSON.parse(await readFile(path.join(root, 'manifest.webmanifest'), 'utf8'));
if (!manifest.icons?.some(i => i.purpose === 'any') || !manifest.icons?.some(i => i.purpose === 'maskable')) {
  throw new Error('Manifest must contain any and maskable icons');
}
const config = JSON.parse(await readFile(path.join(root, 'capacitor.config.json'), 'utf8'));
if (config.webDir !== 'www' || !config.appId || !config.appName) throw new Error('Capacitor config is incomplete');
console.log(`Project verification passed: ${required.length} required assets, ${scripts.length} inline script(s), PWA manifest and Capacitor config.`);
