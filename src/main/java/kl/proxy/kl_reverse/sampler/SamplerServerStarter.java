package kl.proxy.kl_reverse.sampler;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import kl.proxy.kl_reverse.ServerStarter;

public class SamplerServerStarter extends ServerStarter {

	private static final int RESOURCE_PORT = 8880;

	public SamplerServerStarter(Promise<Void> startPromise) {
		super(startPromise);
	}

	public void start(JsonObject jsonConfig) {
		/*
		 * REF: https://vertx.io/docs/vertx-web/java/
		 */
		Router router = Router.router(vertx);

		router.route().method(HttpMethod.GET).handler(ctx -> {
			HttpServerRequest request = ctx.request();

			String payload = SamplerService.get(request.getParam("from"), request.getParam("to"));

			HttpServerResponse response = ctx.response();
			// enable chunked responses because we will be adding data as
			// we execute over other handlers. This is only required once and
			// only if several handlers do output.
			response.setChunked(true);
			response.putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
			response.write(payload);

			// Now end the response
			ctx.response().end();
		});

		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(router).listen(RESOURCE_PORT,
				asyncResult -> this.serverStartListener("Resources", RESOURCE_PORT, asyncResult, startPromise));

	}
}