package net.svcret.ejb.invoker.virtual;

import java.util.List;
import java.util.Set;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.springframework.beans.factory.annotation.Autowired;

import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IServiceOrchestrator;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.ex.InvalidRequestException;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.InvocationRequestFailedException;
import net.svcret.ejb.ex.InvocationResponseFailedException;
import net.svcret.ejb.invoker.BaseServiceInvoker;
import net.svcret.ejb.invoker.IServiceInvoker;
import net.svcret.ejb.invoker.soap.InvocationFailedException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.virtual.PersServiceVersionVirtual;

import com.google.common.annotations.VisibleForTesting;

@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Singleton
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ServiceInvokerVirtual extends BaseServiceInvoker implements IServiceInvokerVirtual {

	@EJB
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
