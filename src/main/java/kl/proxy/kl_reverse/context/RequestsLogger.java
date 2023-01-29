package kl.proxy.kl_reverse.context;

import java.util.AbstractQueue;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.shareddata.LocalMap;
import kl.proxy.kl_reverse.Constants;

public class RequestsLogger {
	
	synchronized public static void add(String prettJson) {		
		Handler<PriorityBlockingQueue<String>> handler = queue1 -> queue1.add(prettJson);
		
		PriorityBlockingQueue<String> queue = updateRequestLoggerQueue(handler);
		
		System.out.println("queue.size() = " + queue.size());
		if (queue.size() > 10) {
			queue.remove();
		}
	}
	
	public static JsonArray get() {
		AbstractQueue<String> queue = updateRequestLoggerQueue(q -> {});

		List<Object> jsonObjects = queue.stream()
				.map(each -> Json.decodeValue(each))
				.collect(Collectors.toUnmodifiableList());
		return new JsonArray(jsonObjects);
	}
	
	private static PriorityBlockingQueue<String> updateRequestLoggerQueue(Handler<PriorityBlockingQueue<String>> handler) {
		LocalMap<String, PriorityBlockingQueue<String>> localMap =
				DataSharable.getSharedData().getLocalMap(Constants.SharedData.REQUEST_LOGGER_MAP);
		
		PriorityBlockingQueue<String> queue = localMap.get(Constants.SharedData.DEFAULT_KEY);
		if(queue == null) {
			queue = new PriorityBlockingQueue<String>();
		}
		handler.handle(queue);
		localMap.put(Constants.SharedData.DEFAULT_KEY, queue);
		return queue;
	}
}
