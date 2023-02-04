package kl.proxy.kl_reverse.context;

import io.vertx.core.http.HttpMethod;

public class CacheIdentifier {
	
	HttpMethod method;
	String requestUri;
	
	public CacheIdentifier(HttpMethod method, String requestUri) {
		this.method = method;
		this.requestUri = requestUri;
	}

	@Override
	public String toString() {
		return "CacheIdentifier [method=" + method + ", requestUri=" + requestUri + "]";
	}

}
