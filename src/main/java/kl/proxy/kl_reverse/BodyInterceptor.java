package kl.proxy.kl_reverse;

import io.vertx.core.Future;
import io.vertx.httpproxy.Body;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyInterceptor;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;

public class BodyInterceptor {

	public static ProxyInterceptor newInterceptor() {
		Filter filter = new Filter();
		filter.handler(buffer -> System.out.println(buffer.toString()));

		return new ProxyInterceptor() {
			@Override
			public Future<ProxyResponse> handleProxyRequest(ProxyContext context) {
				ProxyRequest request = context.request();
				Body body = request.getBody();
				Body filteredBody = Body.body(filter.init(body.stream()), body.length());
				request.setBody(filteredBody);

				logRequest(request, "[req]");
				return context.sendRequest();
			}

			@Override
			public Future<Void> handleProxyResponse(ProxyContext context) {
				ProxyRequest request = context.request();
				ProxyResponse proxyResponse = context.response();

				logRequest(request, "[" + proxyResponse.getStatusCode() + "]");
				Body body = proxyResponse.getBody();
				Body filteredBody = Body.body(filter.init(body.stream()), body.length());

				proxyResponse.setBody(filteredBody);
				return context.sendResponse();
			}

			private void logRequest(ProxyRequest request, String postfixText) {
				System.out.println(request.hashCode() + ": "
						+ String.join(" ", postfixText, request.getMethod().toString(), request.absoluteURI()));
			}
		};
	}

}
