const { test } = require('@playwright/test');
const { _electron: electron } = require('playwright');
const path = require('path');
const fs = require('fs');

test('electron smoke', async () => {
  const appDir = '/Users/nickxiao/JasEatsChoice/JasEatsChoiceFront';
  const outputDir = '/Users/nickxiao/JasEatsChoice/output/playwright';
  fs.mkdirSync(outputDir, { recursive: true });
  process.chdir(appDir);
  const electronPath = require(path.join(appDir, 'node_modules', 'electron'));
  const app = await electron.launch({ executablePath: electronPath, args: ['.'] });
  const page = await app.firstWindow();
  await page.waitForTimeout(8000);
  await page.screenshot({ path: path.join(outputDir, 'electron-smoke.png') });
  const text = await page.locator('body').innerText().catch(() => '');
  console.log('ELECTRON_URL=' + page.url());
  console.log('ELECTRON_TEXT=' + text.slice(0, 1500).replace(/\n/g, ' | '));
  await app.close();
});
