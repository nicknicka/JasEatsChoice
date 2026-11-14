# JasEatsChoice 一键入口
#
#   make doctor      检查 Docker、端口、.env、依赖是否就绪
#   make env         生成 deploy/.env（随机密码与 JWT 密钥，永不入库）
#   make up          起开发栈（MySQL + Redis + Spring Boot + nginx），并等健康
#   make down        停栈（保留数据卷）
#   make reset-db    删掉 MySQL 数据卷，空卷启动后由后端 Flyway 重新迁移（会清空现有数据）
#   make web         构建 Web 版静态产物到 apps/client/dist-web
#   make test        跑全部 138 条 Playwright 用例
#   make test-smoke  只跑 01/04/10 三条冒烟
#   make check       静态防线：单测 + 路由漂移 + 小程序 API 调用 + lint 门禁（不起容器）
#   make build-miniapp  构建微信小程序产物（mp-weixin）
#   make build-h5       构建 H5 产物并校验完整性（#583）
#   make desktop     Electron 打包回归（build:mac）
#   make screenshots 全量网页截图 + 索引页（产出在 output/，不入库）
#   make logs        跟随后端日志

SHELL := /bin/bash
ROOT  := $(shell pwd)
COMPOSE_DEV  := docker compose --env-file deploy/.env -f deploy/compose.yml -f deploy/compose.dev.yml
COMPOSE_PROD := docker compose --env-file deploy/.env -f deploy/compose.yml -f deploy/compose.prod.yml
NPM := npm --no-audit --no-fund
# 后端路由来源：auto=栈在跑就读 OpenAPI，否则静态解析 Controller；CI 一般走 static
CHECK_SOURCE ?= auto

.PHONY: doctor env deps up down ps logs reset-db web desktop screenshots test test-smoke lint \
	check check-unit check-routes check-miniapp-api check-lint check-privacy build-miniapp build-h5 check-h5 nginx-test config-prod agent-check

doctor:
	@echo "== Docker =="
	@docker info >/dev/null 2>&1 && echo "  OK: 守护进程可用（$$(docker --version)）" \
		|| { echo "  FAIL: Docker 守护进程没起来，请先打开 Docker Desktop"; exit 1; }
	@echo "== compose 文件 =="
	@test -f deploy/compose.yml && echo "  OK: deploy/compose.yml"
	@echo "== deploy/.env =="
	@test -f deploy/.env && echo "  OK: deploy/.env（未入库）" \
		|| { echo "  FAIL: 先执行 make env"; exit 1; }
	@echo "== 端口占用 =="
	@for p in 3306 6379 7777 11277 80; do \
		if lsof -nP -iTCP:$$p -sTCP:LISTEN >/dev/null 2>&1; then \
			echo "  WARN: 端口 $$p 已被占用"; \
		else \
			echo "  OK: 端口 $$p 空闲"; \
		fi; \
	done
	@echo "== Node 依赖 =="
	@test -d apps/client/node_modules && echo "  OK: apps/client" || echo "  WARN: 执行 make deps"
	@test -d tests/node_modules && echo "  OK: tests" || echo "  WARN: 执行 make deps"

env:
	bash scripts/gen-env.sh

deps:
	$(NPM) --prefix apps/client install
	$(NPM) --prefix tests install

up:
	$(COMPOSE_DEV) up -d --build
	@echo "等待服务健康…"
	@for i in $$(seq 1 60); do \
		if [ -n "$$($(COMPOSE_DEV) ps --format '{{.Name}} {{.Status}}' | grep -c healthy)" ] && \
		   curl -fs http://localhost:7777/api/actuator/health >/dev/null 2>&1; then \
			echo "后端 UP"; break; \
		fi; sleep 5; \
	done
	@echo
	@$(COMPOSE_DEV) ps
	@echo
	@echo "接口: http://localhost/api   健康: http://localhost/nginx-health"
	@echo "前端开发服务器: cd apps/client && npm run dev:web  (http://localhost:5173)"

down:
	$(COMPOSE_DEV) down

ps:
	$(COMPOSE_DEV) ps

logs:
	$(COMPOSE_DEV) logs -f spring-boot

# 数据卷是唯一的数据来源；空卷启动后由后端 Flyway 依次执行迁移
reset-db:
	$(COMPOSE_DEV) down -v
	$(COMPOSE_DEV) up -d mysql
	@echo "等待 MySQL 就绪；后端启动时由 Flyway 执行迁移…"
	@for i in $$(seq 1 60); do \
		if $(COMPOSE_DEV) exec -T mysql mysqladmin ping -h localhost -uroot -p"$$MYSQL_ROOT_PASSWORD" >/dev/null 2>&1; then break; fi; \
		sleep 3; \
	done
	@$(COMPOSE_DEV) ps
	$(COMPOSE_DEV) up -d

web:
	cd apps/client && $(NPM) run build:web
	@echo "产物: apps/client/dist-web（prod overlay 的 WEB_ROOT 默认指向这里）"

desktop:
	cd apps/client && $(NPM) run build:mac

# 小程序构建门禁：不依赖容器与密钥，CI 与本地跑同一条命令
build-miniapp:
	cd apps/miniapp && $(NPM) run build:mp-weixin

# H5 构建（#583）：先出产物，再校验它不是 12KB 空壳。
# 这两步必须连着跑——「构建退出码 0 但产物只有 pinia」正是当初静默失效的形态，
# 单看命令成功没有意义，得看产物里到底有没有应用入口。
build-h5:
	cd apps/miniapp && $(NPM) run build:h5
	python3 scripts/check_h5_build.py

# H5 产物完整性门禁（#583）：只校验既有产物，不重新构建。
# 单独拆出来是为了让已构建过的环境（或只想复验门禁本身）不必再花一次构建时间。
check-h5:
	python3 scripts/check_h5_build.py

# 全量网页截图：按需拉起前端 dev server → 逐页截图 → 重建 index.html/README.md。
# 需要后端栈已 make up；截图与索引都落在 output/web-screenshots（.gitignore，不入库）。
screenshots:
	bash scripts/screenshots.sh

# 用例需要三样东西：容器栈、渲染进程 dev server（5173）、以及可登录的种子账号
test:
	$(NPM) --prefix tests run test

test-smoke:
	cd tests && npx playwright test 01-auth.spec.js 04-order.spec.js 10-merchant-order-dish.spec.js

# 后端单测：只起依赖（mysql/redis/qdrant），应用上下文由测试自己拉起。
# 脚本会临时导出 deploy/.env 的库凭据；Netty 端口已在 pom 里固定为随机端口。
test-backend:
	bash scripts/ci-backend.sh

lint:
	cd apps/client && $(NPM) run lint

# 静态防线：不起容器、不打桩，一分钟内把机械性问题挡在跑 5 分钟端到端之前。
# CI 与本地共用这三个目标，命令不在两处各自演化。
check: check-unit check-routes check-miniapp-api check-route-auth check-public-routes check-lint check-privacy

check-unit:
	python3 -m unittest discover -s scripts -p 'test_*.py'

check-routes:
	python3 scripts/check_routes.py --source $(CHECK_SOURCE)

# 鉴权档位清单（#576）：SecurityConfig 改默认拒绝后，「哪条路由匿名可调」只能对着
# 放行清单读。这里校验 docs/technical/route-auth-matrix.md 与当前代码一致，
# 防止有人改了放行清单却忘了同步文档（或反过来手改文档）。
check-route-auth:
	python3 scripts/gen_route_auth_matrix.py --check

# 公开清单一致性（#576）：桌面端 PUBLIC_API_PATHS 与小程序 AUTH_WHITELIST 的每条
# 都必须能被后端 permitAll 覆盖。漂移的失败形态是「打开页面被弹回登录」
# （前端不带令牌 → 401 → 全局拦截器清登录态），很难反查到白名单里的拼写错误。
check-public-routes:
	python3 scripts/check_public_route_consistency.py

# 小程序：页面调用的 xxxApi.method 是否真的存在于对应 api 模块里。
# 这类缺陷不会被编译或路由检查发现——调用瞬间抛 TypeError，若被 try/catch
# 吞掉就只剩一行 console.error，能长期潜伏。
check-miniapp-api:
	python3 scripts/check_miniapp_api.py

# 隐私守卫（规格里的 privacy:check）：跟踪路径不得含 uploads/ 目录段，
# 跟踪文本文件不得含高置信度私钥 / 云厂商 / 平台令牌。
check-privacy:
	python3 scripts/check_privacy.py

# eslint 自己会因 error 数非零退出 1，所以这里容忍它的退出码，
# 由 check_lint.py 拿冻结基线去判“有没有变差”。
check-lint:
	@cd apps/client && $(NPM) run lint:ci -- -f json -o $(ROOT)/.lint-report.json || true
	@python3 scripts/check_lint.py --json $(ROOT)/.lint-report.json

# 专用 Agent 账号隔离验证（需要该机器上已初始化 dsh 账号；一般用 sudo 跑）。
# 不进 make check：CI 里没有也没必要有专用账号。
agent-check:
	scripts/agent-runtime/verify-isolation.sh

# 只解析 compose，不启动
config-prod:
	$(COMPOSE_PROD) config --quiet && echo "prod overlay 解析通过"

# 在一次性容器里 nginx -t 两套配置
nginx-test:
	$(COMPOSE_DEV) config --quiet
	docker run --rm \
		-v $(ROOT)/deploy/nginx/nginx.conf:/etc/nginx/nginx.conf:ro \
		-v $(ROOT)/deploy/nginx/snippets:/etc/nginx/snippets:ro \
		-v $(ROOT)/deploy/nginx/conf.d/dev.conf:/etc/nginx/conf.d/default.conf:ro \
		nginx:1.25-alpine nginx -t
