package kl.proxy.kl_reverse.proxy.cache;

import java.util.Date;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.httpproxy.Body;
import io.vertx.httpproxy.ProxyResponse;
import io.vertx.httpproxy.impl.ParseUtils;

public class Resource {

	final String absoluteUri;
	final int statusCode;
	final String statusMessage;
	final MultiMap headers;
	final long timestamp;
	final long maxAge;
	final Date lastModified;
	final String etag;
	private final Buffer requestPayload = Buffer.buffer();
	final Buffer responsePayload = Buffer.buffer();
	HttpMethod httpMethod;

	public Resource(String absoluteUri, int statusCode, String statusMessage, MultiMap headers, long timestamp, long maxAge, HttpMethod httpMethod) {
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

	public Resource() {
		this.absoluteUri = "";
		this.statusCode = 0;
		this.statusMessage = "";
		this.headers = null;
		this.timestamp = 0;
		// TODO Auto-generated constructor stub
		this.maxAge = 0;
		this.lastModified = new Date();
		this.etag = "";
	}

	public void init(ProxyResponse proxyResponse) {
		proxyResponse.setStatusCode(200);
		proxyResponse.setStatusMessage(statusMessage);
		proxyResponse.headers().addAll(headers);
		proxyResponse.setBody(Body.body(responsePayload));
	}
	
	public Buffer getRequestPayload() {
		return requestPayload;
	}
	
	public Buffer getResponsePayload() {
		return responsePayload;
	}

	@Override
	public String toString() {
		return "Resource [" + httpMethod + " " + absoluteUri + ", status=" + statusCode + "]";
	}
}