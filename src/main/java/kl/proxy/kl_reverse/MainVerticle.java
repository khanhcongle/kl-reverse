package kl.proxy.kl_reverse;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.httpproxy.HttpProxy;
import io.vertx.httpproxy.ProxyInterceptor;
import kl.proxy.kl_reverse.proxy.BodyInterceptor;

public class MainVerticle extends AbstractVerticle {

	private static final int PROXY_PORT = 8080;
	private static final String ORIGIN_HOST_DOMAIN = "localhost";
	private static final int ORIGIN_HOST_PORT = 8082;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {

		ProxyInterceptor bodyInterceptor = new BodyInterceptor();
		
		HttpProxy httpProxyClient = HttpProxy.reverseProxy(vertx.createHttpClient())
				.origin(ORIGIN_HOST_PORT, ORIGIN_HOST_DOMAIN)
				.addInterceptor(bodyInterceptor);

		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(httpProxyClient)
				.listen(PROXY_PORT, asyncResult -> this.serverStartListener(asyncResult, startPromise));
	}
	
	private void serverStartListener(AsyncResult<HttpServer> result, Promise<Void> startPromise) {
		if (result.succeeded()) {
			startPromise.complete();
			System.out.println("HTTP server started on port: " + PROXY_PORT);
		} else {
			startPromise.fail(result.cause());
		}
	}
}
