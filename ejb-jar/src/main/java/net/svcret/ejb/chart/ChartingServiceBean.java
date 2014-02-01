package net.svcret.ejb.chart;

import static net.svcret.ejb.admin.AdminServiceBean.addToInt;
import static net.svcret.ejb.admin.AdminServiceBean.addToLong;
import static net.svcret.ejb.admin.AdminServiceBean.doWithStatsByMinute;
import static net.svcret.ejb.admin.AdminServiceBean.doWithStatsSupportFindInterval;
import static net.svcret.ejb.admin.AdminServiceBean.doWithStatsSupportIncrement;
import static net.svcret.ejb.admin.AdminServiceBean.growToSizeDouble;
import static net.svcret.ejb.admin.AdminServiceBean.growToSizeInt;
import static net.svcret.ejb.admin.AdminServiceBean.growToSizeLong;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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

import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.TimeRange;
import net.svcret.ejb.admin.AdminServiceBean.IWithStats;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.api.IScheduler;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.ejb.model.entity.PersInvocationMethodUserStats;
import net.svcret.ejb.model.entity.PersMethod;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.util.Validate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.rrd4j.ConsolFun;
import org.rrd4j.data.LinearInterpolator;
import org.rrd4j.data.Plottable;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

import com.google.common.annotations.VisibleForTesting;

/**
 * Bean which generates graphs with statistics about things relating to SR.
 * 
 * Note on testing this class: The unit tests actually generate graph files, so
 * they can be used to try things out.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class ChartingServiceBean implements IChartingServiceBean {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ChartingServiceBean.class);

	private static float[] RAINBOW_BASE_HSB;

	private static Color RAINBOW_COLOUR_0;

	private static final Double ZERO_DOUBLE = 0.0;

	static {
		RAINBOW_COLOUR_0 = Color.decode("#8A56E2");

		RAINBOW_BASE_HSB = new float[3];
		Color.RGBtoHSB(RAINBOW_COLOUR_0.getRed(), RAINBOW_COLOUR_0.getGreen(), RAINBOW_COLOUR_0.getBlue(), RAINBOW_BASE_HSB);
	}

	@EJB
	private IConfigService myConfig;

	@EJB
	private IDao myDao;

	@EJB
	private IScheduler myScheduler;

	@EJB
	private IRuntimeStatusQueryLocal myStatus;

	private Color createStackColour(int theNum, int theIndex) {
		if (theNum == 0) {
			return Color.black;
		}

		float[] hsbVals = provideNewRainbowHsb();

		double step = 1.0 / theNum;
		for (int i = 0; i < theIndex; i++) {
			hsbVals[0] += step;
			while (hsbVals[0] > 1.0) {
				hsbVals[0] -= 1.0;
			}
		}

		int next = Color.HSBtoRGB(hsbVals[0], hsbVals[1], hsbVals[2]);
		return (new Color(next));
	}

	private List<Color> createStackColours(int theNum) {
		ArrayList<Color> retVal = new ArrayList<Color>();
		if (theNum == 0) {
			return retVal;
		}

		retVal.add(RAINBOW_COLOUR_0);
		float[] hsbVals = provideNewRainbowHsb();

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

	private float[] provideNewRainbowHsb() {
		float[] retVal = new float[3];
		retVal[0] = RAINBOW_BASE_HSB[0];
		retVal[1] = RAINBOW_BASE_HSB[1];
		retVal[2] = RAINBOW_BASE_HSB[2];
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

	@Override
	public byte[] renderSvcVerPayloadSizeGraph(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException {
		ourLog.info("Rendering payload size graph for service version {}", theServiceVersionPid);

		myScheduler.flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();

		final List<Integer> invCount = new ArrayList<Integer>();
		final List<Long> totalSuccessReqBytes = new ArrayList<Long>();
		final List<Long> totalSuccessRespBytes = new ArrayList<Long>();
		final List<Long> timestamps = new ArrayList<Long>();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		for (PersMethod nextMethod : svcVer.getMethods()) {
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

		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setWidth(600);
		graphDef.setHeight(200);
		graphDef.setTitle("Message Payload Size: " + svcVer.getService().getDomain().getDomainId() + " / " + svcVer.getService().getServiceId() + " / " + svcVer.getVersionId());

		long[] timestamps1 = new long[invCount.size()];

		double prevReq = 0.0;
		double prevResp = 0.0;
		for (int i = 0; i < invCount.size(); i++) {
			timestamps1[i] = timestamps.get(i) / 1000;
			prevReq = avgSuccessReqSize[i] > 0 ? avgSuccessReqSize[i] : prevReq;
			avgSuccessReqSize[i] = prevReq;
			prevResp = avgSuccessRespSize[i] > 0 ? avgSuccessRespSize[i] : prevResp;
			avgSuccessRespSize[i] = prevResp;
		}

		for (int i = 0; i < avgSuccessReqSize.length; i++) {
		}

		graphDef.setTimeSpan(timestamps1);
		graphDef.setVerticalLabel("Bytes / Message");
		graphDef.setTextAntiAliasing(true);

		LinearInterpolator reqPlot = new LinearInterpolator(timestamps1, avgSuccessReqSize);
		graphDef.datasource("req", reqPlot);
		graphDef.line("req", Color.RED, "Requests  ", 2.0f);
		graphDef.gprint("req", ConsolFun.AVERAGE, "Average Size %8.1f   ");
		graphDef.gprint("req", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("req", ConsolFun.MAX, "Max %8.1f\\l");

		LinearInterpolator respPlot = new LinearInterpolator(timestamps1, avgSuccessRespSize);
		graphDef.datasource("resp", respPlot);
		graphDef.line("resp", Color.GREEN, "Responses ", 2.0f);
		graphDef.gprint("resp", ConsolFun.AVERAGE, "Average Size %8.1f   ");
		graphDef.gprint("resp", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("resp", ConsolFun.MAX, "Max %8.1f\\l");

		return render(graphDef);
	}

	@Override
	public byte[] renderSvcVerLatencyMethodGraph(long theSvcVerPid, TimeRange theRange, boolean theIndividualMethod) throws UnexpectedFailureException, IOException {
		ourLog.info("Rendering user method graph for Service Version {}", theSvcVerPid);

		myScheduler.flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theSvcVerPid);

		/*
		 * Init the graph
		 */

		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setWidth(600);
		graphDef.setHeight(200);

		graphDef.setTitle("Backing Service Latency: " + svcVer.getService().getDomain().getDomainId() + " / " + svcVer.getService().getServiceId() + " / " + svcVer.getVersionId());
		graphDef.setVerticalLabel("Milliseconds / Call");
		graphDef.setTextAntiAliasing(true);
		graphDef.setMinValue(0.0);

		/*
		 * Loop through each method and load the latency stats
		 */
		final List<String> names = new ArrayList<String>();
		final List<List<Long>> latencyLists = new ArrayList<List<Long>>();
		final List<Long> timestamps = new ArrayList<Long>();

		for (PersMethod nextMethod : svcVer.getMethods()) {
			if (BaseDtoServiceVersion.METHOD_NAME_UNKNOWN.equals(nextMethod.getName())) {
				continue;
			}

			final List<Long> latencyMin;
			if (theIndividualMethod) {
				names.add(nextMethod.getName());
				latencyMin = new ArrayList<Long>();
				latencyLists.add(latencyMin);
			} else {
				if (names.isEmpty()) {
					names.add("All Methods");
					latencyMin = new ArrayList<Long>();
					latencyLists.add(latencyMin);
				} else {
					latencyMin = latencyLists.get(0);
				}
			}

			doWithStatsByMinute(myConfig.getConfig(), theRange, myStatus, nextMethod, new IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>() {
				@Override
				public void withStats(int theIndex, PersInvocationMethodSvcverStats theStats) {
					growToSizeLong(latencyMin, theIndex);
					growToSizeLong(timestamps, theIndex);
					long latency = theStats.getSuccessInvocationTotalTime() > 0 ? theStats.getSuccessInvocationTotalTime() / theStats.getSuccessInvocationCount() : 0;
					latencyMin.set(theIndex, addToLong(latencyMin.get(theIndex), latency));
					timestamps.set(theIndex, theStats.getPk().getStartTime().getTime());
				}
			});

		}

		/*
		 * Set time span
		 */
		long[] graphTimestamps = new long[timestamps.size()];
		for (int i = 0; i < timestamps.size(); i++) {
			graphTimestamps[i] = timestamps.get(i) / 1000;
		}
		graphDef.setTimeSpan(graphTimestamps);

		/*
		 * Figure out the longest name
		 */
		int longestName = 0;
		for (String next : names) {
			longestName = Math.max(longestName, next.length());
		}

		/*
		 * Straighten
		 */
		int numWithValues = 0;
		List<Boolean> hasValuesList = new ArrayList<Boolean>();
		for (List<Long> nextList : latencyLists) {
			boolean hasValues = false;
			for (int i = 0; i < nextList.size(); i++) {
				long l = nextList.get(i);
				if (l == 0) {
					if (i > 0 && nextList.get(i - 1) > 0) {
						nextList.set(i, nextList.get(i - 1));
					}
				} else {
					hasValues = true;
				}
			}
			hasValuesList.add(hasValues);
			if (hasValues) {
				numWithValues++;
			}
		}

		/*
		 * Figure out colours
		 */
		List<Color> colours = new ArrayList<Color>();
		int colourIndex = 0;
		for (int i = 0; i < hasValuesList.size(); i++) {
			if (hasValuesList.get(i)) {
				colours.add(createStackColour(numWithValues, colourIndex++));
			} else {
				colours.add(Color.black);
			}
		}

		/*
		 * Add the lines to the graph
		 */

		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			List<Long> latencyMin = latencyLists.get(i);

			Plottable avgPlot = new LinearInterpolator(graphTimestamps, toDoublesFromLongs(latencyMin));
			String srcName = "inv" + i;
			graphDef.datasource(srcName, avgPlot);

			graphDef.line(srcName, colours.get(i), " " + StringUtils.rightPad(name, longestName), 2);
			if (hasValuesList.get(i)) {
				graphDef.gprint(srcName, ConsolFun.AVERAGE, "Avg %5.1f ");
				graphDef.gprint(srcName, ConsolFun.MIN, "Min %5.1f ");
				graphDef.gprint(srcName, ConsolFun.MAX, "Max %5.1f \\l");
			} else {
				graphDef.comment("No Invocations During This Period\\l");
			}

		}

		return render(graphDef);
	}

	@Override
	public byte[] renderSvcVerThrottlingGraph(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException {
		ourLog.info("Rendering throttling graph for service version {}", theServiceVersionPid);

		myScheduler.flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();

		final List<Long> throttleAcceptCount = new ArrayList<Long>();
		final List<Long> throttleRejectCount = new ArrayList<Long>();
		final List<Long> timestamps = new ArrayList<Long>();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		for (PersMethod nextMethod : svcVer.getMethods()) {
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

		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setWidth(600);
		graphDef.setHeight(200);
		graphDef.setTitle("Request Throttling: " + svcVer.getService().getDomain().getDomainId() + " / " + svcVer.getService().getServiceId() + " / " + svcVer.getVersionId());

		long[] timestamps1 = new long[timestamps.size()];

		for (int i = 0; i < timestamps.size(); i++) {
			timestamps1[i] = timestamps.get(i) / 1000;
		}

		graphDef.setTimeSpan(timestamps1);
		graphDef.setVerticalLabel("Throttled Calls / Min");
		graphDef.setTextAntiAliasing(true);

		LinearInterpolator avgPlot = new LinearInterpolator(timestamps1, toDoublesFromLongs(throttleAcceptCount));
		graphDef.datasource("inv", avgPlot);
		graphDef.area("inv", Color.GREEN, "Accepted (Throttled but call was allowed to proceed after delay)\\l");
		graphDef.gprint("inv", ConsolFun.AVERAGE, "Average %8.1f   ");
		graphDef.gprint("inv", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("inv", ConsolFun.MAX, "Max %8.1f\\l");

		LinearInterpolator avgFaultPlot = new LinearInterpolator(timestamps1, toDoublesFromLongs(throttleRejectCount));
		graphDef.datasource("invfault", avgFaultPlot);
		graphDef.stack("invfault", Color.BLUE, "Rejected (Throttle queue was full or not allowed)\\l");
		graphDef.gprint("invfault", ConsolFun.AVERAGE, "Average %8.1f   ");
		graphDef.gprint("invfault", ConsolFun.MIN, "Min %8.1f   ");
		graphDef.gprint("invfault", ConsolFun.MAX, "Max %8.1f\\l");

		return render(graphDef);
	}

	@Override
	public byte[] renderSvcVerUsageGraph(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException {
		ourLog.info("Rendering latency graph for service version {}", theServiceVersionPid);

		myScheduler.flushInMemoryStatisticsUnlessItHasHappenedVeryRecently();

		final List<Double> invCount = new ArrayList<Double>();
		final List<Double> invCountFault = new ArrayList<Double>();
		final List<Double> invCountFail = new ArrayList<Double>();
		final List<Double> invCountSecurityFail = new ArrayList<Double>();
		final List<Long> timestamps = new ArrayList<Long>();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		for (PersMethod nextMethod : svcVer.getMethods()) {
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

		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setWidth(600);
		graphDef.setHeight(200);
		graphDef.setTitle("Usage: " + svcVer.getService().getDomain().getDomainId() + " / " + svcVer.getService().getServiceId() + " / " + svcVer.getVersionId());

		long[] timestamps1 = new long[invCount.size()];

		for (int i = 0; i < invCount.size(); i++) {
			timestamps1[i] = timestamps.get(i) / 1000;
		}

		graphDef.setTimeSpan(timestamps1);
		graphDef.setVerticalLabel("Calls / Min");
		graphDef.setTextAntiAliasing(true);

		LinearInterpolator avgPlot = new LinearInterpolator(timestamps1, toDoublesFromDoubles(invCount));
		graphDef.datasource("inv", avgPlot);
		if (hasValues(invCount)) {
			graphDef.area("inv", Color.decode("#00C000"), StringUtils.rightPad("Successful Calls", 20));
			graphDef.gprint("inv", ConsolFun.AVERAGE, "Average %8.1f   ");
			graphDef.gprint("inv", ConsolFun.MIN, "Min %8.1f   ");
			graphDef.gprint("inv", ConsolFun.MAX, "Max %8.1f\\l");
		} else {
			graphDef.area("inv", Color.decode("#00C000"), StringUtils.rightPad("No Successful calls during this time period", 20));
		}
		
		if (hasValues(invCountFault)) {
			LinearInterpolator avgFaultPlot = new LinearInterpolator(timestamps1, toDoublesFromDoubles(invCountFault));
			graphDef.datasource("invfault", avgFaultPlot);
			graphDef.stack("invfault", Color.decode("#6060C0"), StringUtils.rightPad("Faults", 20));
			graphDef.gprint("invfault", ConsolFun.AVERAGE, "Average %8.1f   ");
			graphDef.gprint("invfault", ConsolFun.MIN, "Min %8.1f   ");
			graphDef.gprint("invfault", ConsolFun.MAX, "Max %8.1f\\l");
		} else {
			graphDef.comment("No Faults during this time period\\l");
		}

		if (hasValues(invCountFail)) {
			LinearInterpolator avgFailPlot = new LinearInterpolator(timestamps1, toDoublesFromDoubles(invCountFail));
			graphDef.datasource("invfail", avgFailPlot);
			graphDef.stack("invfail", Color.decode("#F00000"), StringUtils.rightPad("Fails", 20));
			graphDef.gprint("invfail", ConsolFun.AVERAGE, "Average %8.1f   ");
			graphDef.gprint("invfail", ConsolFun.MIN, "Min %8.1f   ");
			graphDef.gprint("invfail", ConsolFun.MAX, "Max %8.1f\\l");
		} else {
			graphDef.comment("No Failures during this time period\\l");
		}

		if (hasValues(invCountSecurityFail)) {
			LinearInterpolator avgSecurityFailPlot = new LinearInterpolator(timestamps1, toDoublesFromDoubles(invCountSecurityFail));
			graphDef.datasource("invSecurityFail", avgSecurityFailPlot);
			graphDef.stack("invSecurityFail", Color.decode("#F0A000"), StringUtils.rightPad("Security Fails", 20));
			graphDef.gprint("invSecurityFail", ConsolFun.AVERAGE, "Average %8.1f   ");
			graphDef.gprint("invSecurityFail", ConsolFun.MIN, "Min %8.1f   ");
			graphDef.gprint("invSecurityFail", ConsolFun.MAX, "Max %8.1f\\l");
		} else {
			graphDef.comment("No Security Failures during this time period\\l");
		}

		return render(graphDef);
	}

	private boolean hasValues(List<Double> theInvCountFault) {
		for (Double next : theInvCountFault) {
			if (next != null && next > 0.0) {
				return true;
			}
		}
		return false;
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
		final Map<Long, PersMethod> pidToMethod = new HashMap<Long, PersMethod>();
		for (long nextPid : methods.keySet()) {
			PersMethod method = myDao.getServiceVersionMethodByPid(nextPid);
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
				PersMethod m1 = pidToMethod.get(theO1);
				PersMethod m2 = pidToMethod.get(theO2);
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
		for (PersMethod next : pidToMethod.values()) {
			longestName = Math.max(longestName, next.getName().length());
		}

		// Draw the methods
		String previousServiceDesc = null;
		List<Color> colours = createStackColours(pids.size());
		for (int i = 0; i < pids.size(); i++) {
			Long nextPid = pids.get(i);
			PersMethod nextMethod = pidToMethod.get(nextPid);
			List<Double> values = methods.get(nextPid);

			LinearInterpolator avgPlot = new LinearInterpolator(graphTimestamps, toDoublesFromDoubles(values));
			String srcName = "inv" + i;
			graphDef.datasource(srcName, avgPlot);
			String methodDesc = nextMethod.getServiceVersion().getService().getServiceId() + " " + nextMethod.getServiceVersion().getVersionId();
			if (!StringUtils.equals(previousServiceDesc, methodDesc)) {
				graphDef.comment(methodDesc + "\\l");
				previousServiceDesc = methodDesc;
			}

			double sumDoubles = sumDoubles(values);
			double pct = (sumDoubles / grandTotal);

			if (i == 0) {
				graphDef.area(srcName, colours.get(i), " " + StringUtils.rightPad(nextMethod.getName(), longestName));
			} else {
				graphDef.stack(srcName, colours.get(i), " " + StringUtils.rightPad(nextMethod.getName(), longestName));
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

	private double sumDoubles(List<Double> theValues) {
		double retVal = 0.0;
		for (Double next : theValues) {
			retVal += next;
		}
		return retVal;
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

	private static final class MethodComparator implements Comparator<PersMethod> {
		public static final MethodComparator INSTANCE = new MethodComparator();

		@Override
		public int compare(PersMethod theO1, PersMethod theO2) {
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
