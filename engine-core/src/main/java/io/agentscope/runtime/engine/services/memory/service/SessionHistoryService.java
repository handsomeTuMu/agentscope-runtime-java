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
 * 文件名称: SessionHistoryService.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.services.memory.service
 *
 * 会话历史管理服务接口，定义对话会话的标准 CRUD 操作。
 * 负责创建、检索、删除会话以及向会话追加消息。
 * 实现类包括 InMemorySessionHistoryService、RedisSessionHistoryService 等。
 */
package io.agentscope.runtime.engine.services.memory.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.agentscope.runtime.engine.schemas.Message;
import io.agentscope.runtime.engine.schemas.Session;
import io.agentscope.runtime.engine.shared.Service;

/**
 * 会话历史管理服务接口。
 *
 * <p>角色：定义对话会话（Session）的标准管理操作，包括创建、检索、删除
 * 和列出会话，以及向会话追加消息历史。所有方法返回 CompletableFuture（异步设计）。</p>
 *
 * <p>设计模式：策略模式 —— 不同的后端实现提供各自的会话存储方案。</p>
 */
public interface SessionHistoryService extends Service {
    
    /**
     * Create a new session for the specified user
     *
     * @param userId User identifier
     * @param sessionId Optional session ID, automatically generated if null
     * @return CompletableFuture<Session> Asynchronously created new session object
     */
    CompletableFuture<Session> createSession(String userId, Optional<String> sessionId);
    
    /**
     * Retrieve a specific session
     *
     * @param userId User identifier
     * @param sessionId Session identifier to retrieve
     * @return CompletableFuture<Optional<Session>> Asynchronous retrieval result, returns session object if found, otherwise returns empty
     */
    CompletableFuture<Optional<Session>> getSession(String userId, String sessionId);
    
    /**
     * Delete a specific session
     *
     * @param userId User identifier
     * @param sessionId Session identifier to delete
     * @return CompletableFuture<Void> Asynchronous deletion result
     */
    CompletableFuture<Void> deleteSession(String userId, String sessionId);
    
    /**
     * List all sessions for the specified user
     *
     * @param userId User identifier
     * @return CompletableFuture<List<Session>> Asynchronous session list result
     */
    CompletableFuture<List<Session>> listSessions(String userId);
    
    /**
     * Append messages to the history of a specific session
     *
     * @param session Session to append messages to
     * @param messages Message or list of messages to append
     * @return CompletableFuture<Void> Asynchronous append result
     */
    CompletableFuture<Void> appendMessage(Session session, List<Message> messages);
}
