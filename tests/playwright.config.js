const { defineConfig, devices } = require('@playwright/test')

module.exports = defineConfig({
  testDir: './specs',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: [
    ['list'],
    ['html', { outputFolder: './reports/html', open: 'never' }],
    ['json', { outputFile: './reports/results.json' }]
  ],
  timeout: 30000,
  expect: { timeout: 10000 },
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:5173',
    // 本地兜底：拉不到 Playwright 自带 chromium 构建时，用 PW_CHANNEL=chrome 驱动系统 Chrome。
    // CI 不设这个变量，跑的仍是 npx playwright install 装上的 chromium。
    channel: process.env.PW_CHANNEL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 15000,
    navigationTimeout: 30000
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ],
  outputDir: './reports/artifacts'
})
