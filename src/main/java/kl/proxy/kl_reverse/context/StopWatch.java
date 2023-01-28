package kl.proxy.kl_reverse.context;

import io.vertx.core.shareddata.LocalMap;
import kl.proxy.kl_reverse.Constants;

public class StopWatch {

	public static void putStartTime(int hashCode) {
		getStartTimeMap().put(hashCode, System.currentTimeMillis());
	}

	public static LocalMap<Integer, Long> getStartTimeMap() {
		return DataSharable.getSharedData().getLocalMap(Constants.SharedData.API_RECORDS_ASYNC_MAP);
	}
	
	public static long removeStartTime(int hashCode) {
		return getStartTimeMap().remove(hashCode);
	}
}
