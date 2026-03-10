# Team Panes 使用指南

**Date**: 2026-02-25
**Purpose**: 在 iTerm2 中实时查看团队成员活动状态

---

## 前提条件

- ✅ tmux 已安装 (`brew install tmux`)
- ✅ iTerm2 (推荐) 或 Terminal.app
- ✅ 团队管理脚本 (`team_manager.sh`)

---

## 快速开始

### 1. 启动团队 Panes

```bash
./team_manager.sh start
```

这将创建一个包含 7 个 panes 的 tmux session：
- 🏗️ android-architect
- ⚙️ android-engineer
- 🎨 compose-ui-designer
- 🧪 android-test-engineer
- 🎮 game-designer
- 📚 education-specialist
- ⚡ android-performance-expert

### 2. 导航 Panes

在 tmux session 中：
- **Ctrl+B 然后 ↑↓←→** - 在 panes 之间切换
- **Ctrl+B 然后 D** - 断开连接（保持后台运行）
- **Ctrl+B 然后 Z** - 缩放当前 pane（全屏）

### 3. 重新连接

```bash
./team_manager.sh attach
```

---

## 命令参考

| 命令 | 说明 |
|------|------|
| `./team_manager.sh start` | 创建 panes 并连接 |
| `./team_manager.sh attach` | 连接到现有 session |
| `./team_manager.sh status` | 查看团队状态 |
| `./team_manager.sh stop` | 停止 session |

---

## 工作流程

### 启动 Agent 并查看活动

**步骤 1**: 启动团队 panes
```bash
./team_manager.sh start
```

**步骤 2**: 切换到目标 pane（例如 android-engineer）
```
按 Ctrl+B 然后按 ↓ 多次直到到达目标 pane
```

**步骤 3**: 在该 pane 中准备监控 agent 输出
```bash
# 查找 agent 输出文件
ls -lt /private/tmp/claude-*/*/tasks/*.output | head -1

# 监控输出（替换为实际路径）
./monitor_agent.sh android-engineer /path/to/output
```

**步骤 4**: 从另一个终端启动 agent
```bash
# 使用 Claude Code 启动 agent
Task(..., name="android-engineer", ...)
```

**步骤 5**: 在 pane 中实时看到 agent 活动 ✅

---

## 当前限制

### 1. Agent 启动分离

- Agents 通过 `Task` 工具启动（后台模式）
- Panes 用于**监控** agent 输出，不是直接运行 agent
- 需要手动在 pane 中运行 `monitor_agent.sh`

### 2. 权限请求机制

当前问题：
- Agent 在输出中打印 "Shall I proceed?" 而不是使用 SendMessage
- 需要配置 `allowedPrompts` 来启用正确的权限请求

未来改进：
- 配置团队权限边界
- Agent 使用 SendMessage 请求权限
- 通过 SendMessage 批准/拒绝

### 3. iTerm2 集成

当前配置使用 tmux（不是 iTerm2 原生 panes）：
- tmux panes 在 iTerm2 中正常工作
- 但需要手动启动 tmux session
- 无法自动创建 iTerm2 panes（需要 Python API）

---

## 配置文件

### Team Manager
- **位置**: `team_manager.sh`
- **用途**: 创建和管理 tmux panes
- **配置**: MEMBERS 数组定义团队成员

### Monitor Script
- **位置**: `monitor_agent.sh`
- **用途**: 实时显示 agent 输出
- **用法**: `./monitor_agent.sh <agent-name> <output-file>`

### Team Config
- **位置**: `~/.claude/teams/wordland-dev-team/config.json`
- **状态**: 30 个成员，配置了 iTerm2 backend（未使用）
- **需要更新**: 移除 iTerm2 配置，添加权限边界

---

## 示例场景

### 场景 1: 启动 android-engineer 查看 Task #1 进度

```bash
# Terminal 1: 启动 team panes
./team_manager.sh start

# 在 tmux 中切换到 android-engineer pane
# 按 Ctrl+B 然后 ↓ 到达第二个 pane

# 运行监控
./monitor_agent.sh android-engineer /path/to/a36cdda.output
```

现在可以看到 agent 的实时输出。

### 场景 2: 同时监控多个 agents

- 启动 team panes
- 每个 pane 监控一个 agent
- 在 panes 之间快速切换查看进度

### 场景 3: Debug agent 问题

```bash
# 查看所有 agents 的输出文件
ls -lt /private/tmp/claude-*/*/tasks/*.output

# 在对应的 pane 中监控
./monitor_agent.sh <agent-name> <output-file>
```

---

## 故障排查

### 问题: Session 已存在

```bash
# 解决方案 1: 连接到现有 session
./team_manager.sh attach

# 解决方案 2: 杀死并重新创建
tmux kill-session -t wordland-team
./team_manager.sh start
```

### 问题: Pane 中没有输出

- 检查 agent 是否正在运行：`TaskList`
- 检查输出文件路径：`ls /private/tmp/...`
- 确保在正确的 pane 中运行了 `monitor_agent.sh`

### 问题: 无法切换 panes

- 确保 `Ctrl+B` 按键序列正确（先按 Ctrl+B，松开，再按方向键）
- 检查是否在 tmux session 中（底部会有状态栏）

---

## 下一步改进

1. **自动化启动** - 脚本自动启动 agent 并连接监控
2. **权限请求** - 配置 `allowedPrompts` 启用 SendMessage
3. **UI 改进** - iTerm2 原生集成（需要 Python API）
4. **状态同步** - TaskList 工具与 pane 状态同步

---

**需要帮助？**
- 运行 `./team_manager.sh help`
- 查看配置文件 `team_manager.sh`
- 检查 tmux manual: `man tmux`
