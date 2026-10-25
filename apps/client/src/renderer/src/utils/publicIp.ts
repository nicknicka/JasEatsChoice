const PUBLIC_IP_STORAGE_KEYS = ['client_ip', 'public_ip'] as const
const IS_DEV = (import.meta as ImportMeta & { env?: { DEV?: boolean } }).env?.DEV === true

const getApiModule = async () => {
  const locationApi = (await import('../api/location.js')).default
  return locationApi
}

// 支持CORS的公网IP服务（仅作为备用）
const IP_CANDIDATE_APIS = [
  {
    url: 'https://api.ip.sb/ip',
    parser: async (response: Response) => {
      const text = await response.text()
      return text.trim()
    }
  },
  {
    url: 'https://api.ipify.org?format=json',
    parser: async (response: Response) => {
      const data = await response.json()
      return typeof data?.ip === 'string' ? data.ip : ''
    }
  },
  {
    url: 'https://api64.ipify.org?format=json',
    parser: async (response: Response) => {
      const data = await response.json()
      return typeof data?.ip === 'string' ? data.ip : ''
    }
  },
  {
    url: 'https://api64.ipify.org?format=text',
    parser: async (response: Response) => {
      const text = await response.text()
      return text.trim()
    }
  }
] as const

const hasText = (value: unknown): value is string => {
  return typeof value === 'string' && value.trim().length > 0
}

const isValidIp = (value: string): boolean => {
  const ipv4 = /^(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}$/
  const ipv6 = /^([\da-fA-F]{1,4}:){7}[\da-fA-F]{1,4}$|^::1$|^::$/
  return ipv4.test(value) || ipv6.test(value)
}

const fetchWithTimeout = async (url: string, timeoutMs = 3500): Promise<Response> => {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), timeoutMs)

  try {
    return await fetch(url, {
      method: 'GET',
      signal: controller.signal,
      cache: 'no-store'
    })
  } finally {
    clearTimeout(timer)
  }
}

export const resolveAndStorePublicIp = async (): Promise<string | null> => {
  if (typeof window === 'undefined') {
    return null
  }

  if (IS_DEV) {
    console.info('[定位] 开始获取公网IP...')
  }

  const errors: string[] = []
  const parsedIp = (rawIp: string) => {
    return typeof rawIp === 'string' ? rawIp.trim() : ''
  }

  const storeIp = (ip: string, source: string) => {
    PUBLIC_IP_STORAGE_KEYS.forEach((key) => localStorage.setItem(key, ip))
    ;(window as Window & { __CLIENT_IP__?: string }).__CLIENT_IP__ = ip
    if (IS_DEV) {
      console.info(`[定位] 公网IP获取成功（${source}）：`, ip)
    }
  }

  // 前端优先：本地后端可能走代理/VPN出口，前端直连更接近用户浏览器看到的公网IP
  for (const api of IP_CANDIDATE_APIS) {
    try {
      const response = await fetchWithTimeout(api.url)
      if (!response.ok) {
        errors.push(`${api.url} 响应状态异常: ${response.status}`)
        continue
      }

      const ip = parsedIp(await api.parser(response))
      if (!hasText(ip) || !isValidIp(ip)) {
        errors.push(`${api.url} 返回内容不是有效IP: ${ip || '<empty>'}`)
        continue
      }

      storeIp(ip, '前端直连')
      return ip
    } catch (error) {
      const message = (error as Error).message
      errors.push(`${api.url} 请求失败: ${message}`)
      if (IS_DEV) {
        console.warn('[定位] 获取公网IP失败，尝试下一个来源:', message)
      }
    }
  }

  // 后端兜底：只在前端直连全部失败时使用
  try {
    const locationApi = await getApiModule()
    const response = await locationApi.getPublicIp()
    const data = response as { code?: string; data?: { ip?: string } } | undefined

    if (data && data.code === '200' && data.data?.ip) {
      const ip = parsedIp(data.data.ip)
      if (isValidIp(ip)) {
        storeIp(ip, '后端代理兜底')
        return ip
      }
    }
  } catch (error) {
    if (IS_DEV) {
      console.warn('[定位] 后端代理获取IP失败:', (error as Error).message)
    }
  }

  if (IS_DEV) {
    console.warn('[定位] 公网IP获取失败：所有来源均不可用', errors)
  } else {
    console.warn('[定位] 公网IP获取失败：所有来源均不可用')
  }

  return null
}
