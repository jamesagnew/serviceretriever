package net.svcret.ejb.ejb.nodecomm;

import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.ITransactionLogger;

@WebService(targetNamespace = SynchronousListener.SVC_NS, serviceName = SynchronousListener.SVC_SVCNAME, portName = SynchronousListener.SVC_PORT)
public class SynchronousListener implements ISynchronousInvoker {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SynchronousListener.class);
	
	static final String METHOD_FRS_NAME = "flushRuntimeStatus";
	static final String METHOD_FRS_RESP = "FlushRumtimeStatusResponse";
	static final String METHOD_FTL_NAME = "flushTransactionLogs";
	static final String METHOD_FTL_RESP = "FlushTransactionLogsResponse";
	static final String SVC_NS = "net:svcret:ejb:nodecomm";
	static final String SVC_PORT = "NodeCommPort";
	static final String SVC_SVCNAME = "ServiceRetriever_NodeCommService";

	@EJB
	private IRuntimeStatus myRuntimeStatusService;

	@EJB
	private ITransactionLogger myTransactionLoggerService;

	/* (non-Javadoc)
	 * @see net.svcret.ejb.ejb.nodecomm.ISynchronousInvoker#flushRuntimeStatus()
	 */
	@Override
	@WebMethod(operationName = METHOD_FRS_NAME)
	@SOAPBinding(parameterStyle = ParameterStyle.BARE, style = Style.DOCUMENT, use = Use.LITERAL)
	@WebResult(name = METHOD_FRS_RESP, targetNamespace = SVC_NS)
	public void flushRuntimeStatus() {
		try {
			myRuntimeStatusService.flushStatus();
		} catch (Exception e) {
			ourLog.error("Failed to flush status", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.svcret.ejb.ejb.nodecomm.ISynchronousInvoker#flushTransactionLogs()
	 */
	@Override
	@WebMethod(operationName = METHOD_FTL_NAME)
	@SOAPBinding(parameterStyle = ParameterStyle.BARE, style = Style.DOCUMENT, use = Use.LITERAL)
	@WebResult(name = METHOD_FTL_RESP, targetNamespace = SVC_NS)
	public void flushTransactionLogs() {
		try {
			myTransactionLoggerService.flush();
		} catch (Exception e) {
			ourLog.error("Failed to flush status", e);
		}
	}

}
