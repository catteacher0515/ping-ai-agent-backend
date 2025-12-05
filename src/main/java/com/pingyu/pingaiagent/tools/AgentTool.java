package com.pingyu.pingaiagent.tools;

/**
 * 案件 Refactor-001: 工具标记接口
 * <p>
 * 作用: 作为一个"身份证"，所有实现了这个接口的 Bean，
 * 都会被 ToolConfig 自动识别并注册给 AI。
 * <p>
 * (这是一个 Marker Interface，不需要定义任何方法)
 */
public interface AgentTool {
}