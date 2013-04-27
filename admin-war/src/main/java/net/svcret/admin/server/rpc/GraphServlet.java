package net.svcret.admin.server.rpc;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.svcret.admin.shared.util.ChartParams;
import net.svcret.admin.shared.util.ChartTypeEnum;
import net.svcret.ejb.api.IChartingServiceBean;

@WebServlet(urlPatterns = { "/graph.png" })
public class GraphServlet extends HttpServlet {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(GraphServlet.class);

	private static final long serialVersionUID = 1L;

	@EJB
	private IChartingServiceBean myChartSvc;

	private byte[] renderLatency(HttpServletRequest theReq) throws IOException {
		long pid = getPid(theReq);
		
		ourLog.info("Rendering latency graph for service version {}", pid);

		byte[] graph = myChartSvc.renderLatencyGraphForServiceVersion(pid);

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

		byte[] bytes = null;
		switch (chartType) {
		case LATENCY:
			bytes = renderLatency(theReq);
			break;
		case USAGE:
			bytes = renderUsage(theReq);
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

	private byte[] renderUsage(HttpServletRequest theReq) throws IOException {
		long pid = getPid(theReq);
		
		ourLog.info("Rendering usage graph for service version {}", pid);

		byte[] graph = myChartSvc.renderUsageGraphForServiceVersion(pid);

		return graph;
	}

}
