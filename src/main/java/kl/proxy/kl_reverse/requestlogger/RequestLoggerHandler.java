package kl.proxy.kl_reverse.requestlogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
import kl.proxy.kl_reverse.context.CacheIdentifier;
import kl.proxy.kl_reverse.context.RequestsLogger;
import kl.proxy.kl_reverse.context.StopWatch;

public class RequestLoggerHandler extends RequestsLogger {
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
	
	public static void logRequest(ProxyRequest request, ProxyResponse response, Buffer bodyBuffer) throws MalformedURLException {
		String requestUri = String.join(" ", request.getMethod().toString(), request.getURI());
		
		long currentTimeMillis = System.currentTimeMillis();
		long start = StopWatch.removeStartTime(request.hashCode());
		long responseTimeMilis = currentTimeMillis - start;
		
		JsonObject record = JsonObject.of(
				"request", requestUri,
				"status", response.getStatusCode(),
				"start", start,
				"time", responseTimeMilis,
				"path", new URL(request.absoluteURI()).getPath(),
				"cid", new CacheIdentifier(request.getMethod(), request.getURI()).toString(),
				"resBody", bodyBuffer.toString(),
				"resHeaders", response.headers().toString()
				);
		String prettJson = record.encodePrettily();
		System.out.println(prettJson);
		RequestsLogger.add(prettJson);
	}
}
