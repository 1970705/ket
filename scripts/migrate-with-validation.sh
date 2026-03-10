#!/bin/bash

# Database Migration with Validation Script
# 用于安全地执行数据库迁移并验证结果

set -e  # 遇到错误立即退出

# 配置
PROJECT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
APP_DB="wordland.db"
BACKUP_DB="wordland_backup_$(date +%Y%m%d_%H%M%S).db"
DEVICE_ID=""
PACKAGE_NAME="com.wordland"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示帮助信息
show_help() {
    cat << 'EOF'
Usage: ./migrate-with-validation.sh [OPTIONS]

Database Migration with Validation Script

Options:
    --device <id>          Android device ID (optional)
    --backup-only          只备份数据库，不执行迁移
    --validate-only       只验证现有数据库，不执行迁移
    --force                强制执行迁移（跳过确认）
    -h, --help             显示此帮助信息

Examples:
    ./migrate-with-validation.sh --device emulator-5554
    ./migrate-with-validation.sh --backup-only
    ./migrate-with-validation.sh --validate-only

EOF
}

# 解析参数
BACKUP_ONLY=false
VALIDATE_ONLY=false
FORCE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --device)
            DEVICE_ID="$2"
            shift 2
            ;;
        --backup-only)
            BACKUP_ONLY=true
            shift
            ;;
        --validate-only)
            VALIDATE_ONLY=true
            shift
            ;;
        --force)
            FORCE=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# 检查设备连接
check_device() {
    if [ -z "$DEVICE_ID" ]; then
        DEVICE_ID=$(adb devices | grep -v "List" | awk 'NR==2 {print $1}')
        if [ -z "$DEVICE_ID" ]; then
            log_error "No Android device found. Please connect a device."
            exit 1
        fi
        log_info "Using device: $DEVICE_ID"
    fi
}

# 备份数据库
backup_database() {
    log_info "Backing up database..."

    # 拉取数据库
    adb -s "$DEVICE_ID" shell "su -c 'cat /data/data/$PACKAGE_NAME/databases/$APP_DB'" > /tmp/$APP_DB 2>/dev/null || {
        log_error "Failed to pull database from device"
        exit 1
    }

    # 创建备份文件名
    local backup_file="$PROJECT_DIR/docs/backups/$BACKUP_DB"
    mkdir -p "$(dirname "$backup_file")"

    # 复制到备份目录
    cp /tmp/$APP_DB "$backup_file" || {
        log_error "Failed to create backup"
        exit 1
    }

    # 清理临时文件
    rm -f /tmp/$APP_DB

    log_info "Database backed up to: $backup_file"
    return 0
}

# 验证数据库
validate_database() {
    log_info "Validating database..."

    # 检查数据库是否存在
    adb -s "$DEVICE_ID" shell "su -c 'ls -l /data/data/$PACKAGE_NAME/databases/$APP_DB'" > /dev/null 2>&1 || {
        log_error "Database not found on device"
        return 1
    }

    # 检查数据库完整性
    local integrity_check=$(adb -s "$DEVICE_ID" shell "su -c 'PRAGMA integrity_check'" 2>&1)
    if [[ $integrity_check == *"ok"* ]]; then
        log_info "Database integrity: OK"
    else
        log_warn "Database integrity check failed"
    fi

    # 检查数据库版本
    local db_version=$(adb -s "$DEVICE_ID" shell "su -c 'PRAGMA user_version'" 2>&1 | tr -d '\r')
    log_info "Database version: $db_version"

    return 0
}

# 测试迁移
test_migration() {
    log_info "Testing migration..."

    # 运行迁移测试
    cd "$PROJECT_DIR"
    ./gradlew test --tests "*MigrationTest" || {
        log_error "Migration tests failed"
        return 1
    }

    log_info "Migration tests passed"
    return 0
}

# 执行迁移
execute_migration() {
    log_info "Executing database migration..."

    # 卸载旧版本（如果有）
    adb -s "$DEVICE_ID" uninstall "$PACKAGE_NAME" 2>/dev/null || true

    # 安装新版本
    log_info "Installing new version..."
    adb -s "$DEVICE_ID" install app/build/outputs/apk/debug/app-debug.apk || {
        log_error "Failed to install APK"
        return 1
    }

    # 启动应用（触发迁移）
    log_info "Starting app to trigger migration..."
    adb -s "$DEVICE_ID" shell am start -n "$PACKAGE_NAME/.ui.MainActivity"

    # 等待迁移完成
    sleep 5

    # 检查崩溃日志
    local crash_logs=$(adb -s "$DEVICE_ID" logcat -d | grep -i "fatal\|android.database.sqlite" | head -20)
    if [ -n "$crash_logs" ]; then
        log_error "Migration failed. Crash logs:"
        echo "$crash_logs"
        return 1
    fi

    log_info "Migration completed successfully"
    return 0
}

# 回滚迁移
rollback_migration() {
    log_warn "Rolling back migration..."

    # 卸载当前版本
    adb -s "$DEVICE_ID" uninstall "$PACKAGE_NAME"

    # 恢复备份版本
    log_info "Restoring backup..."
    # 这里需要根据实际情况调整
    log_warn "Please manually install the previous version"
}

# 主流程
main() {
    log_info "=== Database Migration with Validation ==="

    # 如果只备份
    if [ "$BACKUP_ONLY" = true ]; then
        check_device
        backup_database
        log_info "Backup completed. Exiting."
        exit 0
    fi

    # 如果只验证
    if [ "$VALIDATE_ONLY" = true ]; then
        check_device
        validate_database
        log_info "Validation completed. Exiting."
        exit 0
    fi

    # 完整流程
    check_device

    # 备份
    backup_database

    # 验证迁移前状态
    log_info "Pre-migration validation:"
    validate_database

    # 确认执行
    if [ "$FORCE" != "true" ]; then
        echo ""
        read -p "Ready to execute migration. Continue? (y/N) " -n 1 -r answer
        echo
        if [[ ! $answer =~ ^[Yy]$ ]]; then
            log_info "Migration cancelled by user."
            exit 0
        fi
    fi

    # 执行迁移
    execute_migration

    # 验证迁移后状态
    log_info "Post-migration validation:"
    validate_database

    # 测试迁移
    test_migration

    log_info "=== Migration completed successfully ==="
}

# 捕获错误并回滚
trap 'log_error "Migration failed. Initiating rollback..."; rollback_migration; exit 1' ERR

# 执行主流程
main
