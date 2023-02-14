package kl.proxy.kl_reverse.history;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

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

public class HistoryServerStarter extends ServerStarter {

	private static final int RESOURCE_PORT = 8880;

	public HistoryServerStarter(Promise<Void> startPromise) {
		super(startPromise);
	}

	@Override
	public void start(JsonObject jsonConfig) {
		/*
		 * REF: https://vertx.io/docs/vertx-web/java/
		 */
		Router router = Router.router(vertx);

		router.route("/histories").method(HttpMethod.GET).handler(ctx -> {
			HttpServerRequest request = ctx.request();

			LocalDateTime from = paramToLocalDateTime(request.getParam("from"));
			LocalDateTime to = paramToLocalDateTime(request.getParam("to"));			
			
			String payload = HistoryService.get(from, to, request.getParam("exclude"));

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
	
		router.route("/histories").method(HttpMethod.DELETE).handler(ctx -> {
			HistoryService.empty();
			ctx.response().setStatusCode(200).end();
		});

		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(router).listen(RESOURCE_PORT,
				asyncResult -> this.serverStartListener("Resources", RESOURCE_PORT, asyncResult, startPromise));

	}
	static private LocalDateTime paramToLocalDateTime(String fromParam) {
		return Optional.ofNullable(fromParam).map(string -> new Date(Long.valueOf(string))).map(Date::toInstant)
				.map(instant -> LocalDateTime.ofInstant(instant, ZoneOffset.UTC)).orElse(null);
	}
}