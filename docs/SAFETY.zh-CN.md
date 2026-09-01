# 🔐 安全说明

[English](SAFETY.md) | [简体中文](SAFETY.zh-CN.md)

## 🔑 敏感数据

不要把以下数据无条件注入模型：

- 密码；
- Access Token；
- API Key；
- 私钥；
- 未脱敏的高敏感业务信息。

## 🛡️ 权限边界

`ContextSource` 必须遵守应用本身的权限体系。

ACO 负责“编排上下文”，不负责替代业务授权系统。

## 🗄️ 数据库访问

优先：

```text
明确查询
只读访问
字段白名单
结果数量限制
```

避免开放式、无限制的 NL2SQL。

## 🧠 Prompt Injection

ACO 使用 `<business-context>` 与策略说明降低模型把业务数据误认为系统指令的风险。

但这不是完整的 Prompt Injection 防御方案。

真正的安全仍依赖：

- 数据源可信度；
- 权限控制；
- allowlist；
- 输入输出校验；
- 敏感字段过滤。

## 📝 日志

默认不要把完整业务上下文写入日志。

诊断信息优先记录：

```text
source id
execution result
rejection reason
budget usage
```

而不是原始敏感内容。