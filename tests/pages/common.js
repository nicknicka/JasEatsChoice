class LoginPage {
  constructor(page) {
    this.page = page
    this.phoneInput = page.locator('input[placeholder="手机号"]').first()
    this.usernameInput = this.phoneInput
    this.passwordInput = page.locator('input[placeholder="密码"], input[type="password"]').first()
    this.captchaInput = page.locator('input[placeholder="验证码"]').first()
    this.agreementCheckbox = page.locator('.agreement-row .el-checkbox').first()
    this.loginButton = page.locator('button.login-btn').first()
    this.registerLink = page.locator('a:has-text("注册"), span:has-text("注册")').first()
    this.forgotPasswordLink = page.locator('a:has-text("忘记密码"), a:has-text("找回密码")').first()
    this.errorMessage = page.locator('.el-message--error, .el-form-item__error, .error-message, [class*="error"]').first()
  }

  async goto() {
    await this.page.goto('/login')
    await this.page.waitForLoadState('networkidle').catch(() => null)
    await this.phoneInput.waitFor({ state: 'visible', timeout: 10000 })
  }

  async ensureAgreementAccepted() {
    const className = await this.agreementCheckbox.getAttribute('class').catch(() => '')
    if (!className || !className.includes('is-checked')) {
      await this.agreementCheckbox.click()
    }
  }

  async waitForCaptchaReady() {
    await this.captchaInput.waitFor({ state: 'visible', timeout: 10000 })

    for (let attempt = 0; attempt < 10; attempt += 1) {
      const currentValue = await this.captchaInput.inputValue().catch(() => '')
      if (currentValue && currentValue.trim()) {
        return currentValue.trim()
      }
      await this.page.waitForTimeout(500)
    }

    const refreshButton = this.page.locator('.captcha-refresh').first()
    if (await refreshButton.count() > 0) {
      await refreshButton.click().catch(() => null)
      for (let attempt = 0; attempt < 10; attempt += 1) {
        const currentValue = await this.captchaInput.inputValue().catch(() => '')
        if (currentValue && currentValue.trim()) {
          return currentValue.trim()
        }
        await this.page.waitForTimeout(500)
      }
    }

    throw new Error('验证码未自动填充，无法继续登录测试')
  }

  async fillCredentials(phone, password) {
    await this.phoneInput.fill(phone)
    await this.passwordInput.fill(password)
    await this.waitForCaptchaReady()
  }

  async submit() {
    await this.loginButton.click()
  }

  async login(phone, password) {
    await this.ensureAgreementAccepted()
    await this.fillCredentials(phone, password)
    await this.submit()
  }

  async gotoRegister() {
    await this.registerLink.click()
  }

  async gotoForgotPassword() {
    await this.forgotPasswordLink.click()
  }
}

class RegisterPage {
  constructor(page) {
    this.page = page
  }

  async goto() {
    await this.page.goto('/register')
    await this.page.waitForLoadState('networkidle').catch(() => null)
  }

  async fillForm(data) {
    for (const [key, value] of Object.entries(data)) {
      const input = this.page.locator(`input[placeholder*="${key}"], label:has-text("${key}") + .el-form-item__content input`).first()
      if (await input.count() > 0) await input.fill(value)
    }
  }

  async submit() {
    await this.page.locator('button:has-text("注册"), button[type="submit"]').first().click()
  }
}

class AdminLoginPage {
  constructor(page) {
    this.page = page
    this.usernameInput = page.locator('input[placeholder="管理员账号"]').first()
    this.passwordInput = page.locator('input[placeholder="密码"], input[type="password"]').first()
    this.loginButton = page.locator('button.login-btn').first()
  }

  async goto() {
    await this.page.goto('/admin/login')
    await this.page.waitForLoadState('networkidle').catch(() => null)
    await this.usernameInput.waitFor({ state: 'visible', timeout: 10000 })
  }

  async login(username, password) {
    await this.usernameInput.fill(username)
    await this.passwordInput.fill(password)
    await this.loginButton.click()
  }
}

class UserHomePage {
  constructor(page) {
    this.page = page
  }

  async goto() {
    await this.page.goto('/user/home')
    await this.page.waitForLoadState('networkidle').catch(() => null)
  }

  async navigateTo(route) {
    await this.page.goto(`/user/home/${route}`)
    await this.page.waitForLoadState('networkidle').catch(() => null)
  }
}

class MerchantHomePage {
  constructor(page) {
    this.page = page
  }

  async goto() {
    await this.page.goto('/merchant/home')
    await this.page.waitForLoadState('networkidle').catch(() => null)
  }

  async navigateTo(route) {
    await this.page.goto(`/merchant/home/${route}`)
    await this.page.waitForLoadState('networkidle').catch(() => null)
  }
}

class AdminDashboardPage {
  constructor(page) {
    this.page = page
  }

  async goto() {
    await this.page.goto('/admin/dashboard')
    await this.page.waitForLoadState('networkidle').catch(() => null)
  }
}

module.exports = {
  LoginPage,
  RegisterPage,
  AdminLoginPage,
  UserHomePage,
  MerchantHomePage,
  AdminDashboardPage
}
