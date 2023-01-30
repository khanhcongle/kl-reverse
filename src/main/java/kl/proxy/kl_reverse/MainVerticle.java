package kl.proxy.kl_reverse;

import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.httpproxy.HttpProxy;
import io.vertx.httpproxy.ProxyInterceptor;
import kl.proxy.kl_reverse.context.RequestsLogger;
import kl.proxy.kl_reverse.proxy.BodyInterceptor;

public class MainVerticle extends AbstractVerticle {

	private static final int PROXY_PORT = 8080;
	private static final String ORIGIN_HOST_DOMAIN = "localhost";
	private static final int ORIGIN_HOST_PORT = 8082;
	private static final int RESOURCE_PORT = 8880;
	
	private Promise<Void> startPromise;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		this.startPromise = startPromise;
		
		getConfig().onSuccess(jsonConfig -> {
			startResourceListener(jsonConfig);
			startReverseProxyServer(jsonConfig);
			this.startPromise.complete();
		});
	}
	
	private Future<JsonObject> getConfig() {
		ConfigStoreOptions systemConfig = new ConfigStoreOptions().setType("sys").setConfig(new JsonObject().put("hierarchical", true));
		ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions().addStore(systemConfig);
		ConfigRetriever retriever = ConfigRetriever.create(vertx, configRetrieverOptions);
		return retriever.getConfig();
	}

	private void startResourceListener(JsonObject jsonConfig) {
		/*
		 * REF: https://vertx.io/docs/vertx-web/java/
		 */
		Router router = Router.router(vertx);
		
		router.route()
			.method(HttpMethod.GET)
			.method(HttpMethod.POST)
			.method(HttpMethod.PUT)
			.handler(ctx -> {
			    HttpServerResponse response = ctx.response();
			    // enable chunked responses because we will be adding data as
			    // we execute over other handlers. This is only required once and
			    // only if several handlers do output.
			    response.setChunked(true);
			    
			    response.putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
			    
			    response.write(RequestsLogger.get().encodePrettily());
			    
			    // Now end the response
			    ctx.response().end();
			});
		
		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(router)
			.listen(RESOURCE_PORT, asyncResult -> this.serverStartListener("Resources", RESOURCE_PORT, asyncResult, startPromise));

	}

	private void startReverseProxyServer(JsonObject jsonConfig) {
		String bindings = jsonConfig.getString("bind");		
		//
		startProxyServers(bindings, this::startProxyServer);
	}

	private void startProxyServer(Integer proxyPort, Integer originPort) {
		ProxyInterceptor bodyInterceptor = new BodyInterceptor();
		
		HttpProxy httpProxyClient = HttpProxy.reverseProxy(vertx.createHttpClient())
				.origin(originPort, ORIGIN_HOST_DOMAIN)
				.addInterceptor(bodyInterceptor);

		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(httpProxyClient)
				.listen(proxyPort, asyncResult -> this.serverStartListener("Reverse Proxy", proxyPort, asyncResult, startPromise));
	}

	private static void startProxyServers(String bindingAsString, BiConsumer<Integer, Integer> starter) {
		String[] bindings = StringUtils.split(bindingAsString, ",");
		if(bindings == null || bindings.length == 0) {
			starter.accept(PROXY_PORT, ORIGIN_HOST_PORT);
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
			starter.accept(proxyPort, originPort);
		}
	}
	
	private void serverStartListener(
			String serverName,
			int port,
			AsyncResult<HttpServer> result,
			Promise<Void> startPromise) {
		
		if (result.succeeded()) {
			System.out.println(serverName + " started on port: " + port);
		} else {
			startPromise.fail(result.cause());
		}
	}
}
