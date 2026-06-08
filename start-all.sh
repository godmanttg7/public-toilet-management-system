#!/bin/bash
# 公厕管理系统 - 一键启动脚本 (UOS/Linux · 达梦数据库)

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "============================================"
echo "   公厕管理系统 - 一键启动"
echo "============================================"
echo ""

# ========== 编译后端 ==========
echo "[后端] 正在编译..."
cd "$SCRIPT_DIR/toilet-server"
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "[错误] 后端编译失败！"
    exit 1
fi
echo "[后端] 编译成功"

# ========== 启动后端 ==========
echo "[后端] 正在启动 (端口 8083)..."
nohup java -jar "$SCRIPT_DIR/toilet-server/target/toilet-server-1.0.0.jar" > "$SCRIPT_DIR/toilet-server/backend.log" 2>&1 &
BACKEND_PID=$!
echo "$BACKEND_PID" > "$SCRIPT_DIR/.backend.pid"

echo "[后端] 等待启动 (约15秒)..."
sleep 15

# ========== 启动前端 ==========
echo ""
echo "[前端] 正在启动 (端口 5176)..."
cd "$SCRIPT_DIR/toilet-frontend"
if [ ! -d "node_modules" ]; then
    echo "[前端] 首次运行，安装依赖中..."
    npm install
fi

nohup npx vite --port 5176 --host > "$SCRIPT_DIR/toilet-frontend/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo "$FRONTEND_PID" > "$SCRIPT_DIR/.frontend.pid"

sleep 3

# ========== 完成 ==========
echo ""
echo "============================================"
echo "   启动完成！"
echo ""
echo "   前端: http://localhost:5176"
echo "   后端: http://localhost:8083"
echo ""
echo "   登录: admin / 123456"
echo "============================================"
echo ""

xdg-open http://localhost:5176 2>/dev/null || true

echo "按回车键关闭此窗口（服务继续运行）..."
read -r
