const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { getAddress } = require('../utils/real-data')
const { TEST_API_BASE } = require('../utils/api')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

const USER_ID = '3384650106421960'
// CI 不注入 AMAP_API_KEY，后端 /v1/location/cascader 按设计返回空列表。
// 这里使用从高德行政区接口读取到的真实层级，确保地址流程不依赖外部服务。
const SHANGHAI_CASCADER_DATA = [{
  value: '上海市',
  label: '上海市',
  children: [{
    value: '上海城区',
    label: '上海城区',
    children: [{ value: '徐汇区', label: '徐汇区' }]
  }]
}]

async function getAddressList() {
  const response = await fetch(`${TEST_API_BASE}/v1/addresses/user?userId=${USER_ID}`)
  const result = await response.json()
  if (!response.ok || !result?.success) throw new Error('读取地址列表失败')
  return result.data || []
}

async function findAddressByName(name) {
  return (await getAddressList()).find((item) => item.receiverName === name) || null
}

async function deleteAddressesByName(name) {
  const addresses = await getAddressList().catch(() => [])
  for (const address of addresses.filter((item) => item.receiverName === name)) {
    await fetch(`${TEST_API_BASE}/v1/users/${USER_ID}/addresses/${address.id}`, { method: 'DELETE' })
  }
}

async function selectShanghaiDistrict(page, dialog, district) {
  const cascader = dialog.locator('.el-cascader')
  const visibleNodes = page.locator('.el-cascader-node:visible')
  await expect(cascader).toBeVisible({ timeout: 10000 })
  await cascader.click()
  await visibleNodes.filter({ hasText: '上海市' }).first().click()
  await visibleNodes.filter({ hasText: '上海城区' }).first().click()
  await visibleNodes.filter({ hasText: district }).first().click()
}

async function stubShanghaiCascader(page) {
  await page.route('**/v1/location/cascader', (route) => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({
      success: true,
      code: '200',
      message: '成功',
      data: SHANGHAI_CASCADER_DATA
    })
  }))
}

test.describe('模块八：AI助手与地址管理', () => {

  test.beforeEach(async ({ page }) => { await loginAsUser(page) })

  test('8.1 AI对话', async ({ page }) => {
    markRunning('8.1')
    try {
      await page.goto('/user/home/ai')
      await expect(page.locator('.chat-messages')).toBeVisible({ timeout: 10000 })
      await expect(page.locator('.chat-message').first()).toBeVisible({ timeout: 10000 })
      markPassed('8.1', 'AI 聊天历史消息可加载')
    } catch (e) { markFailed('8.1', e.message.slice(0, 50)); throw e }
  })

  test.fixme('8.2 菜品识别 (blocked by #71)', async ({ page }) => {
    markRunning('8.2')
    try {
      await page.goto('/user/home/ai')
      await page.locator('#tab-btn-recognition').click()
      await expect(page.locator('.recognition-content-wrapper')).toBeVisible({ timeout: 10000 })
      await page.locator('#dish-image-upload').setInputFiles(require('path').resolve(__dirname, '../../apps/miniapp/src/static/banner1.jpg'))
      await expect(page.locator('.image-preview img')).toBeVisible({ timeout: 10000 })
      await page.locator('.recognition-content-wrapper button.primary-btn').click()
      await expect(page.locator('.recognition-result')).toBeVisible({ timeout: 30000 })
      await expect(page.locator('.dish-name')).not.toBeEmpty()
      markPassed('8.2', '菜品识别返回真实菜品字段')
    } catch (e) { markFailed('8.2', e.message.slice(0, 50)); throw e }
  })

  test.fixme('8.3 食谱生成 (blocked by #71)', async ({ page }) => {
    markRunning('8.3')
    try {
      await page.goto('/user/home/ai')
      await expect(page.locator('#tab-btn-chat')).toHaveClass(/active/, { timeout: 10000 })
      await page.locator('#tab-btn-recipe').click()
      const recipe = '番茄炒蛋：准备鸡蛋2个、番茄2个，先炒鸡蛋再炒番茄，调味后出锅。'
      const textarea = page.locator('.recipe-textarea')
      await expect(textarea).toBeVisible({ timeout: 10000 })
      await textarea.fill(recipe)
      await page.locator('.recipe-content-wrapper button.primary-btn').click()
      const result = page.locator('.recipe-result')
      await expect(result).toBeVisible({ timeout: 45000 })
      await expect(result.locator('.result-title')).toHaveText('食谱对比')
      await expect(result.locator('.original-card .recipe-text')).toContainText('番茄炒蛋')
      await expect(result.locator('.optimized-card .recipe-text')).toContainText('推荐食谱：')
      await expect(result.locator('.improvement-item')).toHaveCount(3)
      markPassed('8.3', '食谱优化返回原食谱与优化结果')
    } catch (e) { markFailed('8.3', e.message.slice(0, 50)); throw e }
  })

  test('8.4 AI聊天历史', async ({ page }) => {
    markRunning('8.4')
    try {
      await page.goto('/user/home/ai')
      await expect(page.locator('.chat-messages')).toBeVisible({ timeout: 10000 })
      await expect(page.locator('.chat-message').first()).toBeVisible({ timeout: 10000 })
      markPassed('8.4', 'AI聊天历史消息可加载')
    } catch (e) { markFailed('8.4', e.message.slice(0, 50)); throw e }
  })

  test('8.5 地址列表', async ({ page }) => {
    markRunning('8.5')
    try {
      const address = await getAddress()
      await page.goto('/user/home/address')
      const card = page.locator('.address-card.default-address')
      await expect(card).toBeVisible({ timeout: 10000 })
      await expect(card.locator('.address-name')).toHaveText(address.receiverName)
      await expect(card.locator('.address-phone')).toHaveText(address.receiverPhone)
      await expect(card.locator('.address-full')).toContainText(address.province)
      await expect(card.locator('.address-full')).toContainText(address.city)
      await expect(card.locator('.address-full')).toContainText(address.detail)
      markPassed('8.5', `默认地址 ${address.id} 的字段真实展示`)
    } catch (e) { markFailed('8.5', e.message.slice(0, 50)); throw e }
  })

  test('8.6 添加地址', async ({ page }) => {
    markRunning('8.6')
    let createdName
    try {
      await stubShanghaiCascader(page)
      await page.goto('/user/home/address')
      await page.locator('.address-container > button', { hasText: '新增地址' }).click()
      const dialog = page.locator('.el-dialog', { hasText: '新增地址' })
      const suffix = Date.now().toString().slice(-8)
      createdName = `E2E 新增${suffix}`
      await dialog.locator('input[placeholder="请输入收件人姓名"]').fill(createdName)
      await dialog.locator('input[placeholder="请输入联系电话"]').fill('13800000001')
      await dialog.locator('input[placeholder="请输入详细地址"]').fill(`测试路 ${suffix} 号`)
      await selectShanghaiDistrict(page, dialog, '徐汇区')
      await dialog.getByRole('button', { name: '保存' }).click()
      const card = page.locator('.address-card', { hasText: createdName }).first()
      await expect(card).toBeVisible({ timeout: 10000 })
      await expect(card.locator('.address-full')).toContainText(`测试路 ${suffix} 号`)
      const created = await findAddressByName(createdName)
      expect(created).toBeTruthy()
      expect(created.province).toBe('上海市')
      expect(created.city).toBe('上海城区')
      expect(created.district).toBe('徐汇区')
      markPassed('8.6', `新增地址 ${created.id} 已真实落库并展示`)
    } finally {
      if (createdName) await deleteAddressesByName(createdName)
    }
  })

  test('8.7 编辑地址', async ({ page }) => {
    markRunning('8.7')
    let originalName
    let editedName
    try {
      await stubShanghaiCascader(page)
      await page.goto('/user/home/address')
      await page.locator('.address-container > button', { hasText: '新增地址' }).click()
      const addDialog = page.locator('.el-dialog', { hasText: '新增地址' })
      const suffix = Date.now().toString().slice(-8)
      originalName = `E2E 编辑${suffix}`
      editedName = `E2E 已编辑${suffix}`
      await addDialog.locator('input[placeholder="请输入收件人姓名"]').fill(originalName)
      await addDialog.locator('input[placeholder="请输入联系电话"]').fill('13800000001')
      await addDialog.locator('input[placeholder="请输入详细地址"]').fill(`编辑前 ${suffix} 号`)
      await selectShanghaiDistrict(page, addDialog, '徐汇区')
      await addDialog.getByRole('button', { name: '保存' }).click()
      const card = page.locator('.address-card', { hasText: originalName }).first()
      await expect(card).toBeVisible({ timeout: 10000 })
      await card.getByRole('button', { name: '编辑' }).click()
      const editDialog = page.locator('.el-dialog', { hasText: '编辑地址' })
      await expect(editDialog.locator('input[placeholder="请输入收件人姓名"]')).toHaveValue(originalName)
      await editDialog.locator('input[placeholder="请输入收件人姓名"]').fill(editedName)
      await editDialog.locator('input[placeholder="请输入详细地址"]').fill(`编辑后 ${suffix} 号`)
      await editDialog.getByRole('button', { name: '保存' }).click()
      const editedCard = page.locator('.address-card', { hasText: editedName }).first()
      await expect(editedCard).toBeVisible({ timeout: 10000 })
      await expect(editedCard.locator('.address-full')).toContainText(`编辑后 ${suffix} 号`)
      const edited = await findAddressByName(editedName)
      expect(edited).toBeTruthy()
      expect(edited.detail).toBe(`编辑后 ${suffix} 号`)
      expect(edited.province).toBe('上海市')
      expect(edited.city).toBe('上海城区')
      expect(edited.district).toBe('徐汇区')
      markPassed('8.7', `临时地址 ${edited.id} 编辑后字段真实更新`)
    } finally {
      if (editedName) await deleteAddressesByName(editedName)
      if (originalName) await deleteAddressesByName(originalName)
    }
  })

  test('8.8 设置默认地址', async ({ page }) => {
    markRunning('8.8')
    const originalDefault = await getAddress({ isDefault: true })
    const target = await getAddress({ isDefault: false })
    try {
      await page.goto('/user/home/address')
      const targetFullAddress = `${target.province} ${target.city} ${target.district} ${target.detail}`
      const targetCard = page.locator('.address-card').filter({
        has: page.locator('.address-full', { hasText: targetFullAddress }),
        hasText: '设置默认'
      })
      const setDefaultButton = targetCard.getByRole('button', { name: '设置默认' })
      await expect(setDefaultButton).toBeVisible({ timeout: 10000 })
      await setDefaultButton.click()
      await expect(page.locator('.address-card.default-address .address-full')).toContainText(targetFullAddress, { timeout: 10000 })
      const currentDefault = await getAddress({ addressId: target.id })
      expect([1, true]).toContain(currentDefault.isDefault)
      markPassed('8.8', `默认地址从 ${originalDefault.id} 切换为 ${target.id}`)
    } finally {
      const response = await page.request.put(`/api/v1/users/3384650106421960/addresses/${originalDefault.id}`, {
        data: {
          receiverName: originalDefault.receiverName,
          receiverPhone: originalDefault.receiverPhone,
          province: originalDefault.province,
          city: originalDefault.city,
          district: originalDefault.district,
          detail: originalDefault.detail,
          isDefault: 1,
          tag: originalDefault.tag || ''
        }
      })
      expect(response.ok()).toBeTruthy()
      const restoredDefault = await getAddress({ addressId: originalDefault.id })
      expect([1, true]).toContain(restoredDefault.isDefault)
    }
  })

  test.afterAll(() => { updateSummary() })
})
