// 后端地址的唯一来源：默认直连本地 Spring Boot，CI 可指向 nginx 网关。
// 登录路径以 /api/v3/api-docs 为准（/v1/users/login、/admin/login），
// 用例统一走 UI 登录，这里只导出基址，不再重复封装请求。
module.exports.TEST_API_BASE = process.env.TEST_API_BASE || 'http://localhost:7777/api'
