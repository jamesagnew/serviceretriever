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
import net.svcret.ejb.invoker.IServiceInvoker;
import net.svcret.ejb.invoker.soap.InvocationFailedException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.virtual.PersServiceVersionVirtual;

public class ServiceInvokerVirtual extends BaseServiceInvoker implements IServiceInvokerVirtual {

	public IServiceOrchestrator myOrchestrator;
	

	@Override
	public BasePersServiceVersion introspectServiceFromUrl(String theUrl) throws ProcessingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String obscureMessageForLogs(BasePersServiceVersion theServiceDefinition, String theMessage, Set<String> theElementNamesToRedact) throws InvocationFailedDueToInternalErrorException {
		return determineInvoker(theServiceDefinition).obscureMessageForLogs(theServiceDefinition, theMessage, theElementNamesToRedact);
	}

	private IServiceInvoker determineInvoker(BasePersServiceVersion theServiceDefinition) {
		PersServiceVersionVirtual svcVer = (PersServiceVersionVirtual) theServiceDefinition;
		return myOrchestrator.getServiceInvoker(svcVer.getTarget());
	}

	@Override
	public InvocationResultsBean processInvocation(BasePersServiceVersion theServiceDefinition, RequestType theRequestType, String thePath, String theQuery, String theContentType, Reader theReader)
			throws UnknownRequestException, InvocationRequestFailedException, InvocationFailedException {
		return determineInvoker(theServiceDefinition).processInvocation(theServiceDefinition, theRequestType, thePath, theQuery, theContentType, theReader);
	}

	@Override
	public InvocationResponseResultsBean processInvocationResponse(BasePersServiceVersion theServiceDefinition, HttpResponseBean theResponse) throws InvocationResponseFailedException, InvocationFailedDueToInternalErrorException {
		return determineInvoker(theServiceDefinition).processInvocationResponse(theServiceDefinition, theResponse);
	}

	@Override
	public IResponseValidator provideInvocationResponseValidator(BasePersServiceVersion theServiceDefinition) {
		return determineInvoker(theServiceDefinition).provideInvocationResponseValidator(theServiceDefinition);
	}

}
