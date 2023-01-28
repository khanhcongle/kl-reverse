package kl.proxy.kl_reverse.proxy;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyInterceptor;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
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

	public void filter(ProxyRequest request, ProxyResponse response) {
		filterBody(response);
		log(request, response);		
	}

	public void log(ProxyRequest request, ProxyResponse response) {
		String requestUri = String.join(" ", request.getMethod().toString(), request.getURI());
		
		long start = StopWatch.removeStartTime(request.hashCode());
		long responseTimeMilis = System.currentTimeMillis() - start;
		
		JsonObject record = JsonObject.of(
				"start", start,
				"time", responseTimeMilis,
				"status", response.getStatusCode(),
				"request", requestUri,
				"hash", request.hashCode()
				);
		System.out.println(record.toString());
	}

}
