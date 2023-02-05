package kl.proxy.kl_reverse.proxy;

import java.util.Date;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.httpproxy.Body;
import io.vertx.httpproxy.ProxyResponse;
import io.vertx.httpproxy.impl.ParseUtils;

class Resource {

	final String absoluteUri;
	final int statusCode;
	final String statusMessage;
	final MultiMap headers;
	final long timestamp;
	final long maxAge;
	final Date lastModified;
	final String etag;
	final Buffer content = Buffer.buffer();
	HttpMethod httpMethod;

	Resource(String absoluteUri, int statusCode, String statusMessage, MultiMap headers, long timestamp, long maxAge, HttpMethod httpMethod) {
		String lastModifiedHeader = headers.get(HttpHeaders.LAST_MODIFIED);
		this.absoluteUri = absoluteUri;
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
		this.headers = headers;
		this.timestamp = timestamp;
		this.maxAge = maxAge;
		this.lastModified = lastModifiedHeader != null ? ParseUtils.parseHeaderDate(lastModifiedHeader) : null;
		this.etag = headers.get(HttpHeaders.ETAG);
		this.httpMethod = httpMethod;
	}

	void init(ProxyResponse proxyResponse) {
		proxyResponse.setStatusCode(200);
		proxyResponse.setStatusMessage(statusMessage);
		proxyResponse.headers().addAll(headers);
		proxyResponse.setBody(Body.body(content));
	}

	@Override
	public String toString() {
		return "Resource [" + httpMethod + " " + absoluteUri + ", status=" + statusCode + "]";
	}
}