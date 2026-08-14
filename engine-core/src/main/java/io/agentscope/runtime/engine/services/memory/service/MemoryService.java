/**
 * 文件名称: MemoryService.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.services.memory.service
 *
 * 长期记忆服务接口，用于存储和检索 Agent 的长期记忆。
 * 记忆按用户 ID 组织，支持两种管理策略：
 * 1. 按会话 ID 分组（会话 ID 隶属于用户 ID）
 * 2. 仅按用户 ID 分组
 * 支持数据库存储或内存存储，具体实现包括 InMemoryMemoryService、RedisMemoryService 等。
 */
package io.agentscope.runtime.engine.services.memory.service;

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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.agentscope.runtime.engine.schemas.Message;
import io.agentscope.runtime.engine.shared.Service;

/**
 * 长期记忆服务接口。
 *
 * <p>角色：为 Agent 提供跨会话的长期记忆存储和检索能力。
 * 记忆按用户 ID 组织，可选择按会话 ID 进一步分组。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>添加记忆（将消息存入长期记忆）</li>
 *   <li>搜索记忆（基于查询消息进行语义搜索）</li>
 *   <li>列出记忆（分页查询用户的记忆列表）</li>
 *   <li>删除记忆（清除指定用户或会话的记忆）</li>
 * </ul>
 *
 * <p>设计模式：策略模式 —— 不同的后端实现提供各自的记忆存储方案。</p>
 */
public interface MemoryService extends Service {
    
    /**
     * Add messages to memory service
     *
     * @param userId user ID
     * @param messages list of messages to add
     * @param sessionId optional session ID
     * @return CompletableFuture<Void> asynchronous addition result
     */
    CompletableFuture<Void> addMemory(String userId, List<Message> messages, Optional<String> sessionId);
    
    /**
     * Search messages from memory service
     *
     * @param userId user ID
     * @param messages user query or query with history messages, all in message list format
     * @param filters filters for searching memory
     * @return CompletableFuture<List<Message>> asynchronous search result
     */
    CompletableFuture<List<Message>> searchMemory(String userId, List<Message> messages, Optional<Map<String, Object>> filters);
    
    /**
     * List memory items for specified user, supports pagination and other filters
     *
     * @param userId user ID
     * @param filters filters for memory items, such as page_num, page_size, etc.
     * @return CompletableFuture<List<Message>> asynchronous list result
     */
    CompletableFuture<List<Message>> listMemory(String userId, Optional<Map<String, Object>> filters);
    
    /**
     * Delete memory items for specified user
     *
     * @param userId user ID
     * @param sessionId optional session ID, if provided only delete messages for that session, otherwise delete all messages for the user
     * @return CompletableFuture<Void> asynchronous deletion result
     */
    CompletableFuture<Void> deleteMemory(String userId, Optional<String> sessionId);
    
    /**
     * Get all users list
     *
     * @return CompletableFuture<List<String>> asynchronous user list result
     */
    CompletableFuture<List<String>> getAllUsers();
}
