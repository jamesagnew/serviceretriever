package net.svcret.core.invoker.crud;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.core.api.IDao;
import net.svcret.core.api.IResponseValidator;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.dao.NullTransactionTemplateForUnitTests;
import net.svcret.core.ex.InvalidRequestException;
import net.svcret.core.ex.InvocationRequestFailedException;
import net.svcret.core.invoker.BaseServiceInvoker;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.crud.PersServiceVersionRest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.annotations.VisibleForTesting;

@Service
public class ServiceInvokerRest extends BaseServiceInvoker implements IServiceInvokerRest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceInvokerRest.class);

	@Autowired
	private IDao myDao;

	@Autowired
	private IServiceRegistry myServiceRegistry;

	@Autowired
	private JpaTransactionManager myTxManager;

	private TransactionTemplate myTxTemplate;

	@PostConstruct
	public void setup() {
		myTxTemplate = new TransactionTemplate(myTxManager);
		myTxTemplate.setReadOnly(true);
	}

	public ServiceInvokerRest() {
		// nothing
	}

	@VisibleForTesting
	public void setDaoForUnitTest(IDao theDao) {
		myDao = theDao;
	}

	@VisibleForTesting
	public void setServiceRegistryForUnitTest(IServiceRegistry theServiceRegistry) {
		myServiceRegistry = theServiceRegistry;
	}

	@Override
	public SrBeanProcessedRequest processInvocation(SrBeanIncomingRequest theRequest, final BasePersServiceVersion theServiceDefinition) throws InvalidRequestException,
			InvocationRequestFailedException {
		PersServiceVersionRest svc = (PersServiceVersionRest) theServiceDefinition;

		final String messageType = "method";

		String contentType = theRequest.getContentType();
		contentType = StringUtils.defaultString(contentType);
		int semicolonIndex = contentType.indexOf(';');
		if (semicolonIndex > -1) {
			contentType = contentType.substring(0, semicolonIndex).trim();
		}

		String message;
		try {
			message = org.apache.commons.io.IOUtils.toString(theRequest.getInputReader());
		} catch (IOException e) {
			throw new InvocationRequestFailedException(e);
		}

		PersMethod method = theServiceDefinition.getMethod(messageType);
		if (method == null) {

			method = myTxTemplate.execute(new TransactionCallback<PersMethod>() {
				@Override
				public PersMethod doInTransaction(TransactionStatus status) {
					try {
						ourLog.info("Creating new method '{}' for service version {}", messageType, theServiceDefinition.getPid());
						BasePersServiceVersion dbSvcVer = myDao.getServiceVersionByPid(theServiceDefinition.getPid());
						dbSvcVer.getOrCreateAndAddMethodWithName(messageType);

						try {
							dbSvcVer = myServiceRegistry.saveServiceVersion(dbSvcVer);
						} catch (ProcessingException e) {
							ourLog.error("Failed to auto-create method", e);
							throw new InvocationRequestFailedException(e, "Failed to auto-create method '" + messageType + "'. Error was: " + e.getMessage());
						} catch (UnexpectedFailureException e) {
							ourLog.error("Failed to auto-create method", e);
							throw new InvocationRequestFailedException(e, "Failed to auto-create method '" + messageType + "'. Error was: " + e.getMessage());
						}

						PersMethod method1 = dbSvcVer.getMethod(messageType);
						ourLog.info("Created new method '{}' and got PID {}", messageType, method1.getPid());
						return method1;
					} catch (Exception e) {
						throw new Error(e);
					}
				}
			});

		}

		SrBeanProcessedRequest retVal = new SrBeanProcessedRequest();
		retVal.setResultMethod(method, message, contentType);

		String suffix = null;
		if (theRequest.getPath().startsWith(svc.getDefaultProxyPath())) {
			suffix = theRequest.getPath().substring(svc.getDefaultProxyPath().length());
		} else if (svc.getExplicitProxyPath() != null && theRequest.getPath().startsWith(svc.getExplicitProxyPath())) {
			suffix = theRequest.getPath().substring(svc.getExplicitProxyPath().length());
		} else {
			throw new Error("Path " + theRequest.getPath() + " doesnt match explicit path " + svc.getExplicitProxyPath() + " or default Path " + svc.getExplicitProxyPath());
		}

		retVal.setUrlSuffix(suffix + theRequest.getQuery());
		
		return retVal;
	}

	@Override
	public SrBeanProcessedResponse processInvocationResponse(BasePersServiceVersion theServiceDefinition, SrBeanIncomingResponse theResponse) {

		String responseBody = theResponse.getBody();

		SrBeanProcessedResponse retVal = new SrBeanProcessedResponse();
		retVal.setResponseBody(responseBody);
		retVal.setResponseContentType(theResponse.getContentType());
		retVal.setResponseHeaders(new HashMap<String, List<String>>());
		retVal.setResponseType(ResponseTypeEnum.SUCCESS);

		return retVal;
	}

	@Override
	public IResponseValidator provideInvocationResponseValidator(BasePersServiceVersion theServiceDefinition) {
		return new ResponseValidatorRest((PersServiceVersionRest) theServiceDefinition);
	}

	@Override
	public String obscureMessageForLogs(BasePersServiceVersion theServiceDefinition, String theMessage, Set<String> theElementNamesToRedact) {
		// TODO: implement
		return theMessage;
	}

	@VisibleForTesting
	public void setTransactionTemplateForUnitTest() {
		myTxTemplate = new NullTransactionTemplateForUnitTests();
	}

}
