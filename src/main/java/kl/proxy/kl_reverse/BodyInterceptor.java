package kl.proxy.kl_reverse;

import io.vertx.core.Future;
import io.vertx.httpproxy.Body;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyInterceptor;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;

public class BodyInterceptor implements ProxyInterceptor {
	FilterBase filter;
	
	private BodyInterceptor(FilterBase filter) {
		this.filter = filter;
	}
	
	public static ProxyInterceptor newInterceptor() {
		FilterBase filter = new FilterBase();		
		return new BodyInterceptor(filter);
	}
	
	@Override
	public Future<ProxyResponse> handleProxyRequest(ProxyContext context) {
		ProxyRequest request = context.request();
//		logRequest(request, "[req]");
		filterBody(request);
		
		return context.sendRequest();
	}

	@Override
	public Future<Void> handleProxyResponse(ProxyContext context) {
		ProxyRequest request = context.request();
		ProxyResponse response = context.response();
		logRequest(request, "[" + response.getStatusCode() + "]");
		filterBody(response);
		
		return context.sendResponse();
	}

	private ProxyRequest filterBody(ProxyRequest request) {
		return request.setBody(filterBody(request.getBody()));
	}

	private ProxyResponse filterBody(ProxyResponse response) {
		return response.setBody(filterBody(response.getBody()));
	}

	private Body filterBody(Body body) {
		return Body.body(filter.init(body.stream()), body.length());
	}

	private void logRequest(ProxyRequest request, String postfixText) {
		System.out.println(request.hashCode() + ": "
				+ String.join(" ", postfixText, request.getMethod().toString(), request.getURI()));
	}

}
