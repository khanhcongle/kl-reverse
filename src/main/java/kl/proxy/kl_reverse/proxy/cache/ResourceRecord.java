package kl.proxy.kl_reverse.proxy.cache;

public class ResourceRecord {
	
	private long time;
	private Resource resource;
	
	public ResourceRecord(long time, Resource resource) {
		this.time = time;
		this.resource = resource;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
}
