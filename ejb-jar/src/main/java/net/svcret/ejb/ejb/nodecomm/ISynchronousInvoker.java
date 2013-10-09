package net.svcret.ejb.ejb.nodecomm;

import static net.svcret.ejb.ejb.nodecomm.SynchronousListener.*;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

@WebService(targetNamespace = SVC_NS, name = SVC_SVCNAME)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface ISynchronousInvoker {

	@WebMethod(operationName = METHOD_FRS_NAME)
	@SOAPBinding(parameterStyle = ParameterStyle.BARE, style = Style.DOCUMENT, use = Use.LITERAL)
	@WebResult(name = METHOD_FRS_RESP, targetNamespace = SVC_NS)
	public abstract FlushRuntimeStatusResponse flushRuntimeStatus(@WebParam(targetNamespace=SVC_NS, name="FlushRuntimeStatusRequest") FlushRuntimeStatusRequest theRequest);

	@WebMethod(operationName = METHOD_FTL_NAME)
	@SOAPBinding(parameterStyle = ParameterStyle.BARE, style = Style.DOCUMENT, use = Use.LITERAL)
	@WebResult(name = METHOD_FTL_RESP, targetNamespace = SVC_NS)
	public abstract FlushTransactionLogsResponse flushTransactionLogs(@WebParam(targetNamespace=SVC_NS, name="FlushTransactionLogsRequest") FlushTransactionLogsRequest theRequest);


}