/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 文件名称: RunStatus.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 运行状态常量类，定义 Agent 事件生命周期的所有可能状态值。
 * 这些状态用于 Event、Message 和 Response 对象的状态字段。
 */

package io.agentscope.runtime.engine.schemas;

/**
 * 运行状态常量类。
 *
 * <p>角色：集中定义 Agent 运行时中所有事件、消息和响应的状态常量，
 * 避免魔法字符串的散布。</p>
 *
 * <p>设计模式：常量类（Constants Class）—— 工具类模式，私有构造函数防止实例化。</p>
 */
public class RunStatus {
    /** 已创建 —— 事件已创建但尚未开始处理 */
    public static final String CREATED = "created";
    /** 处理中 —— 事件正在被处理 */
    public static final String IN_PROGRESS = "in_progress";
    /** 已完成 —— 事件已成功处理 */
    public static final String COMPLETED = "completed";
    /** 已取消 —— 事件被取消 */
    public static final String CANCELED = "canceled";
    /** 失败 —— 事件处理失败 */
    public static final String FAILED = "failed";
    /** 已拒绝 —— 事件被拒绝（如权限不足等） */
    public static final String REJECTED = "rejected";
    /** 未知 —— 状态不明确 */
    public static final String UNKNOWN = "unknown";
    /** 已排队 —— 事件在队列中等待处理 */
    public static final String QUEUED = "queued";
    /** 不完整 —— 事件处理不完整（如输出被截断） */
    public static final String INCOMPLETE = "incomplete";

    /** 私有构造函数，防止实例化 */
    private RunStatus() {
        // 工具类，不允许实例化
    }
}

