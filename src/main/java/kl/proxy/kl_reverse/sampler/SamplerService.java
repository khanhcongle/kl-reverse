package kl.proxy.kl_reverse.sampler;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.AbstractQueue;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
import kl.proxy.kl_reverse.Constants;
import kl.proxy.kl_reverse.context.DataSharable;
import kl.proxy.kl_reverse.context.StopWatch;

public class SamplerService {
	private static final Handler<PriorityBlockingQueue<String>> DO_NOTHING = q -> {};
	
	public static Optional<JsonObject> getFirstMatch(Predicate<JsonObject> predicate) {
		AbstractQueue<String> queue = putRequestLoggerQueue(DO_NOTHING);

		return get(queue, predicate).stream().findFirst();
	}

	private static PriorityBlockingQueue<String> putRequestLoggerQueue(Handler<PriorityBlockingQueue<String>> handler) {
		LocalMap<String, PriorityBlockingQueue<String>> localMap = DataSharable.getSharedData()
				.getLocalMap(Constants.SharedData.REQUEST_LOGGER_MAP);

		PriorityBlockingQueue<String> queue = localMap.get(Constants.SharedData.DEFAULT_KEY);
		if (queue == null) {
			queue = new PriorityBlockingQueue<String>();
		}
		handler.handle(queue);
		localMap.put(Constants.SharedData.DEFAULT_KEY, queue);
		return queue;
	}

	public static String get(LocalDateTime from, LocalDateTime to) {
		AbstractQueue<String> queue = putRequestLoggerQueue(q -> {});

		Predicate<JsonObject> predicate = json -> timeRangeMatcher(json.getLong("start"), from, to);

		return new JsonArray(get(queue, predicate)).encodePrettily();
	}

	private static List<JsonObject> get(AbstractQueue<String> queue, Predicate<JsonObject> predicate) {
		List<JsonObject> jsonObjects = queue.stream().map(string -> new JsonObject(string)).filter(predicate)
				.sorted((o1, o2) -> o1.getLong("start").compareTo(o2.getLong("start")))
				.collect(Collectors.toUnmodifiableList());
		return jsonObjects;
	}

	public static void logRequest(ProxyRequest request, ProxyResponse response) {
		String requestUri = String.join(" ", request.getMethod().toString(), request.getURI());

		long currentTimeMillis = System.currentTimeMillis();
		long start = StopWatch.removeStartTime(request.hashCode());
		long responseTimeMilis = currentTimeMillis - start;

		JsonObject record;
		try {
			record = JsonObject.of("request", requestUri, "status", response.getStatusCode(), "start", start, "time",
					responseTimeMilis, "path", new URL(request.absoluteURI()).getPath());
			String prettJson = record.encodePrettily();
			System.out.println(prettJson);
			add(prettJson);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	synchronized private static void add(String prettJson) {
		Handler<PriorityBlockingQueue<String>> handler = queue1 -> queue1.add(prettJson);

		PriorityBlockingQueue<String> queue = putRequestLoggerQueue(handler);

		System.out.println("queue.size() = " + queue.size());
		if (queue.size() > 100) {
			queue.remove();
		}
	}

	private static boolean timeRangeMatcher(long date, LocalDateTime from, LocalDateTime to) {
		LocalDateTime givenDateTime = LocalDateTime.ofInstant(new Date(date).toInstant(), ZoneOffset.UTC);

		boolean isMatch = true;
		isMatch &= from != null ? from.isBefore(givenDateTime) || givenDateTime.isEqual(from) : true;
		isMatch &= to != null ? givenDateTime.isBefore(to) : true;
		return isMatch;
	}
}
