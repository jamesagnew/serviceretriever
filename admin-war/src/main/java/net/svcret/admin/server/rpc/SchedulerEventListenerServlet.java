package net.svcret.admin.server.rpc;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.IScheduler;
import net.svcret.ejb.log.ITransactionLogger;

@WebServlet(urlPatterns = { "/doSecondaryFlush" })
public class SchedulerEventListenerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SchedulerEventListenerServlet.class);

	@EJB
	private IScheduler myScheduler;

	@EJB
	private IRuntimeStatus myStatsSvc;
	
	@EJB
	private ITransactionLogger myTransactionLoggerSvc;
	
	@Override
	protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
		long start = System.currentTimeMillis();

		if ("flushstats".equals(theReq.getAttribute("action"))) {
			myStatsSvc.flushStatus();
		} else if ("flushrecentmessages".equals(theReq.getAttribute("action"))) {
			myTransactionLoggerSvc.flush();
		} else {
			throw new ServletException("Unknown action: " + theReq.getAttribute("action"));
		}
		
		long delay = System.currentTimeMillis() - start;

		theResp.setContentType("text/plain");
		String msg = "Completed flush in " + delay + "ms";
		theResp.getWriter().append(msg);
		ourLog.info(msg);

	}

}
