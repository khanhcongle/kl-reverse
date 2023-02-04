package kl.proxy.kl_reverse.proxy;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.httpproxy.Body;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;

public interface FilterableProxyHandler {

	default ProxyRequest filterBody(ProxyRequest request) {
		return request.setBody(filterBody(request.getBody(), null));
	}

	default ProxyResponse filterBody(ProxyResponse response, Handler<Buffer> bufferHandler) {
		return response.setBody(filterBody(response.getBody(), bufferHandler));
	}

	default Body filterBody(Body body, Handler<Buffer> bufferHandler) {
		ReadStream<Buffer> stream = body.stream();
		return Body.body(new FilterBase(stream, bufferHandler), body.length());
	}
}