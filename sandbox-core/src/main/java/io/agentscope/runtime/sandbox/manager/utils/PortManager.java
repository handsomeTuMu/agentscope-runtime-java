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
 * 文件名称: PortManager.java
 * 模块: sandbox-core
 * 包: io.agentscope.runtime.sandbox.manager.utils
 *
 * 线程安全的端口分配管理器，负责端口资源的分配、注册和释放。
 * 管理端口范围（PortRange），避免端口冲突，支持按容器名追踪端口归属。
 */
package io.agentscope.runtime.sandbox.manager.utils;

import io.agentscope.runtime.sandbox.manager.model.container.PortRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程安全的端口分配管理器。
 *
 * <p>角色：管理沙箱容器的端口分配，避免端口冲突。在 Docker 容器创建时，
 * 需要分配宿主机端口映射到容器内端口，PortManager 负责分配可用的宿主机端口。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>在指定端口范围内分配空闲端口</li>
 *   <li>按容器名注册和追踪端口归属</li>
 *   <li>释放单个端口或按容器名批量释放端口</li>
 *   <li>查询已分配和可用端口数量</li>
 * </ul>
 *
 * <p>设计模式：资源池模式 —— 集中管理端口资源，分配和回收；
 * 使用 ConcurrentHashMap 和 synchronized 保证线程安全。</p>
 */
public class PortManager {
    private static final Logger logger = LoggerFactory.getLogger(PortManager.class);
    
    private final Set<Integer> allocatedPorts = ConcurrentHashMap.newKeySet();
    
    private final ConcurrentHashMap<String, Set<Integer>> containerPorts = new ConcurrentHashMap<>();
    
    private final PortRange portRange;
    
    public PortManager(PortRange portRange) {
        this.portRange = portRange;
        logger.info("PortManager initialized with port range: {}-{}", portRange.getStart(), portRange.getEnd());
    }
    
    /**
     * Allocate a free port
     * Thread-safe operation that guarantees no port conflicts
     * 
     * @return An available port number, or -1 if no port is available
     */
    public synchronized int allocatePort() {
        int startPort = portRange.getStart();
        int endPort = portRange.getEnd();
        
        for (int port = startPort; port <= endPort; port++) {
            if (allocatedPorts.contains(port)) {
                continue;
            }
            
            if (isPortAvailable(port)) {
                allocatedPorts.add(port);
                logger.info("Allocated port: {}", port);
                return port;
            }
        }

        logger.warn("No available ports in range {}-{}", startPort, endPort);
        return -1;
    }
    
    /**
     * Allocate multiple ports
     * 
     * @param count Number of ports to allocate
     * @return Array of allocated ports, or null if not enough ports available
     */
    public synchronized int[] allocatePorts(int count) {
        int[] ports = new int[count];
        Set<Integer> tempAllocated = new HashSet<>();
        
        try {
            for (int i = 0; i < count; i++) {
                int port = allocatePort();
                if (port == -1) {
                    for (int allocated : tempAllocated) {
                        allocatedPorts.remove(allocated);
                    }
                    return null;
                }
                ports[i] = port;
                tempAllocated.add(port);
            }
            return ports;
        } catch (Exception e) {
            for (int allocated : tempAllocated) {
                allocatedPorts.remove(allocated);
            }
            throw e;
        }
    }
    
    /**
     * Register ports for a container
     * This allows tracking which ports belong to which container
     * 
     * @param containerName The container name
     * @param ports The ports to register
     */
    public void registerContainerPorts(String containerName, int... ports) {
        Set<Integer> portSet = containerPorts.computeIfAbsent(
            containerName, 
            k -> ConcurrentHashMap.newKeySet()
        );
        for (int port : ports) {
            portSet.add(port);
        }
        logger.info("Registered {} port(s) for container: {}", ports.length, containerName);
    }
    
    /**
     * Release a specific port
     * 
     * @param port The port to release
     */
    public synchronized void releasePort(int port) {
        if (allocatedPorts.remove(port)) {
            logger.info("Released port: {}", port);
        }
    }
    
    /**
     * Release all ports for a container
     * 
     * @param containerName The container name
     */
    public synchronized void releaseContainerPorts(String containerName) {
        Set<Integer> ports = containerPorts.remove(containerName);
        if (ports != null) {
            for (int port : ports) {
                allocatedPorts.remove(port);
            }
            logger.info("Released {} port(s) for container: {}", ports.size(), containerName);
        }
    }
    
    /**
     * Release multiple ports
     * 
     * @param ports The ports to release
     */
    public synchronized void releasePorts(int... ports) {
        for (int port : ports) {
            releasePort(port);
        }
    }
    
    /**
     * Check if a port is available on the system
     * 
     * @param port The port to check
     * @return true if available, false otherwise
     */
    private boolean isPortAvailable(int port) {
        String[] addressesToTest = {"0.0.0.0", "127.0.0.1", "localhost"};
        
        for (String addr : addressesToTest) {
            try (ServerSocket socket = new ServerSocket()) {
                socket.setReuseAddress(false);
                socket.bind(new InetSocketAddress(addr, port), 1);
            } catch (IOException e) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get the number of allocated ports
     * 
     * @return The count of allocated ports
     */
    public int getAllocatedPortCount() {
        return allocatedPorts.size();
    }
    
    /**
     * Get the number of available ports
     * 
     * @return The count of available ports
     */
    public int getAvailablePortCount() {
        int total = portRange.getEnd() - portRange.getStart() + 1;
        return total - allocatedPorts.size();
    }
    
    /**
     * Clear all allocated ports (for cleanup)
     */
    public synchronized void clear() {
        allocatedPorts.clear();
        containerPorts.clear();
        logger.info("Cleared all port allocations");
    }
}

