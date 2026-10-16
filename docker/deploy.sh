#!/usr/bin/env bash
# 迷失客客の博客 一键部署脚本（服务器上、本目录内执行）
# 首次运行会生成 .env 模板并退出，修改其中的密钥后重新执行即可。
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -f .env ]; then
  cp .env.example .env
  echo "已生成 docker/.env，请修改其中的 MYSQL_PASSWORD / JWT_SECRET / INTERNAL_TOKEN 后重新执行本脚本。"
  exit 1
fi

docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps
echo
echo "部署完成：http://<服务器IP>/（默认管理员 admin / admin123，请尽快在后台修改密码）"
