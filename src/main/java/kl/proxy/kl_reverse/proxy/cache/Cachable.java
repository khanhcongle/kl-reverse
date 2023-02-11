package kl.proxy.kl_reverse.proxy.cache;

import io.vertx.httpproxy.spi.cache.Cache;

class Cachable {
	protected static final String CACHED_RESOURCE = "cached_resource";
	protected long defaultMaxAge =  5000L; // milliseconds
	
	protected Cache<String, Resource> cache;
}
