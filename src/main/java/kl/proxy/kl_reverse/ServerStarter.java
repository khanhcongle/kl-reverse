package kl.proxy.kl_reverse;

import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import kl.proxy.kl_reverse.context.ContextAccessable;

public abstract class ServerStarter {
	private static final String ORIGIN_HOST_DOMAIN = "localhost";
	
	protected Vertx vertx;
	protected Promise<Void> startPromise;
	
	public ServerStarter(Promise<Void> startPromise) {
		this.vertx = ContextAccessable.getVertx();
		this.startPromise = startPromise;
	}
	
	public abstract void start(JsonObject jsonConfig);
	
	protected String getHostDomain() {
		return ORIGIN_HOST_DOMAIN;
	}
	
	protected void serverStartListener(String serverName, int port, AsyncResult<HttpServer> result, Promise<Void> startPromise) {
		if (result.succeeded()) {
			System.out.println(serverName + " started on port: " + port);
		} else {
			startPromise.fail(result.cause());
		}
	}
	
	
	
}
