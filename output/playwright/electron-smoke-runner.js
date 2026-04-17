const path = require('path');
const fs = require('fs');
const { _electron: electron } = require('playwright');

(async () => {
  const appDir = '/Users/nickxiao/JasEatsChoice/JasEatsChoiceFront';
  const outputDir = '/Users/nickxiao/JasEatsChoice/output/playwright';
  const smokeUser = {
    phone: '17322222222',
    password: 'asdasd',
    captcha: '8888'
  };
  fs.mkdirSync(outputDir, { recursive: true });
  process.chdir(appDir);
  const electronPath = require(path.join(appDir, 'node_modules', 'electron'));
  const app = await electron.launch({ executablePath: electronPath, args: ['.'] });
  const page = await app.firstWindow();
  const consoleErrors = [];

  page.on('console', (msg) => {
    if (msg.type() === 'error') {
      consoleErrors.push(msg.text());
    }
  });

  const takeShot = async (name) => {
    await page.screenshot({ path: path.join(outputDir, name), fullPage: true });
  };

  const wait = (ms) => page.waitForTimeout(ms);
  const isVisible = async (locator) => locator.isVisible().catch(() => false);
  const closeVisibleDialog = async (dialogSelector) => {
    const dialog = page.locator(dialogSelector);
    if (!(await isVisible(dialog))) {
      return false;
    }

    const closeButton = dialog.locator('.el-dialog__headerbtn').first();
    if (await isVisible(closeButton)) {
      await closeButton.click();
      await dialog.waitFor({ state: 'hidden', timeout: 10000 }).catch(() => {});
      await wait(1000);
      return true;
    }

    await page.keyboard.press('Escape');
    await dialog.waitFor({ state: 'hidden', timeout: 10000 }).catch(() => {});
    await wait(1000);
    return !(await isVisible(dialog));
  };

  await wait(3000);
  await takeShot('electron-login.png');

  const agreementCheckbox = page.locator('.agreement-row .el-checkbox');
  const agreementClass = (await agreementCheckbox.getAttribute('class').catch(() => '')) || '';
  if (!agreementClass.includes('is-checked')) {
    await agreementCheckbox.click();
  }

  await page.locator('input[placeholder="手机号"]').fill(smokeUser.phone);
  await page.locator('input[placeholder="密码"]').fill(smokeUser.password);

  const captchaInput = page.locator('input[placeholder="验证码"]');
  const captchaValue = await captchaInput.inputValue().catch(() => '');
  if (!captchaValue) {
    await captchaInput.fill(smokeUser.captcha);
  }

  await page.locator('button.login-btn').click();
  await page.waitForURL(/#\/user\/home/, { timeout: 20000 });
  await wait(4000);
  await takeShot('electron-home.png');

  await page.evaluate(() => {
    window.location.hash = '#/user/home/chat';
  });
  await page.waitForURL(/#\/user\/home\/chat/, { timeout: 15000 });
  await page.locator('.chat-container').waitFor({ timeout: 15000 });
  await wait(6000);
  await takeShot('electron-chat.png');

  const conversationCount = await page.locator('.conversation-item').count();
  const groupConversationLocator = page.locator('.conversation-item').filter({
    has: page.locator('.group-tag')
  });
  const groupConversationCount = await groupConversationLocator.count();

  let selectedGroupName = '';
  if (groupConversationCount > 0) {
    const firstGroup = groupConversationLocator.first();
    selectedGroupName = (
      await firstGroup.locator('.name').first().innerText().catch(() => '')
    ).trim();
    await firstGroup.click();
    await wait(4000);
  }

  let hasFloatingOrderButton = await isVisible(page.locator('.floating-order-btn'));
  const hasCreateOrderButton = (await page.locator('.group-quick-actions .el-button').count()) > 1;

  if (!hasFloatingOrderButton && hasCreateOrderButton) {
    await page.locator('.group-quick-actions .el-button').first().click();
    await wait(5000);
    hasFloatingOrderButton = await isVisible(page.locator('.floating-order-btn'));
  }

  let openedOrderDrawer = false;
  if (hasFloatingOrderButton) {
    await page.locator('.floating-order-btn').click();
    await wait(2000);
    openedOrderDrawer = await isVisible(page.locator('.group-order-drawer'));
  }

  if (openedOrderDrawer) {
    await wait(2000);
    await takeShot('electron-group-order-drawer.png');
  }

  let merchantDialogVisible = false;
  let productDialogVisible = false;
  let productAdded = false;
  let drawerText = openedOrderDrawer
    ? await page.locator('.group-order-drawer').innerText().catch(() => '')
    : '';

  if (openedOrderDrawer && /选择商家|选择菜品/.test(drawerText)) {
    await page.getByText(/选择商家|选择菜品/, { exact: false }).last().click();
    await wait(3000);
    merchantDialogVisible = await isVisible(page.locator('.merchant-select-dialog'));
  }

  if (merchantDialogVisible) {
    const firstMerchantCard = page.locator('.merchant-select-dialog .merchant-card').first();
    if ((await firstMerchantCard.count()) > 0) {
      await firstMerchantCard.click();
      await wait(4000);
      productDialogVisible = await isVisible(page.locator('.product-select-dialog'));
    }
  }

  if (productDialogVisible) {
    const firstProduct = page.locator('.product-select-dialog .product-item').first();
    if ((await firstProduct.count()) > 0) {
      await firstProduct.locator('button', { hasText: '定制' }).click().catch(async () => {
        await firstProduct.click();
      });
      await page.locator('.product-customize-dialog').waitFor({ timeout: 10000 });
      await page.getByText('确认定制', { exact: true }).click();
      await wait(1500);
      const batchAddButton = page.locator('.product-select-dialog .batch-actions .el-button--success').first();
      if (await isVisible(batchAddButton)) {
        await batchAddButton.click();
      } else {
        await firstProduct.locator('button', { hasText: '加入订单' }).click();
      }
      productAdded = true;
      await wait(2000);
      await closeVisibleDialog('.product-select-dialog');
    }
  }

  drawerText = openedOrderDrawer
    ? await page.locator('.group-order-drawer').innerText().catch(() => '')
    : '';
  const hasPayButton = openedOrderDrawer && /确认成团并结算|去支付/.test(drawerText);
  const hasSelectMerchantButton = openedOrderDrawer && /选择商家|选择菜品/.test(drawerText);

  let reachedOrderConfirmation = false;
  let orderConfirmationText = '';
  let completedBalancePayment = false;
  let returnedOrdersPage = false;
  let refreshedPaidGroupOrderInChat = false;
  let paidDrawerText = '';
  if (hasPayButton) {
    await closeVisibleDialog('.product-customize-dialog');
    await closeVisibleDialog('.product-select-dialog');
    await closeVisibleDialog('.merchant-select-dialog');
    const payButton = page.locator('.group-order-drawer .drawer-footer .el-button--success').last();
    await payButton.click();
    await wait(2000);
    await page.waitForFunction(
      () => window.location.hash.includes('/user/home/order-confirmation'),
      null,
      { timeout: 15000 }
    );
    await page.locator('.order-confirmation-container').waitFor({ timeout: 15000 });
    await wait(5000);
    reachedOrderConfirmation = true;
    await takeShot('electron-order-confirmation.png');
    orderConfirmationText = await page.locator('body').innerText().catch(() => '');

    const submitButton = page.locator('.submit-button-fixed').first();
    if (await isVisible(submitButton)) {
      await submitButton.click();
      const confirmButton = page.locator('.el-message-box .el-button--primary').last();
      await confirmButton.waitFor({ timeout: 10000 });
      await confirmButton.click();
      await page.waitForFunction(
        () => window.location.hash.includes('/user/home/orders'),
        null,
        { timeout: 20000 }
      ).catch(() => {});
      returnedOrdersPage = await page.evaluate(() =>
        window.location.hash.includes('/user/home/orders')
      ).catch(() => false);
      completedBalancePayment = returnedOrdersPage;
      await wait(4000);
      await takeShot('electron-payment-result.png');
    }
  }

  if (completedBalancePayment) {
    await page.evaluate(() => {
      window.location.hash = '#/user/home/chat';
    });
    await page.waitForURL(/#\/user\/home\/chat/, { timeout: 15000 });
    await page.locator('.chat-container').waitFor({ timeout: 15000 });
    await wait(6000);

    const visibleDrawer = page.locator('.group-order-drawer');
    if (await isVisible(visibleDrawer)) {
      const drawerCloseButton = page.locator('.group-order-drawer .el-drawer__headerbtn').first();
      if (await isVisible(drawerCloseButton)) {
        await drawerCloseButton.click().catch(() => {});
      } else {
        await page.keyboard.press('Escape').catch(() => {});
      }
      await visibleDrawer.waitFor({ state: 'hidden', timeout: 10000 }).catch(() => {});
      await wait(1500);
    }

    const paidGroupConversation = page.locator('.conversation-item').filter({
      has: page.locator('.group-tag')
    }).first();
    if ((await paidGroupConversation.count()) > 0) {
      await paidGroupConversation.click({ force: true });
      await wait(4000);
    }

    const floatingOrderButton = page.locator('.floating-order-btn');
    if (await isVisible(floatingOrderButton)) {
      await floatingOrderButton.click();
      await wait(3000);
      paidDrawerText = await page.locator('.group-order-drawer').innerText().catch(() => '');
      refreshedPaidGroupOrderInChat = /已支付|查看历史|继续点餐|待支付加菜/.test(paidDrawerText);
      await takeShot('electron-paid-group-order.png');
    }
  }

  const title = await page.title();
  const url = page.url();
  const text = await page.locator('body').innerText().catch(() => '');
  const summary = {
    title,
    url,
    conversationCount,
    groupConversationCount,
    selectedGroupName,
    hasFloatingOrderButton,
    hasCreateOrderButton,
    openedOrderDrawer,
    merchantDialogVisible,
    productDialogVisible,
    productAdded,
    hasPayButton,
    hasSelectMerchantButton,
    reachedOrderConfirmation,
    completedBalancePayment,
    returnedOrdersPage,
    refreshedPaidGroupOrderInChat,
    drawerText: drawerText.slice(0, 2000),
    orderConfirmationText: orderConfirmationText.slice(0, 2000),
    paidDrawerText: paidDrawerText.slice(0, 2000),
    pageText: text.slice(0, 3000),
    consoleErrors
  };

  fs.writeFileSync(
    path.join(outputDir, 'electron-smoke-summary.json'),
    JSON.stringify(summary, null, 2),
    'utf8'
  );
  console.log(JSON.stringify(summary, null, 2));
  await app.close();
})().catch((error) => {
  console.error(error);
  process.exit(1);
});
