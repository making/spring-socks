package lol.maki.socks.accesslog;


public class AccessLogBuilder {

	private String address;

	private boolean crawler;

	private String date;

	private long elapsed;

	private String host;

	private String method;

	private String path;

	private String referer;

	private int status;

	private String userAgent;

	public AccessLog build() {
		return new AccessLog(date, method, path, status, host, address, elapsed, userAgent, referer);
	}

	public AccessLogBuilder setAddress(String address) {
		this.address = address;
		return this;
	}

	public AccessLogBuilder setDate(String date) {
		this.date = date;
		return this;
	}

	public AccessLogBuilder setElapsed(long elapsed) {
		this.elapsed = elapsed;
		return this;
	}

	public AccessLogBuilder setHost(String host) {
		this.host = host;
		return this;
	}

	public AccessLogBuilder setMethod(String method) {
		this.method = method;
		return this;
	}

	public AccessLogBuilder setPath(String path) {
		this.path = path;
		return this;
	}

	public AccessLogBuilder setReferer(String referer) {
		this.referer = referer;
		return this;
	}

	public AccessLogBuilder setStatus(int status) {
		this.status = status;
		return this;
	}

	public AccessLogBuilder setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}
}