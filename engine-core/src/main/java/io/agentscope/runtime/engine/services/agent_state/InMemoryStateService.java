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
 * 文件名称: InMemoryStateService.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.services.agent_state
 *
 * StateService 的内存实现版本，使用嵌套 Map 结构存储 Agent 状态。
 * 适用于开发测试场景或不需要持久化的短期运行场景。
 * 支持多用户、多会话和非连续轮次 ID。
 */

package io.agentscope.runtime.engine.services.agent_state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StateService 的内存实现。
 *
 * <p>角色：使用嵌套 ConcurrentHashMap 结构在 JVM 内存中存储 Agent 状态数据。
 * 支持多用户、多会话和非连续轮次 ID。当 round_id 为 null 时保存状态，
 * 会自动分配新的递增轮次 ID；当 round_id 为 null 时导出状态，返回最新轮次的状态。</p>
 *
 * <p>数据结构：</p>
 * <pre>
 * { user_id: { session_id: { round_id: state_dict } } }
 * </pre>
 *
 * <p>设计模式：策略模式 —— StateService 接口的具体实现之一；
 * 深拷贝防护 —— 保存和读取时都执行深拷贝，防止外部修改影响存储数据。</p>
 *
 * <p><b>线程安全说明：</b>使用 ConcurrentHashMap 保证并发安全。</p>
 */
public class InMemoryStateService extends StateService {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryStateService.class);
    
    private static final String DEFAULT_SESSION_ID = "default";
    
    // Structure: { user_id: { session_id: { round_id: state_dict } } }
    private Map<String, Map<String, Map<Integer, Map<String, Object>>>> store;
    private boolean health = false;
    
    public InMemoryStateService() {
        this.store = null;
        this.health = false;
    }
    
    @Override
    public void start() {
        if (store == null) {
            store = new ConcurrentHashMap<>();
        }
        health = true;
    }
    
    @Override
    public void stop() {
        if (store != null) {
            store.clear();
        }
        store = null;
        health = false;
    }
    
    @Override
    public boolean health() {
        return health;
    }
    
    @Override
    public CompletableFuture<Integer> saveState(
            String userId,
            Map<String, Object> state,
            String sessionId,
            Integer roundId) {
        if (store == null) {
            return CompletableFuture.failedFuture(
                new RuntimeException("Service not started"));
        }
        
        String sid = sessionId != null ? sessionId : DEFAULT_SESSION_ID;
        
        store.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        store.get(userId).computeIfAbsent(sid, k -> new ConcurrentHashMap<>());
        
        Map<Integer, Map<String, Object>> roundsDict = store.get(userId).get(sid);
        
        // Auto-generate round_id if not provided
        if (roundId == null) {
            if (!roundsDict.isEmpty()) {
                roundId = Collections.max(roundsDict.keySet()) + 1;
            } else {
                roundId = 1;
            }
        }
        
        // Store a deep copy so caller modifications don't affect saved state
        Map<String, Object> stateCopy = deepCopy(state);
        roundsDict.put(roundId, stateCopy);
        
        return CompletableFuture.completedFuture(roundId);
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> exportState(
            String userId,
            String sessionId,
            Integer roundId) {
        if (store == null) {
            return CompletableFuture.failedFuture(
                new RuntimeException("Service not started"));
        }
        
        String sid = sessionId != null ? sessionId : DEFAULT_SESSION_ID;
        Map<String, Map<Integer, Map<String, Object>>> sessions = store.get(userId);
        if (sessions == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        Map<Integer, Map<String, Object>> roundsDict = sessions.get(sid);
        if (roundsDict == null || roundsDict.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        if (roundId == null) {
            // Get the latest round_id
            Integer latestRoundId = Collections.max(roundsDict.keySet());
            Map<String, Object> state = roundsDict.get(latestRoundId);
            return CompletableFuture.completedFuture(deepCopy(state));
        }
        
        Map<String, Object> state = roundsDict.get(roundId);
        return CompletableFuture.completedFuture(state != null ? deepCopy(state) : null);
    }
    
    /**
     * Deep copy a map to prevent external modifications.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopy(Map<String, Object> original) {
        if (original == null) {
            return null;
        }
        Map<String, Object> copy = new HashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                copy.put(entry.getKey(), deepCopy((Map<String, Object>) value));
            } else if (value instanceof List) {
                copy.put(entry.getKey(), new ArrayList<>((List<?>) value));
            } else {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }
}

