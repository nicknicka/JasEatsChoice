import { register } from 'node:module'

register('./test-import-meta-env-loader.mjs', import.meta.url)
