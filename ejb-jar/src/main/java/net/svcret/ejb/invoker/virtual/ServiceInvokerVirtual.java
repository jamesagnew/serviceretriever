package net.svcret.ejb.invoker.virtual;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.InvocationRequestFailedException;
import net.svcret.ejb.ex.InvocationResponseFailedException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.invoker.soap.InvocationFailedException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;

public class ServiceInvokerVirtual implements IServiceInvokerVirtual {

	@Override
	public Map<String, List<String>> createBackingRequestHeadersForMethodInvocation(Map<String, List<String>> theIncomingHeaders) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasePersServiceVersion introspectServiceFromUrl(String theUrl) throws ProcessingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String obscureMessageForLogs(String theMessage, Set<String> theElementNamesToRedact) throws InvocationFailedDueToInternalErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InvocationResultsBean processInvocation(BasePersServiceVersion theServiceDefinition, RequestType theRequestType, String thePath, String theQuery, String theContentType, Reader theReader)
			throws UnknownRequestException, InvocationRequestFailedException, InvocationFailedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InvocationResponseResultsBean processInvocationResponse(HttpResponseBean theResponse) throws InvocationResponseFailedException, InvocationFailedDueToInternalErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResponseValidator provideInvocationResponseValidator() {
		// TODO Auto-generated method stub
		return null;
	}

}
