package kl.proxy.kl_reverse.context;

import io.vertx.core.shareddata.SharedData;

public interface DataSharable {

	static SharedData getSharedData() {
		return ContextAccessable.getVertx().sharedData();
	}
}