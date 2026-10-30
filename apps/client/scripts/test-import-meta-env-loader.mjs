export async function load(url, context, next) {
  const result = await next(url, context)

  if (url.endsWith('/src/renderer/src/config/chatConfig.js')) {
    return {
      format: result.format || 'module',
      source: String(result.source).replace('import.meta.env.DEV', 'false')
    }
  }

  return result
}
