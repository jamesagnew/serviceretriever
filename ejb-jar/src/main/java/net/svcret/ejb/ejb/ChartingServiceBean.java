package net.svcret.ejb.ejb;

import static net.svcret.ejb.ejb.AdminServiceBean.addToInt;
import static net.svcret.ejb.ejb.AdminServiceBean.addToLong;
import static net.svcret.ejb.ejb.AdminServiceBean.doWithStatsByMinute;
import static net.svcret.ejb.ejb.AdminServiceBean.doWithStatsSupportFindInterval;
import static net.svcret.ejb.ejb.AdminServiceBean.doWithStatsSupportIncrement;
import static net.svcret.ejb.ejb.AdminServiceBean.growToSizeDouble;
import static net.svcret.ejb.ejb.AdminServiceBean.growToSizeInt;
import static net.svcret.ejb.ejb.AdminServiceBean.growToSizeLong;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.imageio.ImageIO;

import net.svcret.admin.shared.model.TimeRange;
import net.svcret.ejb.api.IChartingServiceBean;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.api.IScheduler;
import net.svcret.ejb.ejb.AdminServiceBean.IWithStats;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.ejb.model.entity.PersInvocationMethodUserStats;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.rrd4j.ConsolFun;
import org.rrd4j.data.LinearInterpolator;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

import com.google.common.annotations.VisibleForTesting;

/**
 * Bean which generates graphs with statistics about things relating to SR.
 * 
 * Note on testing this class: The unit tests actually generate graph files,
 * so they can be used to try things out.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class ChartingServiceBean implements IChartingServiceBean {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ChartingServiceBean.class);

	private static final Double ZERO_DOUBLE = 0.0;

	@EJB
	private IConfigService myConfig;

	@EJB
	private IDao myDao;

	@EJB
	private IScheduler myScheduler;

	@EJB
	private IRuntimeStatusQueryLocal myStatus;

	@Override
	public byte[] renderLatencyGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException, UnexpectedFailureException {
		ourLog.info("Rendering latency graph for service version {}", theServiceVersionPid);

		myScheduler.flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();

		final ArrayList<Integer> invCount60Min = new ArrayList<Integer>();
		final ArrayList<Long> time60min = new ArrayList<Long>();
		final ArrayList<Long> timestamps = new ArrayList<Long>();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
//		StatsAccumulator accumulator = new StatsAccumulator();
		
		for (PersServiceVersionMethod nextMethod : svcVer.getMethods()) {
			
			doWithStatsByMinute(myConfig.getConfig(), theRange, myStatus, nextMethod, new IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>() {
				@Override
				public void withStats(int theIndex, PersInvocationMethodSvcverStats theStats) {
					growToSizeInt(invCount60Min, theIndex);
					growToSizeLong(time60min, theIndex);
					growToSizeLong(timestamps, theIndex);
					invCount60Min.set(theIndex, addToInt(invCount60Min.get(theIndex), theStats.getSuccessInvocationCount()));
					time60min.set(theIndex, time60min.get(theIndex) + theStats.getSuccessInvocationTotalTime());
					timestamps.set(theIndex, theStats.getPk().getStartTime().getTime());
				}
			});
		}

		return renderLatency(invCount60Min, time60min, timestamps);
	}

	@Override
	public byte[] renderPayloadSizeGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException {
		ourLog.info("Rendering payload size graph for service version {}", theServiceVersionPid);

		myScheduler.flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();

		final List<Integer> invCount = new ArrayList<Integer>();
		final List<Long> totalSuccessReqBytes = new ArrayList<Long>();
		final List<Long> totalSuccessRespBytes = new ArrayList<Long>();
		final List<Long> timestamps = new ArrayList<Long>();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		for (PersServiceVersionMethod nextMethod : svcVer.getMethods()) {
			doWithStatsByMinute(myConfig.getConfig(), theRange, myStatus, nextMethod, new IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>() {
				@Override
				public void withStats(int theIndex, PersInvocationMethodSvcverStats theStats) {
					growToSizeInt(invCount, theIndex);
					growToSizeLong(totalSuccessRespBytes, theIndex);
					growToSizeLong(totalSuccessReqBytes, theIndex);
					growToSizeLong(timestamps, theIndex);

					totalSuccessReqBytes.set(theIndex, addToLong(totalSuccessReqBytes.get(theIndex), theStats.getSuccessRequestMessageBytes()));
					totalSuccessRespBytes.set(theIndex, addToLong(totalSuccessRespBytes.get(theIndex), theStats.getSuccessResponseMessageBytes()));
					invCount.set(theIndex, addToInt(invCount.get(theIndex), theStats.getSuccessInvocationCount()));
					timestamps.set(theIndex, theStats.getPk().getStartTime().getTime());
				}
			});
		}

		double[] avgSuccessReqSize = new double[invCount.size()];
		double[] avgSuccessRespSize = new double[invCount.size()];
		for (int i = 0; i < invCount.size(); i++) {
			avgSuccessReqSize[i] = invCount.get(i) == 0 ? 0 : totalSuccessReqBytes.get(i) / invCount.get(i);
			avgSuccessRespSize[i] = invCount.get(i) == 0 ? 0 : totalSuccessRespBytes.get(i) / invCount.get(i);
		}

		return renderPayloadSize(invCount, avgSuccessReqSize, avgSuccessRespSize, "Bytes/Message", timestamps);
	}

	@Override
	public byte[] renderThrottlingGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException {
		ourLog.info("Rendering throttling graph for service version {}", theServiceVersionPid);

		myScheduler.flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();

		final List<Long> throttleAcceptCount = new ArrayList<Long>();
		final List<Long> throttleRejectCount = new ArrayList<Long>();
		final List<Long> timestamps = new ArrayList<Long>();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		for (PersServiceVersionMethod nextMethod : svcVer.getMethods()) {
			doWithStatsByMinute(myConfig.getConfig(), theRange, myStatus, nextMethod, new IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>() {
				@Override
				public void withStats(int theIndex, PersInvocationMethodSvcverStats theStats) {
					growToSizeLong(throttleAcceptCount, theIndex);
					growToSizeLong(throttleRejectCount, theIndex);
					growToSizeLong(timestamps, theIndex);

					throttleAcceptCount.set(theIndex, throttleAcceptCount.get(theIndex) + theStats.getTotalThrottleAccepts());
					throttleRejectCount.set(theIndex, throttleRejectCount.get(theIndex) + theStats.getTotalThrottleRejections());
					timestamps.set(theIndex, theStats.getPk().getStartTime().getTime());
				}
			});

		}

		return renderThrottling(throttleAcceptCount, throttleRejectCount, timestamps, "Throttled Calls / Min");
	}

	@Override
	public byte[] renderUsageGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException {
		ourLog.info("Rendering latency graph for service version {}", theServiceVersionPid);

		myScheduler.flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();

		final List<Double> invCount = new ArrayList<Double>();
		final List<Double> invCountFault = new ArrayList<Double>();
		final List<Double> invCountFail = new ArrayList<Double>();
		final List<Double> invCountSecurityFail = new ArrayList<Double>();
		final List<Long> timestamps = new ArrayList<Long>();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		for (PersServiceVersionMethod nextMethod : svcVer.getMethods()) {
			doWithStatsByMinute(myConfig.getConfig(), theRange, myStatus, nextMethod, new IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>() {
				@Override
				public void withStats(int theIndex, PersInvocationMethodSvcverStats theStats) {
					growToSizeDouble(invCount, theIndex);
					growToSizeDouble(invCountFault, theIndex);
					growToSizeDouble(invCountFail, theIndex);
					growToSizeDouble(invCountSecurityFail, theIndex);
					growToSizeLong(timestamps, theIndex);

					double numMinutes = theStats.getPk().getInterval().numMinutes();

					invCount.set(theIndex, invCount.get(theIndex) + (theStats.getSuccessInvocationCount() / numMinutes));
					invCountFault.set(theIndex, invCountFault.get(theIndex) + (theStats.getFaultInvocationCount() / numMinutes));
					invCountFail.set(theIndex, invCountFail.get(theIndex) + (theStats.getFailInvocationCount() / numMinutes));
					invCountSecurityFail.set(theIndex, invCountSecurityFail.get(theIndex) + (theStats.getServerSecurityFailures() / numMinutes));
					timestamps.set(theIndex, theStats.getPk().getStartTime().getTime());
				}
			});

		}

		return renderUsage(invCount, invCountFault, invCountFail, invCountSecurityFail, "Calls / Min", timestamps);
	}

	@Override
	public byte[] renderUserMethodGraphForUser(long theUserPid, TimeRange theRange) throws UnexpectedFailureException, IOException {
		ourLog.info("Rendering user method graph for user {}", theUserPid);

		myScheduler.flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();

		PersUser user = myDao.getUser(theUserPid);

		Date start = theRange.getNoPresetFrom();
		Date end = theRange.getNoPresetTo();
		if (theRange.getWithPresetRange() != null) {
			start = new Date(System.currentTimeMillis() - (theRange.getWithPresetRange().getNumMins() * DateUtils.MILLIS_PER_MINUTE));
			end = new Date();
		}

		HashMap<Long, List<Double>> methods = new HashMap<Long, List<Double>>();
		List<Long> timestamps = new ArrayList<Long>();

		InvocationStatsIntervalEnum nextInterval = doWithStatsSupportFindInterval(myConfig.getConfig(), start);
		Date nextDate = nextInterval.truncate(start);
		Date nextDateEnd = null;

		List<PersInvocationMethodUserStats> stats = myDao.getUserStatsWithinTimeRange(user, start, end);
		Validate.notNull(stats);
		stats = new ArrayList<PersInvocationMethodUserStats>(stats);
		Collections.sort(stats, new Comparator<PersInvocationMethodUserStats>() {
			@Override
			public int compare(PersInvocationMethodUserStats theO1, PersInvocationMethodUserStats theO2) {
				return theO1.getPk().getStartTime().compareTo(theO2.getPk().getStartTime());
			}
		});
		ourLog.debug("Loaded {} user {} stats for range {} - {}", new Object[] { stats.size(), theUserPid, start, end });
		if (stats.size() > 0) {
			ourLog.debug("Found stats with range {} - {}", stats.get(0).getPk().getStartTime(), stats.get(stats.size() - 1).getPk().getStartTime());
		}
		ourLog.debug("Looking for stats starting at {}", nextDate);

		Iterator<PersInvocationMethodUserStats> statIter = stats.iterator();
		PersInvocationMethodUserStats nextStat = null;

		int timesPassed = 0;
		int foundEntries = 0;
		double grandTotal = 0;
		while (nextDate.after(end) == false) {
			double numMinutes = nextInterval.numMinutes();
			nextDateEnd = DateUtils.addMinutes(nextDate, (int) nextInterval.numMinutes());
			timestamps.add(nextDate.getTime());
			int arrayIndex = timesPassed;
			timesPassed++;

			for (List<Double> next : methods.values()) {
				next.add(ZERO_DOUBLE);
			}

			while (nextStat == null || statIter.hasNext()) {
				if (nextStat == null && statIter.hasNext()) {
					nextStat = statIter.next();
					foundEntries++;
				}
				if (nextStat != null && nextStat.getPk().getStartTime().before(nextDateEnd)) {
					long methodPid = nextStat.getPk().getMethod();
					List<Double> newList = methods.get(methodPid);
					if (newList == null) {
						newList = new ArrayList<Double>();
						for (int i = 0; i < timesPassed; i++) {
							newList.add(ZERO_DOUBLE);
						}
						methods.put(methodPid, newList);
					}

					// TODO: should we have an option to include fails/faults?
					double total = nextStat.getSuccessInvocationCount();
					double minuteTotal = total / numMinutes;

					grandTotal += minuteTotal;

					newList.set(arrayIndex, newList.get(arrayIndex) + minuteTotal);
					nextStat = null;
				} else {
					break;
				}

			}

			nextDate = doWithStatsSupportIncrement(nextDate, nextInterval);
			nextInterval = doWithStatsSupportFindInterval(myConfig.getConfig(), nextDate);
			nextDate = nextInterval.truncate(nextDate);
		}

		ourLog.debug("Found {} entries for {} methods", new Object[] { foundEntries, methods.size() });

		// Come up with a good order
		final Map<Long, PersServiceVersionMethod> pidToMethod = new HashMap<Long, PersServiceVersionMethod>();
		for (long nextPid : methods.keySet()) {
			PersServiceVersionMethod method = myDao.getServiceVersionMethodByPid(nextPid);
			if (method != null) {
				pidToMethod.put(nextPid, method);
			} else {
				ourLog.debug("Discarding unknown method: {}", nextPid);
			}
		}
		ArrayList<Long> pids = new ArrayList<Long>(pidToMethod.keySet());
		Collections.sort(pids, new Comparator<Long>() {
			@Override
			public int compare(Long theO1, Long theO2) {
				PersServiceVersionMethod m1 = pidToMethod.get(theO1);
				PersServiceVersionMethod m2 = pidToMethod.get(theO2);
				return MethodComparator.INSTANCE.compare(m1, m2);
			}
		});

		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setWidth(600);
		graphDef.setHeight(200);

		long[] graphTimestamps = new long[timestamps.size()];
		for (int i = 0; i < timestamps.size(); i++) {
			graphTimestamps[i] = timestamps.get(i) / 1000;
		}

		graphDef.setTimeSpan(graphTimestamps);
		graphDef.setVerticalLabel("Calls / Minute");
		graphDef.setTextAntiAliasing(true);

		int longestName = 0;
		for (PersServiceVersionMethod next : pidToMethod.values()) {
			longestName = Math.max(longestName, next.getName().length());
		}

		// Draw the methods
		String previousServiceDesc = null;
		List<Color> colours = createStackColours(pids.size());
		for (int i = 0; i < pids.size(); i++) {
			Long nextPid = pids.get(i);
			PersServiceVersionMethod nextMethod = pidToMethod.get(nextPid);
			List<Double> values = methods.get(nextPid);

			LinearInterpolator avgPlot = new LinearInterpolator(graphTimestamps, toDoublesFromDoubles(values));
			String srcName = "inv" + i;
			graphDef.datasource(srcName, avgPlot);
			String methodDesc = nextMethod.getServiceVersion().getService().getServiceId() + " " + nextMethod.getServiceVersion().getVersionId();
			if (!StringUtils.equals(previousServiceDesc, methodDesc)) {
				graphDef.comment(methodDesc + "\\l");
				previousServiceDesc=methodDesc;
			}

			double sumDoubles = sumDoubles(values);
			double pct = (sumDoubles / grandTotal);
			
			if (i == 0) {
				graphDef.area(srcName, colours.get(i), " "+StringUtils.rightPad(nextMethod.getName(), longestName));
			} else {
				graphDef.stack(srcName, colours.get(i), " "+StringUtils.rightPad(nextMethod.getName(), longestName));
			}
			graphDef.gprint(srcName, ConsolFun.AVERAGE, "Avg %5.1f ");
			graphDef.gprint(srcName, ConsolFun.MIN, "Min %5.1f ");
			graphDef.gprint(srcName, ConsolFun.MAX, "Max %5.1f ");
			
			String formattedPct = new DecimalFormat("0.0#%").format(pct);
			graphDef.comment("Pct: " + formattedPct + "\\l");
		}

		if (pids.size() == 0) {
			double[] values = new double[timestamps.size()];
			for (int j = 0; j < values.length; j++) {
				values[j] = 0.0;
			}
			LinearInterpolator avgPlot = new LinearInterpolator(graphTimestamps, values);
			String srcName = "inv";
			graphDef.datasource(srcName, avgPlot);
			graphDef.area(srcName, Color.BLACK, StringUtils.rightPad("No activity during this range", 100));
		}

		return render(graphDef);
	}

	private double sumDoubles(List<Double> theValues) {
		double retVal = 0.0;
		for (Double next : theValues) {
			retVal += next;
		}
		return retVal;
	}

	@VisibleForTesting
	void setConfigForUnitTest(IConfigService theConfig) {
		myConfig = theConfig;
	}

	@VisibleForTesting
	void setDaoForUnitTest(IDao theDao) {
		myDao = theDao;
	}

	@VisibleForTesting
	void setSchedulerForUnitTest(IScheduler theScheduler) {
		myScheduler = theScheduler;
	}

	@VisibleForTesting
	void setStatusForUnitTest(IRuntimeStatusQueryLocal theStatus) {
		myStatus = theStatus;
	}

	private List<Color> createStackColours(int theNum) {
		// 8a56e2,cf56e2,e256ae,e25668,e28956,e2cf56,aee256,68e256,56e289,56e2cf,56aee2,5668e2
		ArrayList<Color> retVal = new ArrayList<Color>();
		if (theNum == 0) {
			return retVal;
		}

		Color baseColour = Color.decode("#8A56E2");
		retVal.add(baseColour);

		float[] hsbVals = new float[3];
		Color.RGBtoHSB(baseColour.getRed(), baseColour.getGreen(), baseColour.getBlue(), hsbVals);

		double step = 1.0 / theNum;
		for (int i = 1; i < theNum; i++) {
			hsbVals[0] += step;
			while (hsbVals[0] > 1.0) {
				hsbVals[0] -= 1.0;
			}

			int next = Color.HSBtoRGB(hsbVals[0], hsbVals[1], hsbVals[2]);
			retVal.add(new Color(next));
		}

		return retVal;
	}

	private byte[] render(RrdGraphDef graphDef) throws IOException {

		graphDef.setImageFormat("PNG");
		RrdGraph graph = new RrdGraph(graphDef);

		BufferedImage bi = new BufferedImage(graph.getRrdGraphInfo().getWidth(), graph.getRrdGraphInfo().getHeight(), BufferedImage.TYPE_INT_RGB);
		graph.render(bi.getGraphics());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(bi, "png", bos);

		return bos.toByteArray();
	}

	private byte[] renderLatency(List<Integer> theNumCalls, List<Long> theTotalTime, List<Long> theTimestampsMillis) throws IOException {
		Validate.isTrue(theNumCalls.size() == theTotalTime.size());

		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setWidth(600);
		graphDef.setHeight(200);

		long[] timestamps = new long[theNumCalls.size()];
		double[] avgValues = new double[theNumCalls.size()];

		double previousValue = 0.0;
		for (int i = 0; i < theNumCalls.size(); i++) {
			timestamps[i] = theTimestampsMillis.get(i) / 1000;
			previousValue = theNumCalls.get(i) > 0 ? (theTotalTime.get(i) / theNumCalls.get(i)) : previousValue;
			avgValues[i] = previousValue;
		}

		LinearInterpolator avgPlot = new LinearInterpolator(timestamps, avgValues);
		graphDef.datasource("avg", avgPlot);
		graphDef.setTimeSpan(timestamps);
		graphDef.setVerticalLabel("Latency (millis/call)");
		graphDef.setTextAntiAliasing(true);

		graphDef.line("avg", Color.BLACK, "Average (ms)", 2);
		graphDef.gprint("avg", ConsolFun.AVERAGE, "Average %8.1f    ");
		graphDef.gprint("avg", ConsolFun.MIN, "Min  %8.1f    ");
		graphDef.gprint("avg", ConsolFun.MAX, "Max  %8.1f    ");

		return render(graphDef);
	}

	private byte[] renderPayloadSize(List<Integer> theInvCount, double[] theAvgSuccessReqBytes, double[] theAvgSuccessRespBytes, String theIntervalDesc, List<Long> theTimestampsMillis)
			throws IOException {
		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setWidth(600);
		graphDef.setHeight(200);

		long[] timestamps = new long[theInvCount.size()];

		double prevReq = 0.0;
		double prevResp = 0.0;
		for (int i = 0; i < theInvCount.size(); i++) {
			timestamps[i] = theTimestampsMillis.get(i) / 1000;
			prevReq = theAvgSuccessReqBytes[i] > 0 ? theAvgSuccessReqBytes[i] : prevReq;
			theAvgSuccessReqBytes[i] = prevReq;
			prevResp = theAvgSuccessRespBytes[i] > 0 ? theAvgSuccessRespBytes[i] : prevResp;
			theAvgSuccessRespBytes[i] = prevResp;
		}

		for (int i = 0; i < theAvgSuccessReqBytes.length; i++) {
		}

		graphDef.setTimeSpan(timestamps);
		graphDef.setVerticalLabel(theIntervalDesc);
		graphDef.setTextAntiAliasing(true);

		LinearInterpolator reqPlot = new LinearInterpolator(timestamps, theAvgSuccessReqBytes);
		graphDef.datasource("req", reqPlot);
		graphDef.line("req", Color.RED, "Requests  ", 2.0f);
		graphDef.gprint("req", ConsolFun.AVERAGE, "Average Size %8.1f   ");
		graphDef.gprint("req", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("req", ConsolFun.MAX, "Max %8.1f\\l");

		LinearInterpolator respPlot = new LinearInterpolator(timestamps, theAvgSuccessRespBytes);
		graphDef.datasource("resp", respPlot);
		graphDef.line("resp", Color.GREEN, "Responses ", 2.0f);
		graphDef.gprint("resp", ConsolFun.AVERAGE, "Average Size %8.1f   ");
		graphDef.gprint("resp", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("resp", ConsolFun.MAX, "Max %8.1f\\l");

		return render(graphDef);

	}

	private byte[] renderThrottling(List<Long> theAcceptCount, List<Long> theReject, List<Long> theTimestampsMillis, String theIntervalDesc) throws IOException {
		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setWidth(600);
		graphDef.setHeight(200);

		long[] timestamps = new long[theTimestampsMillis.size()];

		for (int i = 0; i < theTimestampsMillis.size(); i++) {
			timestamps[i] = theTimestampsMillis.get(i) / 1000;
		}

		graphDef.setTimeSpan(timestamps);
		graphDef.setVerticalLabel(theIntervalDesc);
		graphDef.setTextAntiAliasing(true);

		LinearInterpolator avgPlot = new LinearInterpolator(timestamps, toDoublesFromLongs(theAcceptCount));
		graphDef.datasource("inv", avgPlot);
		graphDef.area("inv", Color.GREEN, "Accepted (Throttled but call was allowed to proceed after delay)\\l");
		graphDef.gprint("inv", ConsolFun.AVERAGE, "Average %8.1f   ");
		graphDef.gprint("inv", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("inv", ConsolFun.MAX, "Max %8.1f\\l");

		LinearInterpolator avgFaultPlot = new LinearInterpolator(timestamps, toDoublesFromLongs(theReject));
		graphDef.datasource("invfault", avgFaultPlot);
		graphDef.stack("invfault", Color.BLUE, "Rejected (Throttle queue was full or not allowed)\\l");
		graphDef.gprint("invfault", ConsolFun.AVERAGE, "Average %8.1f   ");
		graphDef.gprint("invfault", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("invfault", ConsolFun.MAX, "Max %8.1f\\l");

		return render(graphDef);
	}

	private byte[] renderUsage(List<Double> theInvCount, List<Double> theInvCountFault, List<Double> theInvCountFail, List<Double> theInvCountSecurityFail, String theIntervalDesc,
			List<Long> theTimestampsMillis) throws IOException {
		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setWidth(600);
		graphDef.setHeight(200);

		long[] timestamps = new long[theInvCount.size()];

		for (int i = 0; i < theInvCount.size(); i++) {
			timestamps[i] = theTimestampsMillis.get(i) / 1000;
		}

		graphDef.setTimeSpan(timestamps);
		graphDef.setVerticalLabel(theIntervalDesc);
		graphDef.setTextAntiAliasing(true);

		LinearInterpolator avgPlot = new LinearInterpolator(timestamps, toDoublesFromDoubles(theInvCount));
		graphDef.datasource("inv", avgPlot);
		graphDef.area("inv", Color.GREEN, StringUtils.rightPad("Successful Calls", 20));
		graphDef.gprint("inv", ConsolFun.AVERAGE, "Average %8.1f   ");
		graphDef.gprint("inv", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("inv", ConsolFun.MAX, "Max %8.1f\\l");

		LinearInterpolator avgFaultPlot = new LinearInterpolator(timestamps, toDoublesFromDoubles(theInvCountFault));
		graphDef.datasource("invfault", avgFaultPlot);
		graphDef.stack("invfault", Color.BLUE, StringUtils.rightPad("Faults", 20));
		graphDef.gprint("invfault", ConsolFun.AVERAGE, "Average %8.1f   ");
		graphDef.gprint("invfault", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("invfault", ConsolFun.MAX, "Max %8.1f\\l");

		LinearInterpolator avgFailPlot = new LinearInterpolator(timestamps, toDoublesFromDoubles(theInvCountFail));
		graphDef.datasource("invfail", avgFailPlot);
		graphDef.stack("invfail", Color.GRAY, StringUtils.rightPad("Fails", 20));
		graphDef.gprint("invfail", ConsolFun.AVERAGE, "Average %8.1f   ");
		graphDef.gprint("invfail", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("invfail", ConsolFun.MAX, "Max %8.1f\\l");

		LinearInterpolator avgSecurityFailPlot = new LinearInterpolator(timestamps, toDoublesFromDoubles(theInvCountSecurityFail));
		graphDef.datasource("invSecurityFail", avgSecurityFailPlot);
		graphDef.stack("invSecurityFail", Color.RED, StringUtils.rightPad("Security Fails", 20));
		graphDef.gprint("invSecurityFail", ConsolFun.AVERAGE, "Average %8.1f   ");
		graphDef.gprint("invSecurityFail", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("invSecurityFail", ConsolFun.MAX, "Max %8.1f\\l");

		return render(graphDef);
	}

	private double[] toDoublesFromDoubles(List<Double> theInvCount) {
		double[] retVal = new double[theInvCount.size()];
		int i = 0;
		for (Double next : theInvCount) {
			retVal[i++] = next;
		}
		return retVal;
	}

	private double[] toDoublesFromLongs(List<Long> theInvCount) {
		double[] retVal = new double[theInvCount.size()];
		int i = 0;
		for (Long next : theInvCount) {
			retVal[i++] = next;
		}
		return retVal;
	}

	public static void main(String[] args) throws IOException {
		// if (true) {
		//
		// ChartingServiceBean c = new ChartingServiceBean();
		// TimeRange range = new TimeRange();
		// range.setRange(TimeRangeEnum.ONE_MONTH);
		// c.renderLatencyGraphForServiceVersion(0, range);
		//
		// return;
		// }

		System.setProperty("java.awt.headless", "true");
		int num = 60 * 2;
		long startTime = System.currentTimeMillis() - (num * 60 * 1000);
		int interval = 60 * 1000;

		List<Integer> numCalls = new ArrayList<Integer>();
		List<Long> totalTime = new ArrayList<Long>();
		List<Long> timestamps = new ArrayList<Long>();

		long lastTime = startTime;
		for (int i = 0; i < num; i++) {
			numCalls.add((int) (2 * Math.random()));
			totalTime.add((long) (2 * Math.random()));
			timestamps.add(lastTime);
			lastTime = lastTime + interval;
		}

		byte[] bytes = new ChartingServiceBean().renderLatency(numCalls, totalTime, timestamps);
		FileOutputStream fos = new FileOutputStream("saved.png", false);
		fos.write(bytes);
		fos.close();

		List<Double> calls = new ArrayList<Double>();
		List<Double> callsFault = new ArrayList<Double>();
		List<Double> callsFail = new ArrayList<Double>();
		List<Double> callsSecFail = new ArrayList<Double>();
		for (int i = 0; i < num; i++) {
			calls.add((1000.0 * Math.random()));
			callsFault.add((1000.0 * Math.random()));
			callsFail.add((1000.0 * Math.random()));
			callsSecFail.add((1000.0 * Math.random()));
		}

		bytes = new ChartingServiceBean().renderUsage(calls, callsFault, callsFail, callsSecFail, "Calls/Min", timestamps);
		fos = new FileOutputStream("saved2.png", false);
		fos.write(bytes);
		fos.close();

	}

	private static final class MethodComparator implements Comparator<PersServiceVersionMethod> {
		public static final MethodComparator INSTANCE = new MethodComparator();

		@Override
		public int compare(PersServiceVersionMethod theO1, PersServiceVersionMethod theO2) {
			BasePersServiceVersion v1 = theO1.getServiceVersion();
			BasePersServiceVersion v2 = theO2.getServiceVersion();
			PersService s1 = v1.getService();
			PersService s2 = v2.getService();
			PersDomain d1 = s1.getDomain();
			PersDomain d2 = s2.getDomain();

			int cmp = d1.getDomainId().compareTo(d2.getDomainId());
			if (cmp != 0) {
				return cmp;
			}

			cmp = s1.getServiceId().compareTo(s2.getServiceId());
			if (cmp != 0) {
				return cmp;
			}

			cmp = v1.getVersionId().compareTo(v2.getVersionId());
			if (cmp != 0) {
				return cmp;
			}

			cmp = theO1.getName().compareTo(theO2.getName());
			return cmp;
		}
	}

}
