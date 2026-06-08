#!/bin/bash
# 公厕管理系统 - 后端启动脚本 (UOS/Linux)

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "============================================"
echo "   公厕管理系统 - 后端服务启动"
echo "============================================"
echo ""

cd "$SCRIPT_DIR/toilet-server"

echo "[1/2] 正在编译项目..."
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "[错误] 编译失败！"
    exit 1
fi
echo "[1/2] 编译成功！"
echo ""

echo "[2/2] 正在启动后端服务 (端口 8083)..."
echo ""

nohup java -jar "$SCRIPT_DIR/toilet-server/target/toilet-server-1.0.0.jar" > "$SCRIPT_DIR/toilet-server/backend.log" 2>&1 &
BACKEND_PID=$!
echo "$BACKEND_PID" > "$SCRIPT_DIR/.backend.pid"

echo ""
echo "============================================"
echo "   后端服务已启动！"
echo "   地址: http://localhost:8083"
echo "   日志: toilet-server/backend.log"
echo "============================================"
echo ""

echo "按回车键关闭此窗口（后端继续运行）..."
read -r
