package kl.proxy.kl_reverse.proxy;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.httpproxy.Body;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyInterceptor;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
import kl.proxy.kl_reverse.context.CacheIdentifier;
import kl.proxy.kl_reverse.requestlogger.RequestLoggerHandler;

public class CachedRequestProxyInterceptor implements ProxyInterceptor {
	@Override
	public Future<ProxyResponse> handleProxyRequest(ProxyContext context) {		
		ProxyRequest request = context.request();
		Optional<JsonObject> optionalMatch = RequestLoggerHandler.getFirstMatch(json -> cidChecker(request, json));
		if(optionalMatch.isPresent()) {
			
		    // Release the underlying resources
			request.release();
			
			JsonObject jsonObject = optionalMatch.get();
			ProxyResponse response = request.response()
				      .setStatusCode(200)
				      .setBody(Body.body(Buffer.buffer(jsonObject.getString("resBody"))));
			
			for (String header : jsonObject.getString("resHeaders").split("\\n")) {
				String[] keyValue = header.split("=");
				response.putHeader(keyValue[0], keyValue[1]);
			}
			
			return Future.succeededFuture(response);
		}
		return context.sendRequest();
	}

	private boolean cidChecker(ProxyRequest request, JsonObject json) {
		CacheIdentifier cacheIdentifier = new CacheIdentifier(request.getMethod(), request.getURI());
		
		return StringUtils.equals(cacheIdentifier.toString(), json.getString("cid"));
	}
}
