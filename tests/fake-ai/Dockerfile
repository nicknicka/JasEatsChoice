# 用标准库实现 OpenAI-compatible 假服务，镜像保持最小，仅供 E2E 使用。
FROM python:3.12-alpine

WORKDIR /app
COPY server.py /app/server.py

EXPOSE 4011
CMD ["python", "server.py"]
