package kl.proxy.kl_reverse.proxy;

import java.net.MalformedURLException;
import java.net.URL;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyInterceptor;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
import kl.proxy.kl_reverse.context.RequestsLogger;
import kl.proxy.kl_reverse.context.StopWatch;

public class BodyInterceptor implements ProxyInterceptor, FilterableProxyHandler {
	
	@Override
	public Future<ProxyResponse> handleProxyRequest(ProxyContext context) {		
		ProxyRequest request = context.request();
		StopWatch.putStartTime(request.hashCode());
		filterBody(request);
		
		return context.sendRequest();
	}

	@Override
	public Future<Void> handleProxyResponse(ProxyContext context) {
		ProxyRequest request = context.request();
		ProxyResponse response = context.response();
		filter(request, response);
		
		return context.sendResponse();
	}

	private void filter(ProxyRequest request, ProxyResponse response) {
		filterBody(response);
		try {
			log(request, response);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void log(ProxyRequest request, ProxyResponse response) throws MalformedURLException {
		String requestUri = String.join(" ", request.getMethod().toString(), request.getURI());
		
		long start = StopWatch.removeStartTime(request.hashCode());
		long responseTimeMilis = System.currentTimeMillis() - start;
		
		JsonObject record = JsonObject.of(
				"request", requestUri,
				"status", response.getStatusCode(),
				"start", start,
				"time", responseTimeMilis,
				"path", new URL(request.absoluteURI()).getPath()
				);
		String prettJson = record.encodePrettily();
		System.out.println(prettJson);
		RequestsLogger.add(prettJson);
	}
}
