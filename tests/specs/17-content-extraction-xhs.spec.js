const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

// 依赖 #196 的后端接口（GET/POST/DELETE /v1/content-extraction/xhs-session）。
// #196 合入前本 spec 会失败；PR 冒烟只跑 01/04/10，不会覆盖到它。
const SESSION_PATH = '/api/v1/content-extraction/xhs-session'
const AUTH_SOURCE_ID = 'e2e-xhs-auth-source'
const AUTH_SOURCE_TITLE = 'E2E 需要登录小红书'
const FAILED_SOURCE_ID = 'e2e-extraction-failed-source'
const FAILED_SOURCE_TITLE = 'E2E 提取失败'
const FAILURE_MESSAGE = 'AI识别结果解析失败：上游返回内容不完整，请稍后重试。'
const PUBLISH_SOURCE_ID = 'e2e-publish-source-212'
const PUBLISH_SOURCE_TITLE = 'E2E 发布食谱'
const PUBLISH_EXTRACTION_ID = 'e2e-publish-extraction-212'
const PUBLISHED_RECIPE_ID = 'e2e-recipe-212'
const PUBLISHED_RECIPE_NAME = '番茄炒蛋'
const PUBLISHED_SOURCE_URL = 'https://www.xiaohongshu.com/explore/e2e-publish-212'

// 内容提取接口现在只认已验证 JWT：Authorization 是唯一可信身份来源，
// X-User-Id 不再影响内容提取的归属（其余模块尚未收紧，带上它无害且兼容）。
async function authHeaders(page) {
  const { token, userId } = await page.evaluate(() => ({
    token: localStorage.getItem('auth_token'),
    userId: localStorage.getItem('auth_userId')
  }))
  const headers = {}
  if (token) headers.Authorization = `Bearer ${token}`
  if (userId) headers['X-User-Id'] = userId
  return headers
}

async function deleteSession(page) {
  const headers = await authHeaders(page)
  if (!headers.Authorization) return
  await page.request.delete(SESSION_PATH, { headers })
}

// 真实 AUTH_REQUIRED 依赖小红书线上风控，不可控；这里注入固定来源保证入口可复现。
async function stubAuthRequiredSource(page) {
  await page.route('**/v1/content-extraction/sources', (route) => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({
      success: true,
      code: '200',
      message: '成功',
      data: [{
        id: AUTH_SOURCE_ID,
        title: AUTH_SOURCE_TITLE,
        platform: 'XIAOHONGSHU',
        platformName: '小红书',
        contentType: 'VIDEO',
        contentTypeName: '视频',
        contentUrl: 'https://www.xiaohongshu.com/explore/e2e-auth-required',
        extractionStatus: 'AUTH_REQUIRED',
        extractionStatusName: '需要登录小红书',
        extractionId: null,
        createdAt: new Date().toISOString()
      }]
    })
  }))
}

async function stubFailedSource(page) {
  await page.route('**/v1/content-extraction/sources', (route) => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({
      success: true,
      code: '200',
      message: '成功',
      data: [{
        id: FAILED_SOURCE_ID,
        title: FAILED_SOURCE_TITLE,
        platform: 'OTHER',
        platformName: '其他',
        contentType: 'ARTICLE',
        contentTypeName: '文章',
        contentUrl: 'https://example.com/e2e-failed',
        extractionStatus: 'PARSE_FAILED',
        extractionStatusName: '解析失败',
        extractionId: 'e2e-stale-extraction',
        errorMessage: FAILURE_MESSAGE,
        createdAt: new Date().toISOString()
      }]
    })
  }))
}

async function stubPublishSource(page, publishedState) {
  await page.route('**/v1/content-extraction/sources', (route) => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({
      success: true,
      code: '200',
      message: '成功',
      data: [{
        id: PUBLISH_SOURCE_ID,
        title: PUBLISH_SOURCE_TITLE,
        platform: 'XIAOHONGSHU',
        platformName: '小红书',
        contentType: 'ARTICLE',
        contentTypeName: '文章',
        contentUrl: PUBLISHED_SOURCE_URL,
        extractionStatus: 'SUCCESS',
        extractionStatusName: '提取完成',
        extractionId: PUBLISH_EXTRACTION_ID,
        extractedDishName: PUBLISHED_RECIPE_NAME,
        isPublished: publishedState.isPublished,
        recipeId: publishedState.recipeId,
        createdAt: new Date().toISOString()
      }]
    })
  }))
}

async function stubExtractionDetail(page) {
  await page.route(`**/v1/content-extraction/extraction/${PUBLISH_EXTRACTION_ID}`, (route) => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({
      success: true,
      code: '200',
      message: '成功',
      data: {
        id: PUBLISH_EXTRACTION_ID,
        dishName: PUBLISHED_RECIPE_NAME,
        description: '酸甜开胃',
        ingredients: [],
        steps: [],
        tags: [],
        cookingTime: 15,
        calories: 180,
        isPublished: false,
        recipeId: null
      }
    })
  }))
}

async function stubPublishedRecipeList(page) {
  await page.route('**/v1/recipe/all*', (route) => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({
      success: true,
      code: '200',
      message: '成功',
      data: [{
        id: PUBLISHED_RECIPE_ID,
        name: PUBLISHED_RECIPE_NAME,
        type: 'dinner',
        cookTime: '15分钟',
        sourceUrl: PUBLISHED_SOURCE_URL,
        sourcePlatform: 'XIAOHONGSHU',
        items: [{
          name: PUBLISHED_RECIPE_NAME,
          ingredients: ['番茄 2个', '鸡蛋 3个'],
          calories: 180,
          protein: 0,
          carbs: 0,
          fat: 0
        }],
        calories: 180,
        protein: 0,
        carbs: 0,
        fat: 0,
        favorite: false
      }]
    })
  }))
}

async function openExtractionTab(page) {
  await page.goto('/user/home/ai')
  await expect(page.locator('#tab-btn-chat')).toHaveClass(/active/, { timeout: 10000 })
  await page.locator('#tab-btn-extraction').click()
}

test.describe('模块十七：内容提取登录态绑定', () => {

  test.beforeEach(async ({ page }) => { await loginAsUser(page) })

  test('17.1 小红书登录态绑定并自动重试', async ({ page }) => {
    markRunning('17.1')
    let reExtractCount = 0
    try {
      await stubAuthRequiredSource(page)
      await page.route('**/v1/content-extraction/source/*/re-extract', (route) => {
        reExtractCount += 1
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ success: true, code: '200', message: '成功' })
        })
      })
      await deleteSession(page)

      await openExtractionTab(page)

      const card = page.locator('.source-card', { hasText: AUTH_SOURCE_TITLE }).first()
      await expect(card).toBeVisible({ timeout: 10000 })
      await card.click()

      const dialog = page.locator('.el-dialog', { hasText: '绑定小红书登录态' })
      await expect(dialog).toBeVisible({ timeout: 10000 })
      await expect(dialog.locator('.xhs-status')).toHaveText('未绑定')

      // 首次绑定必须勾选确认后才能提交
      await dialog.locator('textarea').fill('a=1')
      const bindButton = dialog.getByRole('button', { name: '绑定并重试' })
      await expect(bindButton).toBeDisabled()
      await dialog.locator('.el-checkbox').click()
      await expect(bindButton).toBeEnabled()

      // 非法登录态（缺少 web_session）被服务端拒绝，并显示服务端返回的具体原因
      await bindButton.click()
      const errorToast = page.locator('.el-message--error').last()
      await expect(errorToast).toBeVisible({ timeout: 10000 })
      await expect(errorToast).toContainText(/web_session|登录态|cookie/i)

      // 格式合法的假登录态绑定成功 → 自动重试该条
      await dialog.locator('textarea').fill('web_session=e2e-fake-session; a=1')
      await bindButton.click()
      await expect(dialog).toBeHidden({ timeout: 10000 })
      await expect.poll(() => reExtractCount, { timeout: 10000 }).toBeGreaterThan(0)

      // 绑定状态可从服务端读回
      const statusResponse = await page.request.get(SESSION_PATH, {
        headers: await authHeaders(page)
      })
      const statusResult = await statusResponse.json()
      expect(statusResult.success).toBe(true)
      expect(statusResult.data.bound).toBe(true)

      // 解绑
      await card.click()
      await expect(dialog.locator('.xhs-status')).toHaveText('已绑定')
      await dialog.getByRole('button', { name: '解绑' }).click()
      await expect(dialog.locator('.xhs-status')).toHaveText('未绑定')

      markPassed('17.1', '弹窗 → 非法被拒 → 绑定成功并自动重试 → 解绑')
    } catch (e) {
      markFailed('17.1', e.message.slice(0, 60))
      throw e
    } finally {
      await deleteSession(page)
    }
  })

  test('17.2 添加内容源可手动纠正类型，自动识别不提交类型', async ({ page }) => {
    markRunning('17.2')
    const createPayloads = []
    try {
      await page.route('**/v1/content-extraction/sources', (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true, code: '200', message: '成功', data: [] })
      }))
      await page.route('**/v1/content-extraction/source', async (route) => {
        if (route.request().method() !== 'POST') return route.continue()
        createPayloads.push(JSON.parse(route.request().postData() || '{}'))
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ success: true, code: '200', message: '成功', data: 'e2e-source' })
        })
      })

      await openExtractionTab(page)
      await page.getByRole('button', { name: '添加内容源' }).click()
      const dialog = page.locator('.el-dialog', { hasText: '从内容链接提取菜品' })
      await expect(dialog).toBeVisible()

      const typeSelect = dialog.locator('.el-select').last()
      await expect(typeSelect).not.toHaveClass(/is-disabled/)
      await expect(typeSelect).toContainText('自动识别')

      await dialog.locator('textarea').fill('https://example.com/e2e-article')
      await dialog.getByRole('button', { name: '开始提取' }).click()
      await expect.poll(() => createPayloads.length, { timeout: 10000 }).toBe(1)
      expect(createPayloads[0]).toEqual({ contentUrl: 'https://example.com/e2e-article' })

      await expect(dialog).toBeHidden({ timeout: 10000 })
      await page.getByRole('button', { name: '添加内容源' }).click()
      await expect(dialog).toBeVisible()
      await dialog.locator('.el-select').last().click()
      await page.locator('.el-select-dropdown__item', { hasText: '图片' }).last().click()
      await dialog.locator('textarea').fill('https://example.com/e2e-image')
      await dialog.getByRole('button', { name: '开始提取' }).click()
      await expect.poll(() => createPayloads.length, { timeout: 10000 }).toBe(2)
      expect(createPayloads[1]).toEqual({
        contentUrl: 'https://example.com/e2e-image',
        contentType: 'IMAGE'
      })

      markPassed('17.2', '类型下拉可用；自动识别不传类型；手动类型随请求提交')
    } catch (e) {
      markFailed('17.2', e.message.slice(0, 60))
      throw e
    }
  })

  test('17.3 失败条目显示摘要、具体原因，并可重试或删除', async ({ page }) => {
    markRunning('17.3')
    let reExtractCount = 0
    let deleteCount = 0
    try {
      await stubFailedSource(page)
      await page.route('**/v1/content-extraction/source/*/re-extract', (route) => {
        reExtractCount += 1
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ success: true, code: '200', message: '成功' })
        })
      })
      await page.route('**/v1/content-extraction/source/*', (route) => {
        if (route.request().method() !== 'DELETE') return route.continue()
        deleteCount += 1
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ success: true, code: '200', message: '成功' })
        })
      })

      await openExtractionTab(page)
      const card = page.locator('.source-card', { hasText: FAILED_SOURCE_TITLE }).first()
      await expect(card).toBeVisible({ timeout: 10000 })
      await expect(card.locator('.failure-summary')).toContainText('AI识别结果解析失败')
      await card.click()

      const dialog = page.locator('.el-dialog', { hasText: '提取失败' })
      await expect(dialog).toBeVisible()
      await expect(dialog).toContainText(FAILURE_MESSAGE)
      await dialog.getByRole('button', { name: '重试' }).click()
      await expect.poll(() => reExtractCount, { timeout: 10000 }).toBe(1)
      await expect(dialog).toBeHidden({ timeout: 10000 })

      await card.click()
      await expect(dialog).toBeVisible()
      await dialog.getByRole('button', { name: '删除' }).click()
      await page.locator('.el-message-box').getByRole('button', { name: /^(OK|确定)$/ }).click()
      await expect.poll(() => deleteCount, { timeout: 10000 }).toBe(1)

      markPassed('17.3', '失败摘要 → 具体原因 → 重试 → 删除')
    } catch (e) {
      markFailed('17.3', e.message.slice(0, 60))
      throw e
    }
  })

  test('17.4 卡片入口可选择餐次，发布后查看食谱', async ({ page }) => {
    markRunning('17.4')
    let publishPayload
    const publishedState = { isPublished: false, recipeId: null }
    try {
      await stubPublishSource(page, publishedState)
      await stubPublishedRecipeList(page)
      await page.route(`**/v1/content-extraction/extraction/${PUBLISH_EXTRACTION_ID}/publish`, (route) => {
        publishPayload = JSON.parse(route.request().postData() || '{}')
        publishedState.isPublished = true
        publishedState.recipeId = PUBLISHED_RECIPE_ID
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            code: '200',
            message: '成功',
            data: PUBLISHED_RECIPE_ID
          })
        })
      })

      await openExtractionTab(page)
      const card = page.locator('.source-card', { hasText: PUBLISHED_RECIPE_NAME }).first()
      await expect(card).toBeVisible({ timeout: 10000 })
      await card.locator('.publish-icon').click()

      const publishDialog = page.locator('.el-dialog', { hasText: '发布为食谱' }).last()
      await expect(publishDialog).toBeVisible()
      await expect(publishDialog.locator('.el-select')).toContainText('午餐')
      await publishDialog.locator('.el-select').click()
      await page.locator('.el-select-dropdown__item', { hasText: '晚餐' }).last().click()
      await publishDialog.getByRole('button', { name: '确认发布' }).click()

      await expect.poll(() => publishPayload, { timeout: 10000 }).toEqual({ mealType: 'dinner' })
      await expect(card).toContainText('已发布')
      await expect(card.getByRole('button', { name: '查看食谱' })).toBeVisible()

      await card.getByRole('button', { name: '查看食谱' }).click()
      await page.waitForURL('**/my-recipe?recipeId=*', { timeout: 10000 })
      const recipeDetail = page.locator('.recipe-detail-dialog').last()
      await expect(recipeDetail).toBeVisible()
      await expect(recipeDetail).toContainText(PUBLISHED_RECIPE_NAME)
      await expect(recipeDetail).toContainText('小红书')
      await expect(recipeDetail).toContainText('15 分钟')
      await expect(recipeDetail.getByRole('link', { name: '查看原文' })).toHaveAttribute('href', PUBLISHED_SOURCE_URL)

      markPassed('17.4', '发布餐次可选；已发布可查看食谱；来源与时长正确展示')
    } catch (e) {
      markFailed('17.4', e.message.slice(0, 60))
      throw e
    }
  })

  test('17.5 详情入口默认午餐发布', async ({ page }) => {
    markRunning('17.5')
    let publishPayload
    try {
      await stubPublishSource(page, { isPublished: false, recipeId: null })
      await stubExtractionDetail(page)
      await page.route(`**/v1/content-extraction/extraction/${PUBLISH_EXTRACTION_ID}/publish`, (route) => {
        publishPayload = JSON.parse(route.request().postData() || '{}')
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            code: '200',
            message: '成功',
            data: PUBLISHED_RECIPE_ID
          })
        })
      })

      await openExtractionTab(page)
      await page.locator('.source-card', { hasText: PUBLISHED_RECIPE_NAME }).first().click()
      const detailDialog = page.locator('.extraction-detail-dialog .el-dialog', { hasText: '提取详情' }).first()
      await expect(detailDialog).toBeVisible()
      await detailDialog.getByRole('button', { name: '发布为食谱' }).click()

      const publishDialog = page.locator('.el-dialog', { hasText: '发布为食谱' }).last()
      await expect(publishDialog).toBeVisible()
      await expect(publishDialog.locator('.el-select')).toContainText('午餐')
      await publishDialog.getByRole('button', { name: '确认发布' }).click()

      await expect.poll(() => publishPayload, { timeout: 10000 }).toEqual({ mealType: 'lunch' })
      await expect(detailDialog.getByRole('button', { name: '查看食谱' })).toBeVisible()
      markPassed('17.5', '详情入口默认午餐；发布后详情保持打开并显示查看食谱')
    } catch (e) {
      markFailed('17.5', e.message.slice(0, 60))
      throw e
    }
  })

  test.afterAll(() => { updateSummary() })
})
