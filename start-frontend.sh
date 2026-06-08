#!/bin/bash
# 公厕管理系统 - 前端启动脚本 (UOS/Linux)

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "============================================"
echo "   公厕管理系统 - 前端服务启动"
echo "============================================"
echo ""

cd "$SCRIPT_DIR/toilet-frontend"

echo "[1/2] 检查依赖..."
if [ ! -d "node_modules" ]; then
    echo "首次运行，正在安装依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "[错误] 依赖安装失败！"
        exit 1
    fi
else
    echo "[1/2] 依赖已就绪"
fi

echo ""
echo "[2/2] 正在启动前端服务 (端口 5176)..."
echo ""

nohup npx vite --port 5176 --host --open > "$SCRIPT_DIR/toilet-frontend/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo "$FRONTEND_PID" > "$SCRIPT_DIR/.frontend.pid"

echo ""
echo "============================================"
echo "   前端服务已启动！"
echo "   地址: http://localhost:5176"
echo "   日志: toilet-frontend/frontend.log"
echo "============================================"
echo ""

echo "按回车键关闭此窗口（前端继续运行）..."
read -r
