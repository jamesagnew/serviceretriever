package net.svcret.ejb.ejb;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.imageio.ImageIO;

import net.svcret.ejb.api.IChartingServiceBean;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.ejb.AdminServiceBean.IWithStats;
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

	@Override
	public byte[] renderLatencyGraphForServiceVersion(long theServiceVersionPid) throws IOException {
		ourLog.info("Rendering latency graph for service version {}", theServiceVersionPid);

		int[] invCount60Min = new int[60];
		long[] time60min = new long[60];

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		long startTime = 0;
		for (PersServiceVersionMethod nextMethod : svcVer.getMethods()) {
			startTime = AdminServiceBean.extractSuccessfulInvocationInvocationTimes(invCount60Min, time60min, nextMethod, myStatus);
		}

		return renderLatency(invCount60Min, time60min, startTime, 60 * 1000);
	}

	@Override
	public byte[] renderUsageGraphForServiceVersion(long theServiceVersionPid) throws IOException {
		ourLog.info("Rendering latency graph for service version {}", theServiceVersionPid);

		final int[] invCount = new int[60];
		final int[] invCountFault = new int[60];
		final int[] invCountFail = new int[60];
		final int[] invCountSecurityFail = new int[60];

		BasePersServiceVersion svcVer = myDao.getServiceVersionByPid(theServiceVersionPid);
		long startTime = 0;
		for (PersServiceVersionMethod nextMethod : svcVer.getMethods()) {
			startTime = AdminServiceBean.doWithStats(myStatus, nextMethod, new IWithStats() {
				@Override
				public void withStats(int theIndex, BasePersInvocationStats theStats) {
					invCount[theIndex] = AdminServiceBean.addToInt(invCount[theIndex], theStats.getSuccessInvocationCount());
					invCountFault[theIndex] = AdminServiceBean.addToInt(invCountFault[theIndex], theStats.getFaultInvocationCount());
					invCountFail[theIndex] = AdminServiceBean.addToInt(invCountFail[theIndex], theStats.getFailInvocationCount());
					invCountSecurityFail[theIndex] = AdminServiceBean.addToInt(invCountSecurityFail[theIndex], theStats.getServerSecurityFailures());
				}
			}).getTime();
		}

		return renderUsage(invCount, invCountFault, invCountFail, invCountSecurityFail, startTime, 60 * 1000, "Calls / Min");
	}

	private byte[] renderUsage(int[] theInvCount, int[] theInvCountFault, int[] theInvCountFail, int[] theInvCountSecurityFail, long theStartTime, int theIntervalMillis, String theIntervalDesc) throws IOException {
		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setWidth(600);
		graphDef.setHeight(200);

		long[] timestamps = new long[theInvCount.length];

		for (int i = 0; i < theInvCount.length; i++) {
			timestamps[i] = (theStartTime + (i * theIntervalMillis)) / 1000;
		}

		graphDef.setTimeSpan(theStartTime / 1000, (theStartTime + ((theInvCount.length - 1) * theIntervalMillis)) / 1000);
		graphDef.setVerticalLabel(theIntervalDesc);
		graphDef.setTextAntiAliasing(true);

		LinearInterpolator avgPlot = new LinearInterpolator(timestamps, toDoubles(theInvCount));
		graphDef.datasource("inv", avgPlot);
		graphDef.area("inv", Color.GREEN, "Successful Calls");
		graphDef.gprint("inv", ConsolFun.AVERAGE, "Average %.1f\\l");
		
		LinearInterpolator avgFaultPlot = new LinearInterpolator(timestamps, toDoubles(theInvCountFault));
		graphDef.datasource("invfault", avgFaultPlot);
		graphDef.stack("invfault", Color.BLUE, "Faults");
		graphDef.gprint("invfault", ConsolFun.AVERAGE,"Average %.1f\\l");

		LinearInterpolator avgFailPlot = new LinearInterpolator(timestamps, toDoubles(theInvCountFail));
		graphDef.datasource("invfail", avgFailPlot);
		graphDef.stack("invfail", Color.GRAY, "Fails");
		graphDef.gprint("invfail", ConsolFun.AVERAGE, "Average %.1f\\l");

		LinearInterpolator avgSecurityFailPlot = new LinearInterpolator(timestamps, toDoubles(theInvCountSecurityFail));
		graphDef.datasource("invSecurityFail", avgSecurityFailPlot);
		graphDef.stack("invSecurityFail", Color.RED, "SecurityFails");
		graphDef.gprint("invSecurityFail", ConsolFun.AVERAGE, "Average %.1f\\l");

		return render(graphDef);
	}

	private double[] toDoubles(int[] theInvCount) {
		double[] retVal = new double[theInvCount.length];
		int i = 0;
		for (int next : theInvCount) {
			retVal[i++] = next;
		}
		return retVal;
	}

	private byte[] renderLatency(int[] theNumCalls, long[] theTotalTime, long theStartTime, long theIntervalMillis) throws IOException {
		Validate.isTrue(theNumCalls.length == theTotalTime.length);

		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setWidth(600);
		graphDef.setHeight(200);

		long[] timestamps = new long[theNumCalls.length];
		double[] avgValues = new double[theNumCalls.length];

		for (int i = 0; i < theNumCalls.length; i++) {
			timestamps[i] = (theStartTime + (i * theIntervalMillis)) / 1000; // seconds
																				// not
																				// millis
			avgValues[i] = theNumCalls[i] > 0 ? (theTotalTime[i] / theNumCalls[i]) : 0;
		}

		LinearInterpolator avgPlot = new LinearInterpolator(timestamps, avgValues);
		graphDef.datasource("avg", avgPlot);
		graphDef.setTimeSpan(theStartTime / 1000, (theStartTime + ((theNumCalls.length - 1) * theIntervalMillis)) / 1000);
		graphDef.setVerticalLabel("Latency (millis/call)");
		graphDef.setTextAntiAliasing(true);

		graphDef.line("avg", Color.BLACK, "Average (ms)", 2);
		graphDef.gprint("avg", ConsolFun.AVERAGE, "average %.1f");

		return render(graphDef);
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

	public static void main(String[] args) throws IOException {
		System.setProperty("java.awt.headless", "true");

		int num = 60;
		int[] numCalls = new int[60];
		long[] totalTime = new long[60];
		for (int i = 0; i < num; i++) {
			numCalls[i] = (int) (100.0 * Math.random());
			totalTime[i] = (long) (1000.0 * Math.random());
		}

		long startTime = System.currentTimeMillis();
		int interval = 60 * 1000;

		byte[] bytes = new ChartingServiceBean().renderLatency(numCalls, totalTime, startTime, interval);
		FileOutputStream fos = new FileOutputStream("saved.png", false);
		fos.write(bytes);
		fos.close();

		int[] calls = new int[60];
		int[] callsFault = new int[60];
		int[] callsFail = new int[60];
		int[] callsSecFail = new int[60];
		for (int i = 0; i < num; i++) {
			calls[i] = (int) (1000.0 * Math.random());
			callsFault[i] = (int) (1000.0 * Math.random());
			callsFail[i] = (int) (1000.0 * Math.random());
			callsSecFail[i] = (int) (1000.0 * Math.random());
		}

		bytes = new ChartingServiceBean().renderUsage(calls, callsFault, callsFail, callsSecFail, startTime, 60*1000, "Calls/Min");
		fos = new FileOutputStream("saved2.png", false);
		fos.write(bytes);
		fos.close();
		
	}

}
