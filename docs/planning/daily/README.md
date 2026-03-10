# 每日总结目录

**创建日期**: 2026-02-28
**用途**: 记录每日工作总结和进度更新

---

## 📋 目录说明

本目录存储所有每日工作总结文档，便于追踪项目进度和决策历史。

---

## 📝 命名规范

### 推荐格式

- **日期格式**: `YYYY-MM-DD.md`
- **备选格式**: `DAILY_SUMMARY_YYYY-MM-DD.md`
- **主题格式**: `{TOPIC}_DAILY_SUMMARY_YYYYMMDD.md`

### 示例

- ✅ `2026-02-26.md`
- ✅ `DAILY_SUMMARY_2026-02-25.md`
- ✅ `epic6_daily_summary_20260226.md`

### 排序规则

- 按日期**倒序**排列（最新的在上面）
- 相同日期的文件按时间戳排序

---

## 📊 文件列表

| 文件名 | 日期 | 主题 | 状态 |
|--------|------|------|------|
| DONE_2026-02-26.md | 2026-02-26 | Epic #8.2 + Epic #9 + Epic #6 | ✅ |
| epic6_daily_summary_20260226.md | 2026-02-26 | Epic #6 音频系统 | 🔄 |
| DAILY_SUMMARY_2026-02-25.md | 2026-02-25 | 每日总结 | ✅ |
| TODAY_WORK_SUMMARY_2026-02-25.md | 2026-02-25 | 每日工作总结 | ✅ |

---

## 🔍 快速查找

### 查找特定日期

```bash
# 查找某日的总结
ls docs/planning/daily/*2026-02-26*

# 或使用grep搜索
grep -r "Epic #9" docs/planning/daily/
```

### 查找特定主题

```bash
# 查找Epic相关的总结
grep -r "Epic #" docs/planning/daily/

# 查找特定Epic
grep -r "Epic #6" docs/planning/daily/
```

---

## 📖 相关文档

- **Planning Index**: `../README.md`
- **Epic Index**: `../EPIC_INDEX.md`
- **Pending Tasks**: `../PENDING_TASKS_BACKLOG.md`

---

## 🔄 维护规则

### 归档规则

- **保留期限**: 最近30天的每日总结保留在 `daily/` 目录
- **归档位置**: 超过30天的文档移至 `docs/history/daily/`
- **归档时间**: 每月1日执行归档

### 清理规则

- **重复文档**: 合并相同日期的多个总结文件
- **过时文档**: 移至历史目录
- **临时文档**: 完成后及时删除或归档

---

**维护者**: Team Lead
**更新频率**: 每日更新，每月归档
