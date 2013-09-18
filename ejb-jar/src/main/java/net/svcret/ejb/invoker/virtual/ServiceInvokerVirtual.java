package net.svcret.ejb.invoker.virtual;

import java.io.Reader;
import java.util.Set;

import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.InvocationRequestFailedException;
import net.svcret.ejb.ex.InvocationResponseFailedException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.invoker.BaseServiceInvoker;
import net.svcret.ejb.invoker.soap.InvocationFailedException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;

public class ServiceInvokerVirtual extends BaseServiceInvoker implements IServiceInvokerVirtual {

	public IServiceOrchestrator myOrchestrator;
	

	@Override
	public BasePersServiceVersion introspectServiceFromUrl(String theUrl) throws ProcessingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String obscureMessageForLogs(BasePersServiceVersion theServiceDefinition, String theMessage, Set<String> theElementNamesToRedact) throws InvocationFailedDueToInternalErrorException {
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
	public InvocationResponseResultsBean processInvocationResponse(BasePersServiceVersion theServiceDefinition, HttpResponseBean theResponse) throws InvocationResponseFailedException, InvocationFailedDueToInternalErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResponseValidator provideInvocationResponseValidator(BasePersServiceVersion theServiceDefinition) {
		// TODO Auto-generated method stub
		return null;
	}

}
