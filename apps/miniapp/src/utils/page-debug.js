import { debug, info, warn, error } from '@/utils/logger'

const normalizeError = (value) => {
	if (value instanceof Error) {
		return {
			message: value.message,
			stack: value.stack
		}
	}

	return value
}

export const createPageDebug = (pageName) => {
	const context = `页面调试:${pageName}`

	return {
		lifecycle(stage, data) {
			info(`[生命周期] ${stage}`, data, context)
		},
		action(name, data) {
			debug(`[交互] ${name}`, data, context)
		},
		requestStart(name, data) {
			info(`[请求开始] ${name}`, data, context)
		},
		requestSuccess(name, data) {
			debug(`[请求成功] ${name}`, data, context)
		},
		requestFail(name, err) {
			error(`[请求失败] ${name}`, normalizeError(err), context)
		},
		state(name, data) {
			debug(`[状态] ${name}`, data, context)
		},
		anomaly(name, data) {
			warn(`[异常] ${name}`, data, context)
		}
	}
}
