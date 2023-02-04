package kl.proxy.kl_reverse.context;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.AbstractQueue;
import java.util.Date;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import kl.proxy.kl_reverse.Constants;

public class RequestsLogger {
	
	synchronized public static void add(String prettJson) {		
		Handler<PriorityBlockingQueue<String>> handler = queue1 -> queue1.add(prettJson);
		
		PriorityBlockingQueue<String> queue = putRequestLoggerQueue(handler);
		
		System.out.println("queue.size() = " + queue.size());
		if (queue.size() > 10) {
			queue.remove();
		}
	}
	
	public static JsonArray get(LocalDateTime from, LocalDateTime to) {
		AbstractQueue<String> queue = putRequestLoggerQueue(q -> {});

		Predicate<JsonObject> predicate = json -> timeRangeMatcher(json.getLong("start"), from, to);
		
		return get(queue, predicate);
	}

	private static JsonArray get(AbstractQueue<String> queue, Predicate<JsonObject> predicate) {
		List<JsonObject> jsonObjects = queue.stream()
				.map(string -> new JsonObject(string))
				.filter(predicate)
				.sorted((o1, o2) -> o1.getLong("start").compareTo(o2.getLong("start")))
				.collect(Collectors.toUnmodifiableList());
		return new JsonArray(jsonObjects);
	}

	private static boolean timeRangeMatcher(long date, LocalDateTime from, LocalDateTime to) {
		LocalDateTime givenDateTime = LocalDateTime.ofInstant(new Date(date).toInstant(), ZoneOffset.UTC);
		
		boolean isMatch = true;
		isMatch &= from != null ? from.isBefore(givenDateTime) || givenDateTime.isEqual(from) : true;
		isMatch &= to != null ? givenDateTime.isBefore(to) : true;
		return isMatch;
	}
	
	private static PriorityBlockingQueue<String> putRequestLoggerQueue(Handler<PriorityBlockingQueue<String>> handler) {
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
