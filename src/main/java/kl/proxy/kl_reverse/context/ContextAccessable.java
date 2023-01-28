package kl.proxy.kl_reverse.context;

import io.vertx.core.Vertx;

public interface ContextAccessable {

	static Vertx getVertx() {
		return Vertx.currentContext().owner();
	}
}