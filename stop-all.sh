#!/bin/bash
# 公厕管理系统 - 停止所有服务 (UOS/Linux)

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "============================================"
echo "   正在停止所有服务..."
echo "============================================"
echo ""

# 从 PID 文件停止后端
BACKEND_PID_FILE="$SCRIPT_DIR/.backend.pid"
if [ -f "$BACKEND_PID_FILE" ]; then
    BACKEND_PID=$(cat "$BACKEND_PID_FILE")
    if kill -0 "$BACKEND_PID" 2>/dev/null; then
        echo "[1/2] 停止后端服务 (PID: $BACKEND_PID)..."
        kill -9 "$BACKEND_PID" 2>/dev/null || true
    fi
    rm -f "$BACKEND_PID_FILE"
else
    # 后备方案：通过端口查找
    BACKEND_PID=$(lsof -ti:8083 2>/dev/null || ss -tlnp 2>/dev/null | grep ':8083' | grep -oP 'pid=\K[0-9]+' || true)
    if [ -n "$BACKEND_PID" ]; then
        echo "[1/2] 停止后端服务 (PID: $BACKEND_PID)..."
        kill -9 "$BACKEND_PID" 2>/dev/null || true
    fi
fi
echo "[1/2] 后端已停止"

# 从 PID 文件停止前端
FRONTEND_PID_FILE="$SCRIPT_DIR/.frontend.pid"
if [ -f "$FRONTEND_PID_FILE" ]; then
    FRONTEND_PID=$(cat "$FRONTEND_PID_FILE")
    if kill -0 "$FRONTEND_PID" 2>/dev/null; then
        echo "[2/2] 停止前端服务 (PID: $FRONTEND_PID)..."
        kill -9 "$FRONTEND_PID" 2>/dev/null || true
    fi
    rm -f "$FRONTEND_PID_FILE"
else
    # 后备方案：通过进程名查找
    FRONTEND_PID=$(pgrep -f "vite.*5176" 2>/dev/null || true)
    if [ -n "$FRONTEND_PID" ]; then
        echo "[2/2] 停止前端服务..."
        kill -9 "$FRONTEND_PID" 2>/dev/null || true
    fi
fi
echo "[2/2] 前端已停止"

# 额外清理：防止有残留的 vite 或 java 进程
pkill -f "toilet-server-1.0.0.jar" 2>/dev/null || true
pkill -f "vite.*5176" 2>/dev/null || true

echo ""
echo "============================================"
echo "   所有服务已停止"
echo "============================================"
echo ""

echo "按回车键关闭此窗口..."
read -r
