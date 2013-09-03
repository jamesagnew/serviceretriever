package net.svcret.admin.shared.model;


public class GMonitorRuleFiringProblem extends BaseGObject {
	private static final long serialVersionUID = 1L;

	private Long myFailedLatencyAverageMillisPerCall;
	private Long myFailedLatencyAverageOverMinutes;
	private Long myFailedLatencyThreshold;
	private String myFailedUrlMessage;
	private Long myUrlPid;
	private long myServiceVersionPid;
	
	public Long getFailedLatencyAverageMillisPerCall() {
		return myFailedLatencyAverageMillisPerCall;
	}
	public Long getFailedLatencyAverageOverMinutes() {
		return myFailedLatencyAverageOverMinutes;
	}

	public Long getFailedLatencyThreshold() {
		return myFailedLatencyThreshold;
	}

	public String getFailedUrlMessage() {
		return myFailedUrlMessage;
	}

	public Long getUrlPid() {
		return myUrlPid;
	}

	public long getServiceVersionPid() {
		return myServiceVersionPid;
	}

	@Override
	public void merge(BaseGObject theObject) {
		throw new UnsupportedOperationException();
	}

	public void setFailedLatencyAverageMillisPerCall(Long theFailedLatencyAverageMillisPerCall) {
		myFailedLatencyAverageMillisPerCall = theFailedLatencyAverageMillisPerCall;
	}

	public void setFailedLatencyAverageOverMinutes(Long theFailedLatencyAverageOverMinutes) {
		myFailedLatencyAverageOverMinutes = theFailedLatencyAverageOverMinutes;
	}

	public void setFailedLatencyThreshold(Long theFailedLatencyThreshold) {
		myFailedLatencyThreshold = theFailedLatencyThreshold;
	}

	public void setFailedUrlMessage(String theFailedUrlMessage) {
		myFailedUrlMessage = theFailedUrlMessage;
	}
	public void setUrlPid(Long theUrlPid) {
		myUrlPid = theUrlPid;
	}

	public void setServiceVersionPid(long theServiceVersionPid) {
		myServiceVersionPid = theServiceVersionPid;
	}

}
