package kl.proxy.kl_reverse;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

public class MainVerticle extends AbstractVerticle {
	
	private Promise<Void> startPromise;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		this.startPromise = startPromise;
		
		getConfig().onSuccess(jsonConfig -> {
			new ServerStarter.ResourceServerStarter(startPromise).start(jsonConfig);
			new ServerStarter.ProxyServerStarter(startPromise).start(jsonConfig);
			
			this.startPromise.complete();
		});
	}
	
	private Future<JsonObject> getConfig() {
		ConfigStoreOptions systemConfig = new ConfigStoreOptions().setType("sys").setConfig(new JsonObject().put("hierarchical", true));
		ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions().addStore(systemConfig);
		ConfigRetriever retriever = ConfigRetriever.create(vertx, configRetrieverOptions);
		return retriever.getConfig();
	}
	
	
}
