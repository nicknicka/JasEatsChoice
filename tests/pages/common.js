class LoginPage {
  constructor(page) {
    this.page = page
    this.usernameInput = page.locator('input').first()
    this.passwordInput = page.locator('input[type="password"]').first()
    this.loginButton = page.locator('button:has-text("登录"), button[type="submit"]').first()
    this.registerLink = page.locator('a:has-text("注册"), span:has-text("注册")').first()
    this.forgotPasswordLink = page.locator('a:has-text("忘记密码"), a:has-text("找回密码")').first()
    this.errorMessage = page.locator('.el-message--error, .error-message, [class*="error"]').first()
  }

  async goto() {
    await this.page.goto('/login')
    await this.page.waitForLoadState('networkidle').catch(() => null)
  }

  async login(username, password) {
    await this.usernameInput.fill(username)
    await this.passwordInput.fill(password)
    await this.loginButton.click()
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
    this.usernameInput = page.locator('input').first()
    this.passwordInput = page.locator('input[type="password"]').first()
    this.loginButton = page.locator('button:has-text("登录"), button[type="submit"]').first()
  }

  async goto() {
    await this.page.goto('/admin/login')
    await this.page.waitForLoadState('networkidle').catch(() => null)
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