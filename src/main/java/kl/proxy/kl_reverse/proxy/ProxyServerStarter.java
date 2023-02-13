package kl.proxy.kl_reverse.proxy;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.httpproxy.HttpProxy;
import io.vertx.httpproxy.cache.CacheOptions;
import kl.proxy.kl_reverse.ServerStarter;

public class ProxyServerStarter extends ServerStarter {

	private static final int PROXY_PORT = 8080;
	private static final int ORIGIN_HOST_PORT = 8082;
	private static final int MAX_HISTORY = 50;
	
	private Integer maxHistory;

	public ProxyServerStarter(Promise<Void> startPromise) {
		super(startPromise);
	}

	@Override
	public void start(JsonObject jsonConfig) {
		String[] bindings = StringUtils.split(jsonConfig.getString("bind"), ",");
		this.maxHistory = Optional.ofNullable(jsonConfig.getInteger("maxHistory")).orElse(MAX_HISTORY);
		
		startMultipleProxyServers(bindings);
	}

	private void startMultipleProxyServers(String[] bindings) {
		if (bindings == null || bindings.length == 0) {
			startProxyServer(PROXY_PORT, ORIGIN_HOST_PORT);
			return;
		}
		for (String binding : bindings) {
			String[] ports = StringUtils.split(binding, ":");
			if (ports == null || ports.length != 2) {
				throw new IllegalArgumentException("Format: -Dbind=<proxyPort>:<originPort>,...");
			}
			Integer proxyPort = Integer.parseInt(ports[0]);
			Integer originPort = Integer.parseInt(ports[1]);
			System.out.println("proxyPort: " + proxyPort + "; originPort: " + originPort);
			startProxyServer(proxyPort, originPort);
		}
	}

	private void startProxyServer(Integer proxyPort, Integer originPort) {
		CacheOptions cacheOption = new CacheOptions().setMaxSize(maxHistory);
		
		HttpProxy httpProxyClient = HttpProxy.reverseProxy(vertx.createHttpClient())
				.origin(originPort, getHostDomain())
				.addInterceptor(new ResourceCachingFilter(cacheOption));

		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(httpProxyClient).listen(proxyPort,
				asyncResult -> this.serverStartListener("Reverse Proxy", proxyPort, asyncResult, startPromise));
	}
}
