package net.svcret.core.invoker.virtual;

import java.util.List;
import java.util.Set;

import net.svcret.core.api.IResponseValidator;
import net.svcret.core.api.IServiceOrchestrator;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.ex.InvalidRequestException;
import net.svcret.core.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.core.ex.InvocationRequestFailedException;
import net.svcret.core.ex.InvocationResponseFailedException;
import net.svcret.core.invoker.BaseServiceInvoker;
import net.svcret.core.invoker.IServiceInvoker;
import net.svcret.core.invoker.soap.InvocationFailedException;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersBaseServerAuth;
import net.svcret.core.model.entity.virtual.PersServiceVersionVirtual;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

@Service
public class ServiceInvokerVirtual extends BaseServiceInvoker implements IServiceInvokerVirtual {

	@Autowired
	public IServiceOrchestrator myOrchestrator;

	@Override
	public String obscureMessageForLogs(BasePersServiceVersion theServiceDefinition, String theMessage, Set<String> theElementNamesToRedact) throws InvocationFailedDueToInternalErrorException {
		return determineInvoker(theServiceDefinition).obscureMessageForLogs(theServiceDefinition, theMessage, theElementNamesToRedact);
	}

	@Override
	public SrBeanProcessedRequest processInvocation(SrBeanIncomingRequest theRequest, BasePersServiceVersion theServiceDefinition) throws InvalidRequestException, InvocationRequestFailedException,
			InvocationFailedException {
		return determineInvoker(theServiceDefinition).processInvocation(theRequest, determineTarget(theServiceDefinition));
	}

	@Override
	public SrBeanProcessedResponse processInvocationResponse(BasePersServiceVersion theServiceDefinition,SrBeanIncomingRequest theRequest, SrBeanIncomingResponse theResponse) throws InvocationResponseFailedException,
			InvocationFailedDueToInternalErrorException {
		return determineInvoker(theServiceDefinition).processInvocationResponse(determineTarget(theServiceDefinition), theRequest, theResponse);
	}

	@Override
	public IResponseValidator provideInvocationResponseValidator(BasePersServiceVersion theServiceDefinition) {
		return determineInvoker(theServiceDefinition).provideInvocationResponseValidator(theServiceDefinition);
	}

	@Override
	public List<PersBaseServerAuth<?, ?>> provideServerAuthorizationModules(BasePersServiceVersion theServiceVersion) {
		return ((PersServiceVersionVirtual)theServiceVersion).getTarget().getServerAuths();
	}

	@VisibleForTesting
	public void setOrchestratorForUnitTests(IServiceOrchestrator theOrchestrator) {
		myOrchestrator = theOrchestrator;
	}

	private IServiceInvoker determineInvoker(BasePersServiceVersion theServiceDefinition) {
		PersServiceVersionVirtual svcVer = (PersServiceVersionVirtual) theServiceDefinition;
		return myOrchestrator.getServiceInvoker(svcVer.getTarget());
	}

	private static BasePersServiceVersion determineTarget(BasePersServiceVersion theServiceDefinition) {
		return ((PersServiceVersionVirtual)theServiceDefinition).getTarget();
	}

}
