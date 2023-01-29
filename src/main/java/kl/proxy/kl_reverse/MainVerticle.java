package kl.proxy.kl_reverse;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
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

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		startResourceListener(startPromise);
		startReverseProxyServer(startPromise);
		startPromise.complete();
	}
	
	private void startResourceListener(Promise<Void> startPromise) {
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

	private void startReverseProxyServer(Promise<Void> startPromise) {
		ProxyInterceptor bodyInterceptor = new BodyInterceptor();
		
		HttpProxy httpProxyClient = HttpProxy.reverseProxy(vertx.createHttpClient())
				.origin(ORIGIN_HOST_PORT, ORIGIN_HOST_DOMAIN)
				.addInterceptor(bodyInterceptor);

		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(httpProxyClient)
				.listen(PROXY_PORT, asyncResult -> this.serverStartListener("Reverse Proxy", PROXY_PORT, asyncResult, startPromise));
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
