package kl.proxy.kl_reverse.proxy;

import io.vertx.core.Future;
import io.vertx.httpproxy.Body;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyInterceptor;
import io.vertx.httpproxy.ProxyResponse;
import io.vertx.httpproxy.cache.CacheOptions;
import io.vertx.httpproxy.spi.cache.Cache;
import kl.proxy.kl_reverse.context.StopWatch;
import kl.proxy.kl_reverse.history.HistoryService;
import kl.proxy.kl_reverse.proxy.cache.CachableRequestHandler;
import kl.proxy.kl_reverse.proxy.cache.CachableResponseHandler;
import kl.proxy.kl_reverse.proxy.cache.Resource;

class ResourceCachingFilter implements ProxyInterceptor {

	CachableRequestHandler requestHandler;
	CachableResponseHandler responseHandler;
	int maxHistory;
	
	public ResourceCachingFilter(CacheOptions cacheOption) {
		maxHistory = cacheOption.getMaxSize();
		
		Cache<String, Resource> cache = cacheOption.newCache();
		requestHandler = new CachableRequestHandler(cache);
		responseHandler = CachableResponseHandler.builder()
				.cache(cache)
				.alwaysCache(false)
				.build();
	}

	@Override
	public Future<ProxyResponse> handleProxyRequest(ProxyContext context) {
		recordStart(context);
		
		Future<ProxyResponse> future = requestHandler.tryHandleProxyRequestFromCache(context);
		
		if (future != null) {
			return future;
		}
		return context.sendRequest().onSuccess(event -> {
			System.out.println("Request SENT");
		});
	}

	private void recordStart(ProxyContext context) {
		Resource res = new Resource();
		Body requestBody = context.request().getBody();
		if(requestBody != null) {
			context.request().setBody(Body.body(new BufferingReadStream(requestBody.stream(), res.getRequestPayload()), requestBody.length()));
		}
		StopWatch.putRequest(context.request().hashCode(), res);
	}

	@Override
	public Future<Void> handleProxyResponse(ProxyContext context) {
		return responseHandler.sendAndTryToCacheProxyResponse(context)
				.onSuccess(event -> {
					HistoryService.logRequest(context.request(), context.response(), maxHistory);			
				});
	}

}
