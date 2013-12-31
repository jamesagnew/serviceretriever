package net.svcret.ejb.invoker.virtual;

import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.InvocationRequestFailedException;
import net.svcret.ejb.ex.InvocationResponseFailedException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.invoker.BaseServiceInvoker;
import net.svcret.ejb.invoker.IServiceInvoker;
import net.svcret.ejb.invoker.soap.InvocationFailedException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.virtual.PersServiceVersionVirtual;

import com.google.common.annotations.VisibleForTesting;

@Stateless
public class ServiceInvokerVirtual extends BaseServiceInvoker implements IServiceInvokerVirtual {

	@EJB
	public IServiceOrchestrator myOrchestrator;

	@Override
	public String obscureMessageForLogs(BasePersServiceVersion theServiceDefinition, String theMessage, Set<String> theElementNamesToRedact) throws InvocationFailedDueToInternalErrorException {
		return determineInvoker(theServiceDefinition).obscureMessageForLogs(theServiceDefinition, theMessage, theElementNamesToRedact);
	}

	@Override
	public SrBeanProcessedRequest processInvocation(SrBeanIncomingRequest theRequest, BasePersServiceVersion theServiceDefinition) throws UnknownRequestException, InvocationRequestFailedException,
			InvocationFailedException {
		return determineInvoker(theServiceDefinition).processInvocation(theRequest, determineTarget(theServiceDefinition));
	}

	@Override
	public SrBeanProcessedResponse processInvocationResponse(BasePersServiceVersion theServiceDefinition, SrBeanIncomingResponse theResponse) throws InvocationResponseFailedException,
			InvocationFailedDueToInternalErrorException {
		return determineInvoker(theServiceDefinition).processInvocationResponse(determineTarget(theServiceDefinition), theResponse);
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

	private BasePersServiceVersion determineTarget(BasePersServiceVersion theServiceDefinition) {
		return ((PersServiceVersionVirtual)theServiceDefinition).getTarget();
	}

}
