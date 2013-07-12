package net.svcret.ejb.ejb;

import static net.svcret.ejb.ejb.AdminServiceBean.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.imageio.ImageIO;

import net.svcret.admin.shared.model.TimeRange;
import net.svcret.ejb.api.IChartingServiceBean;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.ejb.AdminServiceBean.IWithStats;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersInvocationStats;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.util.Validate;

import org.rrd4j.ConsolFun;
import org.rrd4j.data.LinearInterpolator;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

@Stateless
public class ChartingServiceBean implements IChartingServiceBean {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ChartingServiceBean.class);

	@EJB
	private IDao myDao;

	@EJB
	private IRuntimeStatus myStatus;
	
	@EJB
	private IConfigService myConfig;

	@Override
	public byte[] renderLatencyGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, ProcessingException {
		ourLog.info("Rendering latency graph for service version {}", theServiceVersionPid);

		final ArrayList<Integer> invCount60Min = new ArrayList<Integer>();
		final ArrayList<Long> time60min = new ArrayList<Long>();
		final ArrayList<Long> timestamps = new ArrayList<Long>();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		for (PersServiceVersionMethod nextMethod : svcVer.getMethods()) {
			 doWithStatsByMinute(myConfig.getConfig(), theRange, myStatus, nextMethod, new IWithStats() {
				@Override
				public void withStats(int theIndex, BasePersInvocationStats theStats) {
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
	public byte[] renderPayloadSizeGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, ProcessingException {
		ourLog.info("Rendering payload size graph for service version {}", theServiceVersionPid);

		final List<Integer> invCount = new ArrayList<Integer>();
		final List<Long> totalSuccessReqBytes = new ArrayList<Long>();
		final List<Long> totalSuccessRespBytes = new ArrayList<Long>();
		final List<Long> timestamps = new ArrayList<Long>();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		for (PersServiceVersionMethod nextMethod : svcVer.getMethods()) {
			doWithStatsByMinute(myConfig.getConfig(), theRange, myStatus, nextMethod, new IWithStats() {
				@Override
				public void withStats(int theIndex, BasePersInvocationStats theStats) {
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

	private byte[] renderPayloadSize(List<Integer> theInvCount, double[] theAvgSuccessReqBytes, double[] theAvgSuccessRespBytes, String theIntervalDesc, List<Long> theTimestampsMillis) throws IOException {
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

		LinearInterpolator reqPlot = new LinearInterpolator(timestamps, theAvgSuccessReqBytes);
		graphDef.datasource("req", reqPlot);
		graphDef.line("req", Color.RED, "Requests", 2.0f);
		graphDef.gprint("req", ConsolFun.AVERAGE, "Average Size %.1f\\l");

		LinearInterpolator respPlot = new LinearInterpolator(timestamps, theAvgSuccessRespBytes);
		graphDef.datasource("resp", respPlot);
		graphDef.line("resp", Color.GREEN, "Responses", 2.0f);
		graphDef.gprint("resp", ConsolFun.AVERAGE, "Average Size %.1f\\l");

		return render(graphDef);

	}

	@Override
	public byte[] renderUsageGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, ProcessingException {
		ourLog.info("Rendering latency graph for service version {}", theServiceVersionPid);

		final List<Double> invCount = new ArrayList<Double>();
		final List<Double> invCountFault = new ArrayList<Double>();
		final List<Double> invCountFail = new ArrayList<Double>();
		final List<Double> invCountSecurityFail = new ArrayList<Double>();
		final List<Long> timestamps = new ArrayList<Long>();

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		for (PersServiceVersionMethod nextMethod : svcVer.getMethods()) {
			doWithStatsByMinute(myConfig.getConfig(), theRange, myStatus, nextMethod, new IWithStats() {
				@Override
				public void withStats(int theIndex, BasePersInvocationStats theStats) {
					growToSizeDouble(invCount, theIndex);
					growToSizeDouble(invCountFault, theIndex);
					growToSizeDouble(invCountFail, theIndex);
					growToSizeDouble(invCountSecurityFail, theIndex);
					growToSizeLong(timestamps, theIndex);

					double numMinutes = theStats.getPk().getInterval().numMinutes();
					
					invCount.set(theIndex, invCount.get(theIndex)+( theStats.getSuccessInvocationCount()/numMinutes));
					invCountFault.set(theIndex, invCountFault.get(theIndex)+ (theStats.getFaultInvocationCount()/numMinutes));
					invCountFail.set(theIndex, invCountFail.get(theIndex)+ (theStats.getFailInvocationCount()/numMinutes));
					invCountSecurityFail.set(theIndex, invCountSecurityFail.get(theIndex)+ (theStats.getServerSecurityFailures()/numMinutes));
					timestamps.set(theIndex, theStats.getPk().getStartTime().getTime());
				}
			});
						
		}

		return renderUsage(invCount, invCountFault, invCountFail, invCountSecurityFail, "Calls / Min", timestamps);
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

		for (int i = 0; i < theNumCalls.size(); i++) {
			timestamps[i] = theTimestampsMillis.get(i) / 1000;
			avgValues[i] = theNumCalls.get(i) > 0 ? (theTotalTime.get(i) / theNumCalls.get(i)) : 0;
		}

		LinearInterpolator avgPlot = new LinearInterpolator(timestamps, avgValues);
		graphDef.datasource("avg", avgPlot);
		graphDef.setTimeSpan(timestamps);
		graphDef.setVerticalLabel("Latency (millis/call)");
		graphDef.setTextAntiAliasing(true);

		graphDef.line("avg", Color.BLACK, "Average (ms)", 2);
		graphDef.gprint("avg", ConsolFun.AVERAGE, "average %.1f");

		return render(graphDef);
	}

	private byte[] renderUsage(List<Double> theInvCount, List<Double> theInvCountFault, List<Double> theInvCountFail, List<Double> theInvCountSecurityFail, String theIntervalDesc, List<Long> theTimestampsMillis) throws IOException {
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
		graphDef.area("inv", Color.GREEN, "Successful Calls");
		graphDef.gprint("inv", ConsolFun.AVERAGE, "Average %.1f\\l");

		LinearInterpolator avgFaultPlot = new LinearInterpolator(timestamps, toDoublesFromDoubles(theInvCountFault));
		graphDef.datasource("invfault", avgFaultPlot);
		graphDef.stack("invfault", Color.BLUE, "Faults");
		graphDef.gprint("invfault", ConsolFun.AVERAGE, "Average %.1f\\l");

		LinearInterpolator avgFailPlot = new LinearInterpolator(timestamps, toDoublesFromDoubles(theInvCountFail));
		graphDef.datasource("invfail", avgFailPlot);
		graphDef.stack("invfail", Color.GRAY, "Fails");
		graphDef.gprint("invfail", ConsolFun.AVERAGE, "Average %.1f\\l");

		LinearInterpolator avgSecurityFailPlot = new LinearInterpolator(timestamps, toDoublesFromDoubles(theInvCountSecurityFail));
		graphDef.datasource("invSecurityFail", avgSecurityFailPlot);
		graphDef.stack("invSecurityFail", Color.RED, "SecurityFails");
		graphDef.gprint("invSecurityFail", ConsolFun.AVERAGE, "Average %.1f\\l");

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

	private double[] toDoubles(List<Integer> theInvCount) {
		double[] retVal = new double[theInvCount.size()];
		int i = 0;
		for (int next : theInvCount) {
			retVal[i++] = next;
		}
		return retVal;
	}

	public static void main(String[] args) throws IOException, ProcessingException {
//		if (true) {
//			
//			ChartingServiceBean c = new ChartingServiceBean();
//			TimeRange range = new TimeRange();
//			range.setRange(TimeRangeEnum.ONE_MONTH);
//			c.renderLatencyGraphForServiceVersion(0, range);
//			
//			return;
//		}
		
		
		System.setProperty("java.awt.headless", "true");
		int num = 60 * 2;
		long startTime = System.currentTimeMillis() - (num * 60 * 1000);
		int interval = 60 * 1000;

		List<Integer> numCalls = new ArrayList<Integer>();
		List<Long> totalTime = new ArrayList<Long>();
		List<Long> timestamps = new ArrayList<Long>();

		long lastTime = startTime;
		for (int i = 0; i < num; i++) {
			numCalls.add((int) (100.0 * Math.random()));
			totalTime.add((long) (1000.0 * Math.random()));
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
			calls.add( (1000.0 * Math.random()));
			callsFault.add( (1000.0 * Math.random()));
			callsFail.add( (1000.0 * Math.random()));
			callsSecFail.add( (1000.0 * Math.random()));
		}

		bytes = new ChartingServiceBean().renderUsage(calls, callsFault, callsFail, callsSecFail, "Calls/Min", timestamps);
		fos = new FileOutputStream("saved2.png", false);
		fos.write(bytes);
		fos.close();

	}

	@Override
	public byte[] renderUserMethodGraphForUser(long theUserPid) {
		ourLog.info("Rendering latency graph for user {}", theUserPid);

		final List<Double> invCount = new ArrayList<Double>();
		final List<Double> invCountFault = new ArrayList<Double>();
		final List<Double> invCountFail = new ArrayList<Double>();
		final List<Double> invCountSecurityFail = new ArrayList<Double>();
		final List<Long> timestamps = new ArrayList<Long>();

//		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
//		for (PersServiceVersionMethod nextMethod : svcVer.getMethods()) {
//			doWithStatsByMinute(myConfig.getConfig(), theRange, myStatus, nextMethod, new IWithStats() {
//				@Override
//				public void withStats(int theIndex, BasePersInvocationStats theStats) {
//					growToSizeDouble(invCount, theIndex);
//					growToSizeDouble(invCountFault, theIndex);
//					growToSizeDouble(invCountFail, theIndex);
//					growToSizeDouble(invCountSecurityFail, theIndex);
//					growToSizeLong(timestamps, theIndex);
//
//					double numMinutes = theStats.getPk().getInterval().numMinutes();
//					
//					invCount.set(theIndex, invCount.get(theIndex)+( theStats.getSuccessInvocationCount()/numMinutes));
//					invCountFault.set(theIndex, invCountFault.get(theIndex)+ (theStats.getFaultInvocationCount()/numMinutes));
//					invCountFail.set(theIndex, invCountFail.get(theIndex)+ (theStats.getFailInvocationCount()/numMinutes));
//					invCountSecurityFail.set(theIndex, invCountSecurityFail.get(theIndex)+ (theStats.getServerSecurityFailures()/numMinutes));
//					timestamps.set(theIndex, theStats.getPk().getStartTime().getTime());
//				}
//			});
//						
//		}
//
//		return renderUsage(invCount, invCountFault, invCountFail, invCountSecurityFail, "Calls / Min", timestamps);

		return null;
	}

}
