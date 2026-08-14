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
 * 文件名称: LocalDeployManager.java
 * 模块: web
 * 包: io.agentscope.runtime
 *
 * 本地部署管理器，实现 {@link DeployManager} 接口。
 * 负责在本地启动 Spring Boot Servlet 服务器来部署 Agent Runner。
 * 支持多协议（A2A、ResponseAPI）端点注册、CORS 配置、自定义端点、中间件等功能。
 * 使用建造者模式构建，支持灵活的配置组合。
 */
package io.agentscope.runtime;

import io.agentscope.runtime.app.AgentApp;
import io.agentscope.runtime.autoconfigure.DeployProperties;
import io.agentscope.runtime.engine.DeployManager;
import io.agentscope.runtime.engine.Runner;
import io.agentscope.runtime.protocol.Protocol;
import io.agentscope.runtime.protocol.ProtocolConfig;
import jakarta.servlet.Filter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 本地部署管理器 —— 实现 {@link DeployManager} 接口。
 *
 * <p>角色：在本地启动 Spring Boot Servlet 服务器来部署 Agent Runner，
 * 注册所有必要的 Spring Bean（Runner、DeployProperties、协议控制器等），
 * 并配置 CORS、自定义端点和中间件。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>启动 Spring Boot Servlet 应用上下文</li>
 *   <li>将 Runner 实例注册为 Spring Bean</li>
 *   <li>按协议配置扫描对应的控制器包</li>
 *   <li>注册 CORS 配置、自定义端点路由和过滤器中间件</li>
 *   <li>管理应用上下文的生命周期（启动/关闭）</li>
 * </ul>
 *
 * <p>设计模式：建造者模式 —— 通过 {@link LocalDeployerManagerBuilder} 灵活构建实例。</p>
 */
public class LocalDeployManager implements DeployManager {
	private static final Logger logger = LoggerFactory.getLogger(LocalDeployManager.class);

	private ConfigurableApplicationContext applicationContext;

	private final String endpointName;
	private final String host;
	private final int port;
	private final List<Protocol> protocols;
	private final List<ProtocolConfig> protocolConfigs;
	private final Consumer<CorsRegistry> corsConfigurer;
	private final List<AgentApp.EndpointInfo> customEndpoints;
	private final List<FilterRegistrationBean<? extends Filter>> middlewares;


	private LocalDeployManager(LocalDeployerManagerBuilder builder) {
		this.endpointName = builder.endpointName;
		this.host = builder.host;
		this.port = builder.port;
		this.protocols = builder.protocols;
		this.protocolConfigs = builder.protocolConfigs;
		this.corsConfigurer = builder.corsConfigurer;
		this.customEndpoints = builder.customEndpoints;
		this.middlewares = builder.middlewares;
	}

	@Override
	public synchronized void deploy(Runner runner) {
		if (this.applicationContext != null && this.applicationContext.isActive()) {
			logger.info("Application context is already active, skipping deployment");
			return;
		}

		Map<String, Object> serverProps = new HashMap<>();
		if (this.port > 0) {
			serverProps.put("server.port", this.port);
		}
		if (this.host != null && !this.host.isBlank()) {
			serverProps.put("server.address", this.host);
		}

		logger.info("Starting streaming deployment for endpoint: {}", endpointName);

		this.applicationContext = new SpringApplicationBuilder()
				.sources(LocalDeployConfig.class)
				.web(WebApplicationType.SERVLET)
				.properties(serverProps)
				.initializers((GenericApplicationContext ctx) -> {
					// Register Runner instance as a bean
					ctx.registerBean(Runner.class, () -> runner);
					// Register DeployProperties instance as a bean
					ctx.registerBean(DeployProperties.class, () -> new DeployProperties(port, host, endpointName));
					// Scan additional packages based on protocols
					ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(ctx);
					scanner.scan("io.agentscope.runtime.lifecycle");
					Map<Protocol, ProtocolConfig> protocolConfigMap = null != protocolConfigs ? protocolConfigs.stream()
							.collect(HashMap::new, (map, config) -> map.put(config.type(), config), HashMap::putAll)
							: Map.of();
					for (Protocol protocol : protocols) {
						String packageName = "io.agentscope.runtime.protocol." + protocol.name().toLowerCase();
						scanner.scan(packageName);
						if (protocolConfigMap.containsKey(protocol)) {
							ProtocolConfig protocolConfig = protocolConfigMap.get(protocol);
							ctx.registerBean(protocolConfig.name(), ProtocolConfig.class, () -> protocolConfig);
						}
					}
					if (corsConfigurer != null) {
						ctx.registerBean(WebMvcConfigurer.class, () -> new WebMvcConfigurer() {
							@Override
							public void addCorsMappings(@NotNull CorsRegistry registry) {
								corsConfigurer.accept(registry);
							}
						});
					}
					if (customEndpoints != null && !customEndpoints.isEmpty()) {
						RouterFunction<ServerResponse> routerFunction = routerFunction();
						ctx.registerBean(routerFunction.getClass(), routerFunction);
					}
					if (middlewares != null && !middlewares.isEmpty()) {
						for (FilterRegistrationBean<? extends Filter> bean : middlewares) {
							ctx.registerBean(bean.getFilterName(), FilterRegistrationBean.class, () -> bean);
						}
					}
				})
				.run();
		logger.info("Streaming deployment completed for endpoint: {}", endpointName);
	}


	protected RouterFunction<ServerResponse> routerFunction() {
		RouterFunctions.Builder route = RouterFunctions.route();
		for (AgentApp.EndpointInfo customEndpoint : customEndpoints) {
			List<String> methods = customEndpoint.methods;
			if (methods == null || methods.isEmpty()) {
				continue;
			}
			if (methods.contains(HttpMethod.GET.name())) {
				route.GET(customEndpoint.path, request -> customEndpoint.handler.apply(request));
			}
			if (methods.contains(HttpMethod.HEAD.name())) {
				route.HEAD(customEndpoint.path, request -> customEndpoint.handler.apply(request));
			}
			if (methods.contains(HttpMethod.POST.name())) {
				route.POST(customEndpoint.path, request -> customEndpoint.handler.apply(request));
			}
			if (methods.contains(HttpMethod.PUT.name())) {
				route.PUT(customEndpoint.path, request -> customEndpoint.handler.apply(request));
			}
			if (methods.contains(HttpMethod.DELETE.name())) {
				route.DELETE(customEndpoint.path, request -> customEndpoint.handler.apply(request));
			}
			if (methods.contains(HttpMethod.PATCH.name())) {
				route.PATCH(customEndpoint.path, request -> customEndpoint.handler.apply(request));
			}
			if (methods.contains(HttpMethod.OPTIONS.name())) {
				route.OPTIONS(customEndpoint.path, request -> customEndpoint.handler.apply(request));
			}
		}
		return route.build();
	}

	@Override
	public void undeploy() {
		shutdown();
	}

	/**
	 * Shutdown the application and clean up resources
	 */
	public synchronized void shutdown() {
		if (this.applicationContext != null && this.applicationContext.isActive()) {
			logger.info("Shutting down LocalDeployManager...");
			this.applicationContext.close();
			this.applicationContext = null;
			logger.info("LocalDeployManager shutdown completed");
		}
	}

	/**
	 * Configuration class for local deployment of streaming services.
	 * This class enables component scanning for A2A controllers and other Spring components.
	 */
	@Configuration
	@EnableAutoConfiguration
	@ComponentScan(basePackages = {
			"io.agentscope.runtime.autoconfigure"
	})
	public static class LocalDeployConfig {
	}

	public static LocalDeployerManagerBuilder builder() {
		return new LocalDeployerManagerBuilder();
	}

	public static class LocalDeployerManagerBuilder {
		private String endpointName;
		private String host;
		private int port = 8080;
		private List<Protocol> protocols = List.of(Protocol.A2A, Protocol.ResponseAPI);
		private List<ProtocolConfig> protocolConfigs = List.of();
		private Consumer<CorsRegistry> corsConfigurer;
		private List<AgentApp.EndpointInfo> customEndpoints;
		private List<FilterRegistrationBean<? extends Filter>> middlewares;

		public LocalDeployerManagerBuilder endpointName(String endpointName) {
			this.endpointName = endpointName;
			return this;
		}

		public LocalDeployerManagerBuilder host(String host) {
			this.host = host;
			return this;
		}

		public LocalDeployerManagerBuilder port(int port) {
			this.port = port;
			return this;
		}

		public LocalDeployerManagerBuilder protocols(List<Protocol> protocols) {
			this.protocols = protocols;
			return this;
		}

		public LocalDeployerManagerBuilder protocolConfigs(List<ProtocolConfig> protocolConfigs) {
			this.protocolConfigs = protocolConfigs;
			return this;
		}

		public LocalDeployerManagerBuilder corsConfigurer(Consumer<CorsRegistry> corsConfigurer) {
			this.corsConfigurer = corsConfigurer;
			return this;
		}

		public LocalDeployerManagerBuilder customEndpoints(List<AgentApp.EndpointInfo> customEndpoints) {
			this.customEndpoints = customEndpoints;
			return this;
		}

		public LocalDeployerManagerBuilder middlewares(List<FilterRegistrationBean<? extends Filter>> middlewares) {
			this.middlewares = middlewares;
			return this;
		}

		public LocalDeployManager build() {
			return new LocalDeployManager(this);
		}
	}
}
