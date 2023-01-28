package kl.proxy.kl_reverse.proxy;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.httpproxy.Body;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;

public interface FilterableProxyHandler {

	default ProxyRequest filterBody(ProxyRequest request) {
		return request.setBody(filterBody(request.getBody()));
	}

	default ProxyResponse filterBody(ProxyResponse response) {
		return response.setBody(filterBody(response.getBody()));
	}

	default Body filterBody(Body body) {
		ReadStream<Buffer> stream = body.stream();
		return Body.body(streamFiltering(stream), body.length());
	}

	default ReadStream<Buffer> streamFiltering(ReadStream<Buffer> stream) {
		return new FilterBase(stream, buff -> {
			System.out.println("Response Body: " + buff.toString());
		});
	}
}