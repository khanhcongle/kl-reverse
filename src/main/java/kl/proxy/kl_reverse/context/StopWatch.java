package kl.proxy.kl_reverse.context;

import java.util.HashMap;
import java.util.Map;

import kl.proxy.kl_reverse.proxy.cache.Resource;
import kl.proxy.kl_reverse.proxy.cache.ResourceRecord;

public class StopWatch {
	
	private static Map<Integer, ResourceRecord> localMap = new HashMap<>();

	public static void putRequest(int hashCode, Resource resource) {
		ResourceRecord resourceRecord = new ResourceRecord(System.currentTimeMillis(), resource);
		getResourceMap().put(hashCode, resourceRecord);
	}
	
	public static ResourceRecord removeRequest(int hashCode) {
		return getResourceMap().remove(hashCode);
	}

	private static Map<Integer, ResourceRecord> getResourceMap() {
		return localMap; // TODO: issue with local cache so using the static one
//		return DataSharable.getSharedData().<Integer, ResourceRecord>getLocalMap(Constants.SharedData.REQUEST_START_TIME_MAP);
	}
}
