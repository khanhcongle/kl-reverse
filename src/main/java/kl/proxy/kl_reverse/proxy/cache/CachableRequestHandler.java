package kl.proxy.kl_reverse.proxy.cache;

import java.util.Date;

import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
import io.vertx.httpproxy.impl.CacheControl;
import io.vertx.httpproxy.impl.ParseUtils;
import io.vertx.httpproxy.spi.cache.Cache;

public class CachableRequestHandler extends Cachable {
		
	public CachableRequestHandler(Cache<String, Resource> cache) {
		super.cache = cache;
	}

	public Future<ProxyResponse> tryHandleProxyRequestFromCache(ProxyContext context) {

		ProxyRequest proxyRequest = context.request();

		HttpServerRequest response = proxyRequest.proxiedRequest();

		Resource resource;
		HttpMethod method = response.method();
		if (method == HttpMethod.GET || method == HttpMethod.HEAD) {
			String cacheKey = proxyRequest.absoluteURI();
			resource = cache.computeIfPresent(cacheKey, this::cacheValidateAndCompute);
			if (resource == null) {
				return null;
			}
		} else {
			return null;
		}

		// to this point means GET or HEAD, but cache is NOT present
		/**
		 * Handle Cache-Control header
		 */
		String cacheControlHeader = response.getHeader(HttpHeaders.CACHE_CONTROL);
		if (cacheControlHeader != null) {
			CacheControl cacheControl = new CacheControl().parse(cacheControlHeader);
			if (cacheControl.maxAge() >= 0) {
				long now = System.currentTimeMillis();
				long currentAge = now - resource.timestamp;
				if (currentAge > cacheControl.maxAge() * 1000) {
					String etag = resource.headers.get(HttpHeaders.ETAG);
					if (etag == null) {
						return null;
					}
					proxyRequest.headers().set(HttpHeaders.IF_NONE_MATCH, resource.etag);
					context.set(CACHED_RESOURCE, resource);
					return context.sendRequest();
				}
			}
		}

		/**
		 * handle If-Modified-Since header
		 */
		String ifModifiedSinceHeader = response.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
		if ((response.method() == HttpMethod.GET || response.method() == HttpMethod.HEAD)
				&& ifModifiedSinceHeader != null && resource.lastModified != null) {
			Date ifModifiedSince = ParseUtils.parseHeaderDate(ifModifiedSinceHeader);
			if (resource.lastModified.getTime() <= ifModifiedSince.getTime()) {
				response.response().setStatusCode(304).end();
				return Future.succeededFuture();
			}
		}
		proxyRequest.release();
		ProxyResponse proxyResponse = proxyRequest.response();
		resource.init(proxyResponse);
		return Future.succeededFuture(proxyResponse);
	}
	
	private Resource cacheValidateAndCompute(String key, Resource resource) {
		long now = System.currentTimeMillis();
		long lastMillis = resource.timestamp + resource.maxAge;
		boolean needGetnew = lastMillis < now;

		if (!needGetnew) {
			System.out.println("Cached: " + resource.toString());
		} else {
			System.out.println("Called: " + resource.toString());
		}

		return needGetnew ? null : resource;
	}
}
