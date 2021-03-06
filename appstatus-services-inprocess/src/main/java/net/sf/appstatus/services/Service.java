package net.sf.appstatus.services;

import java.util.concurrent.atomic.AtomicLong;

import net.sf.appstatus.core.services.IService;

import org.apache.commons.lang3.ObjectUtils;

public class Service implements IService {
	protected AtomicLong cacheHits = new AtomicLong();
	protected AtomicLong hits = new AtomicLong();
	protected AtomicLong running = new AtomicLong();
	protected String name;
	protected String group;
	private CachedCallStatistics totalStats;
	private CachedCallStatistics windowStats;
	private CachedCallStatistics windowPrevisiousStats;
	private long windowSize = 2000;
	private long windowStart = 0;
	private int minMaxDelay;

	public Service(int minMaxDelay) {
		this.minMaxDelay = minMaxDelay;
		totalStats = new CachedCallStatistics(minMaxDelay);
		windowStats = new CachedCallStatistics(minMaxDelay);
		windowPrevisiousStats = new CachedCallStatistics(minMaxDelay);
	}

	public long getRunning() {

		while (true) {
			long runningCalls = running.get();

			if (runningCalls < 0) {
				// Try to fix value
				running.compareAndSet(runningCalls, 0);
			} else
				return runningCalls;
		}

	}


	/**
	 * This method is synchronized to ensure correct statistics computation
	 * 
	 * @param executionTime
	 * @param cacheHit
	 * @param failure
	 * @param error
	 */
	public void addCall(Long executionTime, boolean cacheHit, boolean failure, boolean error, int nestedCalls) {
		totalStats.addCall(executionTime, cacheHit, failure, error, nestedCalls);

		updateWindows();
		windowStats.addCall(executionTime, cacheHit, failure, error, nestedCalls);
	}

	/**
	 * Archive previous window if expired, and create a new window for next
	 * results.
	 */
	private void updateWindows() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - windowStart > windowSize) {
			synchronized (this) {
				if (currentTime - windowStart > windowSize) {

					if (currentTime - windowStart <= 2 * windowSize) {
						windowPrevisiousStats = windowStats;
					} else {
						windowPrevisiousStats = new CachedCallStatistics(minMaxDelay);
					}
					windowStats = new CachedCallStatistics(minMaxDelay);
					windowStart = currentTime;
				}

			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Double getAvgResponseTime() {
		return totalStats.getDirectStatistics().getAvgResponseTime();
	}

	public long getCacheHits() {
		return cacheHits.get();
	}

	public long getHits() {
		return hits.get();
	}

	public long getFailures() {
		return totalStats.getFailures();
	}

	public long getErrors() {
		return totalStats.getErrors();
	}

	public Long getMaxResponseTime() {
		return totalStats.getDirectStatistics().getMaxResponseTime();
	}

	public Long getMinResponseTime() {
		return totalStats.getDirectStatistics().getMinResponseTime();
	}

	public Double getAvgResponseTimeWithCache() {
		return totalStats.getCacheStatistics().getAvgResponseTime();
	}

	public Long getMaxResponseTimeWithCache() {
		return totalStats.getCacheStatistics().getMaxResponseTime();
	}

	public Long getMinResponseTimeWithCache() {
		return totalStats.getCacheStatistics().getMinResponseTime();
	}

	public double getAvgNestedCalls() {
		return totalStats.getDirectStatistics().getAvgNestedCalls();
	}

	public double getAvgNestedCallsWithCache() {
		return totalStats.getCacheStatistics().getAvgNestedCalls();
	}

	public int compareTo(IService otherService) {
		int groupCompare = ObjectUtils.compare(group, otherService.getGroup());
		if (groupCompare != 0) {
			return groupCompare;
		}

		return ObjectUtils.compare(name, otherService.getName());
	}

	public double getCurrentRate() {
		updateWindows();
		return (windowPrevisiousStats.getTotalHits() * 1000) / (double) windowSize;
	}

}
