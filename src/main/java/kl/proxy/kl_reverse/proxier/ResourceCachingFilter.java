package kl.proxy.kl_reverse.proxier;

import java.util.Date;
import java.util.function.BiFunction;

import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.httpproxy.Body;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyInterceptor;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
import io.vertx.httpproxy.impl.CacheControl;
import io.vertx.httpproxy.impl.ParseUtils;
import io.vertx.httpproxy.spi.cache.Cache;
import kl.proxy.kl_reverse.sampler.SamplerService;
import kl.proxy.kl_reverse.sampler.StopWatch;

public class ResourceCachingFilter implements ProxyInterceptor {

	private static final long MAX_AGE = 5000L;

	private static final String CACHED_RESOURCE = "cached_resource";

	private long maxAge = MAX_AGE;

	private static final BiFunction<String, Resource, Resource> CACHE_GET_AND_VALIDATE = (key, resource) -> {
		long now = System.currentTimeMillis();
		long lastMillis = resource.timestamp + resource.maxAge;
		boolean needGetnew = lastMillis < now;

		if (!needGetnew) {
			System.out.println("Cached: " + resource.toString());
		} else {
			System.out.println("Called: " + resource.toString());
		}

		return needGetnew ? null : resource;
	};

	private final Cache<String, Resource> cache;

	public ResourceCachingFilter(Cache<String, Resource> cache) {
		this.cache = cache;
	}

	@Override
	public Future<ProxyResponse> handleProxyRequest(ProxyContext context) {
		StopWatch.putStartTime(context.request().hashCode());
		Future<ProxyResponse> future = tryHandleProxyRequestFromCache(context);
		if (future != null) {
			return future;
		}
		return context.sendRequest();
	}

	@Override
	public Future<Void> handleProxyResponse(ProxyContext context) {
		SamplerService.logRequest(context.request(), context.response());
		return sendAndTryCacheProxyResponse(context);
	}

	private Future<Void> sendAndTryCacheProxyResponse(ProxyContext context) {

		ProxyResponse response = context.response();
		Resource cached = context.get(CACHED_RESOURCE, Resource.class);

		if (cached != null && response.getStatusCode() == 304) {
			// Warning: this relies on the fact that HttpServerRequest will not send a body
			// for HEAD
			response.release();
			cached.init(response);
			return context.sendResponse();
		}

		ProxyRequest request = response.request();
		boolean hasPublicCacheControl = true; // = response.publicCacheControl();
		boolean hasMaxAge = true;// = response.maxAge() > 0;
		boolean cachable = hasPublicCacheControl && hasMaxAge;
		if (!cachable) {
			return context.sendResponse();
		}

		if (request.getMethod() == HttpMethod.GET) {
			String absoluteUri = request.absoluteURI();
			Resource res = new Resource(absoluteUri, response.getStatusCode(), response.getStatusMessage(),
					response.headers(), System.currentTimeMillis(), maxAge, request.getMethod());

			Body body = response.getBody();
			response.setBody(Body.body(new BufferingReadStream(body.stream(), res.content), body.length()));
			Future<Void> fut = context.sendResponse();
			fut.onSuccess(v -> {
				cache.put(absoluteUri, res);
			});
			return fut;
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

	private Future<ProxyResponse> tryHandleProxyRequestFromCache(ProxyContext context) {

		ProxyRequest proxyRequest = context.request();

		HttpServerRequest response = proxyRequest.proxiedRequest();

		Resource resource;
		HttpMethod method = response.method();
		if (method == HttpMethod.GET || method == HttpMethod.HEAD) {
			String cacheKey = proxyRequest.absoluteURI();
			resource = cache.computeIfPresent(cacheKey, CACHE_GET_AND_VALIDATE);
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
}
