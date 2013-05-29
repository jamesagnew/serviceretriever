package net.svcret.admin.server.rpc;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.svcret.admin.shared.model.TimeRange;
import net.svcret.admin.shared.model.TimeRangeEnum;
import net.svcret.admin.shared.util.ChartParams;
import net.svcret.admin.shared.util.ChartTypeEnum;
import net.svcret.ejb.api.IChartingServiceBean;
import net.svcret.ejb.ex.ProcessingException;

@WebServlet(urlPatterns = { "/graph.png" })
public class GraphServlet extends HttpServlet {
	
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(GraphServlet.class);

	private static final long serialVersionUID = 1L;

	@EJB
	private IChartingServiceBean myChartSvc;

	private byte[] renderLatency(HttpServletRequest theReq, TimeRange theRange) throws IOException, ServletException {
		long pid = getPid(theReq);
		
		ourLog.info("Rendering latency graph for service version {}", pid);

		byte[] graph;
		try {
			graph = myChartSvc.renderLatencyGraphForServiceVersion(pid, theRange);
		} catch (ProcessingException e) {
			throw new ServletException(e);
		}

		return graph;
	}

	private long getPid(HttpServletRequest theReq) {
		String pidString = theReq.getParameter(ChartParams.PID);
		long pid = Long.parseLong(pidString);
		return pid;
	}

	@Override
	protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
		String chartTypeString = theReq.getParameter(ChartParams.CHART_TYPE);
		ChartTypeEnum chartType = ChartTypeEnum.valueOf(chartTypeString);

		String rangeString = theReq.getParameter(ChartParams.RANGE);
		TimeRangeEnum timeRange = TimeRangeEnum.valueOf(rangeString);
		TimeRange range = new TimeRange();
		range.setRange(timeRange);
		
		byte[] bytes = null;
		switch (chartType) {
		case LATENCY:
			bytes = renderLatency(theReq, range);
			break;
		case USAGE:
			bytes = renderUsage(theReq, range);
			break;
		case PAYLOADSIZE:
			bytes = renderPayloadSize(theReq, range);
			break;
		}

		if (bytes == null) {
			throw new ServletException("Unknown chart type: " + chartTypeString);
		}

		theResp.setContentType("image/png");
		theResp.setContentLength(bytes.length);
		theResp.getOutputStream().write(bytes);
		theResp.getOutputStream().close();

	}

	private byte[] renderPayloadSize(HttpServletRequest theReq, TimeRange theRange) throws IOException, ServletException {
		long pid = getPid(theReq);
		
		ourLog.info("Rendering payload size graph for service version {}", pid);

		byte[] graph;
		try {
			graph = myChartSvc.renderPayloadSizeGraphForServiceVersion(pid, theRange);
		} catch (ProcessingException e) {
			throw new ServletException(e);
		}

		return graph;
	}

	private byte[] renderUsage(HttpServletRequest theReq, TimeRange theRange) throws IOException, ServletException {
		long pid = getPid(theReq);
		
		ourLog.info("Rendering usage graph for service version {}", pid);

		byte[] graph;
		try {
			graph = myChartSvc.renderUsageGraphForServiceVersion(pid, theRange);
		} catch (ProcessingException e) {
			throw new ServletException(e);
		}

		return graph;
	}

}
