package kl.proxy.kl_reverse.context;

import io.vertx.core.shareddata.LocalMap;
import kl.proxy.kl_reverse.Constants;

public class StopWatch {

	public static void putRequest(int hashCode) {
		getResourceMap().put(hashCode, System.currentTimeMillis());
	}
	
	public static long removeRequest(int hashCode) {
		return getResourceMap().remove(hashCode);
	}

	private static LocalMap<Integer, Long> getResourceMap() {
		return DataSharable.getSharedData().getLocalMap(Constants.SharedData.REQUEST_START_TIME_MAP);
	}
}
