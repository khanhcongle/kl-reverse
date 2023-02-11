package kl.proxy.kl_reverse.proxy.cache;

import java.security.InvalidParameterException;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.httpproxy.Body;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
import io.vertx.httpproxy.spi.cache.Cache;
import kl.proxy.kl_reverse.proxy.BufferingReadStream;

public class CachableResponseHandler extends Cachable {
	
	boolean isAlwaysCache = false;
	
	public CachableResponseHandler() {
		// TODO Auto-generated constructor stub
	}

	public static CachableResponseHandlerBuilder builder() {
		return new CachableResponseHandlerBuilder();
	}
	
	public Future<Void> sendAndTryToCacheProxyResponse(ProxyContext context) {

		ProxyResponse response = context.response();
		Resource cached = context.get(CACHED_RESOURCE, Resource.class);

		if (cached != null && response.getStatusCode() == 304) {
			// Warning: this relies on the fact that HttpServerRequest will not send a body
			// for HEAD
			response.release();
			cached.init(response);
			return context.sendResponse();
		}

		boolean hasPublicCacheControl = isAlwaysCache ? true : response.publicCacheControl();
		long maxAge2 = isAlwaysCache ? defaultMaxAge : response.maxAge();
		boolean hasMaxAge = isAlwaysCache ? true : response.maxAge() > 0;
		boolean cachable = hasPublicCacheControl && hasMaxAge;
		
		if (!cachable) {
			return context.sendResponse();
		}
		
		ProxyRequest request = response.request();

		if (request.getMethod() == HttpMethod.GET) {
			String absoluteUri = request.absoluteURI();
			Resource res = new Resource(absoluteUri, response.getStatusCode(), response.getStatusMessage(),
					response.headers(), System.currentTimeMillis(), maxAge2, request.getMethod());
			
			Body responseBody = response.getBody();
			response.setBody(Body.body(new BufferingReadStream(responseBody.stream(), res.responsePayload), responseBody.length()));
			
			Future<Void> fut = context.sendResponse(); 
			return fut.onSuccess(v -> {
						cache.put(absoluteUri, res);
					});
		}

		if (request.getMethod() == HttpMethod.HEAD) {
			Resource resource = cache.get(request.absoluteURI());

			if (resource != null && !revalidateResource(response, resource)) {
				// Invalidate cache
				cache.remove(request.absoluteURI());
			}
		}

		return context.sendResponse();
	}

	private static boolean revalidateResource(ProxyResponse response, Resource resource) {
		if (resource.etag != null && response.etag() != null) {
			return resource.etag.equals(response.etag());
		}
		return true;
	}
	
	public static class CachableResponseHandlerBuilder {
		
		CachableResponseHandler handler;
		
		public CachableResponseHandlerBuilder() {
			handler = new CachableResponseHandler();
		}
		
		public CachableResponseHandlerBuilder cache(Cache<String, Resource> cache) {
			this.handler.cache = cache;
			return this;
		}
		
		public CachableResponseHandlerBuilder cacheAge(long maxAge) {
			this.handler.defaultMaxAge = maxAge;
			this.alwaysCache(true);
			return this;
		}
		
		public CachableResponseHandlerBuilder alwaysCache(boolean isOverriden) {
			this.handler.isAlwaysCache = isOverriden;
			return this;
		}
		
		public CachableResponseHandler build() {
			if(this.handler.cache == null) {
				throw new InvalidParameterException("cache cannot be null");
			}
			return handler;
		}
	}
}
