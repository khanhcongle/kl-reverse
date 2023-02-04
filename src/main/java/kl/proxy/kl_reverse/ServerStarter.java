package kl.proxy.kl_reverse;

import org.apache.commons.lang3.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.httpproxy.HttpProxy;
import kl.proxy.kl_reverse.context.ContextAccessable;
import kl.proxy.kl_reverse.proxy.CachedRequestProxyInterceptor;
import kl.proxy.kl_reverse.proxy.RequestLoggerProxyInterceptor;
import kl.proxy.kl_reverse.requestlogger.RequestLoggerHandler;

public abstract class ServerStarter {
	private static final String ORIGIN_HOST_DOMAIN = "localhost";
	
	Vertx vertx;
	Promise<Void> startPromise;
	
	public ServerStarter(Promise<Void> startPromise) {
		this.vertx = ContextAccessable.getVertx();
		this.startPromise = startPromise;
	}
	
	public abstract void start(JsonObject jsonConfig);
	
	protected String getHostDomain() {
		return ORIGIN_HOST_DOMAIN;
	}
	
	protected void serverStartListener(String serverName, int port, AsyncResult<HttpServer> result, Promise<Void> startPromise) {
		if (result.succeeded()) {
			System.out.println(serverName + " started on port: " + port);
		} else {
			startPromise.fail(result.cause());
		}
	}
	
	public static class ProxyServerStarter extends ServerStarter {
		
		private static final int PROXY_PORT = 8080;
		private static final int ORIGIN_HOST_PORT = 8082;

		public ProxyServerStarter(Promise<Void> startPromise) {
			super(startPromise);
		}
		
		public void start(JsonObject jsonConfig) {
			String[] bindings = StringUtils.split(jsonConfig.getString("bind"), ",");
			startMultipleProxyServers(bindings);
		}

		private void startMultipleProxyServers(String[] bindings) {
			if(bindings == null || bindings.length == 0) {
				startProxyServer(PROXY_PORT, ORIGIN_HOST_PORT);
				return;
			}
			for (String binding : bindings) {
				String[] ports = StringUtils.split(binding, ":");
				if(ports == null || ports.length != 2) {
					throw new IllegalArgumentException("Format: -Dbind=<proxyPort>:<originPort>,...");
				}
				Integer proxyPort = Integer.parseInt(ports[0]);
				Integer originPort = Integer.parseInt(ports[1]);
				System.out.println("proxyPort: " + proxyPort + "; originPort: " + originPort);
				startProxyServer(proxyPort, originPort);
			}
		}
		
		private void startProxyServer(Integer proxyPort, Integer originPort) {
			HttpProxy httpProxyClient = HttpProxy.reverseProxy(vertx.createHttpClient())
					.origin(originPort, getHostDomain())
					.addInterceptor(new RequestLoggerProxyInterceptor())
					.addInterceptor(new CachedRequestProxyInterceptor())
					;

			HttpServer httpServer = vertx.createHttpServer();
			httpServer.requestHandler(httpProxyClient)
					.listen(proxyPort, asyncResult -> this.serverStartListener("Reverse Proxy", proxyPort, asyncResult, startPromise));
		}
	}
	
	public static class ResourceServerStarter extends ServerStarter {
		private static final int RESOURCE_PORT = 8880;

		public ResourceServerStarter(Promise<Void> startPromise) {
			super(startPromise);
		}

		public void start(JsonObject jsonConfig) {
			/*
			 * REF: https://vertx.io/docs/vertx-web/java/
			 */
			Router router = Router.router(vertx);

			router.route()
				.method(HttpMethod.GET)
				.handler(RequestLoggerHandler::handleGet);
			
			HttpServer httpServer = vertx.createHttpServer();
			httpServer.requestHandler(router)
				.listen(RESOURCE_PORT, asyncResult -> this.serverStartListener("Resources", RESOURCE_PORT, asyncResult, startPromise));

		}
	}
}
