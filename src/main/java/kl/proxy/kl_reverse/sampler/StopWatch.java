package kl.proxy.kl_reverse.sampler;

import io.vertx.core.shareddata.LocalMap;
import kl.proxy.kl_reverse.Constants;
import kl.proxy.kl_reverse.context.DataSharable;

public class StopWatch {

	public static void putStartTime(int hashCode) {
		getStartTimeMap().put(hashCode, System.currentTimeMillis());
	}

	public static LocalMap<Integer, Long> getStartTimeMap() {
		return DataSharable.getSharedData().getLocalMap(Constants.SharedData.REQUEST_START_TIME_MAP);
	}
	
	public static long removeStartTime(int hashCode) {
		return getStartTimeMap().remove(hashCode);
	}
}