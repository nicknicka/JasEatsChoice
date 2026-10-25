# JasEatsChoice 一键入口
#
#   make doctor      检查 Docker、端口、.env、依赖是否就绪
#   make env         生成 deploy/.env（随机密码与 JWT 密钥，永不入库）
#   make up          起开发栈（MySQL + Redis + Spring Boot + nginx），并等健康
#   make down        停栈（保留数据卷）
#   make reset-db    删掉 MySQL 数据卷并重新导入 db/init（会清空现有数据）
#   make web         构建 Web 版静态产物到 apps/client/dist-web
#   make test        跑全部 107 条 Playwright 用例
#   make test-smoke  只跑 01/04/10 三条冒烟
#   make desktop     Electron 打包回归（build:mac）
#   make logs        跟随后端日志

SHELL := /bin/bash
ROOT  := $(shell pwd)
COMPOSE_DEV  := docker compose --env-file deploy/.env -f deploy/compose.yml -f deploy/compose.dev.yml
COMPOSE_PROD := docker compose --env-file deploy/.env -f deploy/compose.yml -f deploy/compose.prod.yml
NPM := npm --no-audit --no-fund

.PHONY: doctor env deps up down ps logs reset-db web desktop test test-smoke lint nginx-test config-prod

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

# 数据卷是唯一的数据来源；db/init 下的脚本只在空卷时执行
reset-db:
	$(COMPOSE_DEV) down -v
	$(COMPOSE_DEV) up -d mysql
	@echo "等待 MySQL 导入 db/init…"
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

# 用例需要三样东西：容器栈、渲染进程 dev server（5173）、以及可登录的种子账号
test:
	$(NPM) --prefix tests run test

test-smoke:
	cd tests && npx playwright test 01-auth.spec.js 04-order.spec.js 10-merchant-order-dish.spec.js

lint:
	cd apps/client && $(NPM) run lint

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
