package net.svcret.admin.server.rpc;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.svcret.ejb.api.IScheduler;

@WebServlet(urlPatterns = { "/doSecondaryFlush" })
public class SchedulerEventListenerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SchedulerEventListenerServlet.class);

	@EJB
	private IScheduler myScheduler;

	@Override
	protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
		long start = System.currentTimeMillis();

		ourLog.debug("Received flush request");
		
		myScheduler.flushInMemoryStatisticsAndTransactionsSecondary();

		long delay = System.currentTimeMillis() - start;

		theResp.setContentType("text/plain");
		theResp.getWriter().append("Completed flush in " + delay + "ms");

	}

}
