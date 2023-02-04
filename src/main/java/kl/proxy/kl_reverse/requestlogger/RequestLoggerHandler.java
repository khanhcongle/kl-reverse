package kl.proxy.kl_reverse.requestlogger;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import kl.proxy.kl_reverse.context.RequestsLogger;

public class RequestLoggerHandler {
	public static void handleGet(RoutingContext ctx) {
		HttpServerRequest request = ctx.request();
		LocalDateTime from = paramToLocalDateTime(request.getParam("from"));
		LocalDateTime to = paramToLocalDateTime(request.getParam("to"));
		
	    String responseBody = RequestsLogger.get(from, to).encodePrettily();

	    HttpServerResponse response = ctx.response();
	    // enable chunked responses because we will be adding data as
	    // we execute over other handlers. This is only required once and
	    // only if several handlers do output.
	    response.setChunked(true);
		response.write(responseBody);
	    
	    // Now end the response
	    ctx.response().end();
	}

	static private LocalDateTime paramToLocalDateTime(String fromParam) {
		return Optional.ofNullable(fromParam)
				.map(string -> new Date(Long.valueOf(string)))
				.map(Date::toInstant)
				.map(instant -> LocalDateTime.ofInstant(instant, ZoneOffset.UTC))
				.orElse(null);
	}
}
