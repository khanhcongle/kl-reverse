package kl.proxy.kl_reverse.proxy;

import java.net.MalformedURLException;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyInterceptor;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
import kl.proxy.kl_reverse.context.StopWatch;
import kl.proxy.kl_reverse.requestlogger.RequestLoggerHandler;

public class RequestLoggerProxyInterceptor implements ProxyInterceptor, FilterableProxyHandler {
	
	@Override
	public Future<ProxyResponse> handleProxyRequest(ProxyContext context) {		
		ProxyRequest request = context.request();
		
		StopWatch.putStartTime(request.hashCode());
		filterBody(request);
		
		return context.sendRequest();
	}

	@Override
	public Future<Void> handleProxyResponse(ProxyContext context) {
		filterResponse(context.request(), context.response());
		
		return context.sendResponse();
	}

	private void filterResponse(ProxyRequest request, ProxyResponse response) {
		filterBody(response, (Buffer buffer) -> {
			try {
				RequestLoggerHandler.logRequest(request, response, buffer.copy());
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
		});
	}
}
