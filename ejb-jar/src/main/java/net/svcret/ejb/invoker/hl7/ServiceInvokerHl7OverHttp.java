package net.svcret.ejb.invoker.hl7;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.api.SrBeanProcessedResponse;
import net.svcret.ejb.ex.InvalidRequestException;
import net.svcret.ejb.ex.InvalidRequestException.IssueEnum;
import net.svcret.ejb.ex.InvocationRequestFailedException;
import net.svcret.ejb.ex.InvocationResponseFailedException;
import net.svcret.ejb.invoker.BaseServiceInvoker;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersMethod;
import net.svcret.ejb.model.entity.hl7.PersServiceVersionHl7OverHttp;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.preparser.PreParser;

import com.google.common.annotations.VisibleForTesting;

@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Singleton
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ServiceInvokerHl7OverHttp extends BaseServiceInvoker implements IServiceInvokerHl7OverHttp {

	private static final String CT_XML = "application/hl7-v2+xml";
	private static final String CT_ER7 = "application/hl7-v2";

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceInvokerHl7OverHttp.class);

	@EJB
	@Autowired
	private IDao myDao;

	@EJB
	@Autowired
	private IServiceRegistry myServiceRegistry;

	public ServiceInvokerHl7OverHttp() {
		// DefaultHapiContext hapiContext = new DefaultHapiContext();
		// hapiContext.setValidationContext(new ValidationContextImpl());
		// GenericParser parser = hapiContext.getGenericParser();
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
	public SrBeanProcessedRequest processInvocation(SrBeanIncomingRequest theRequest, BasePersServiceVersion theServiceDefinition) throws InvalidRequestException, InvocationRequestFailedException {
		PersServiceVersionHl7OverHttp svc = (PersServiceVersionHl7OverHttp)theServiceDefinition;
		
		if (theRequest.getRequestType() != RequestType.POST) {
			throw new InvalidRequestException(IssueEnum.UNSUPPORTED_ACTION, theRequest.getRequestType().name(), "Requests to HL7 over HTTP services must use HTTP POST.");
		}

		String contentType = theRequest.getContentType();
		contentType = StringUtils.defaultString(contentType);
		int semicolonIndex = contentType.indexOf(';');
		if (semicolonIndex > -1) {
			contentType = contentType.substring(0, semicolonIndex).trim();
		}
		
		if (CT_ER7.equals(contentType)) {
			ourLog.debug("Content type is {}", contentType);
		} else if (CT_XML.equals(contentType)) {
			ourLog.debug("Content type is {}", contentType);
		} else {
			throw new InvalidRequestException(IssueEnum.INVALID_REQUEST_CONTENT_TYPE, contentType, "Requests to HL7 over HTTP services must use a valid content-type: " + CT_ER7 + " or " + CT_XML);
		}

		String message;
		try {
			message = org.apache.commons.io.IOUtils.toString(theRequest.getInputReader());
		} catch (IOException e) {
			throw new InvocationRequestFailedException(e);
		}

		String messageType;
		try {
			String[] messageTypeParts = PreParser.getFields(message, "MSH-9-1", "MSH-9-2");
			messageType = svc.getMethodNameTemplate().replace("${messageType}", messageTypeParts[0]).replace("${messageVersion}", messageTypeParts[1]);
		} catch (HL7Exception e) {
			throw new InvocationRequestFailedException(e,"Failed to parse message, error was: " + e.getMessage());
		}

		PersMethod method = theServiceDefinition.getMethod(messageType);
		if (method == null) {
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
			method = dbSvcVer.getMethod(messageType);
			ourLog.info("Created new method '{}' and got PID {}", messageType, method.getPid());
		}

		SrBeanProcessedRequest retVal = new SrBeanProcessedRequest();
		retVal.setResultMethod(method, message, contentType);
		
		return retVal;
	}

	@Override
	public SrBeanProcessedResponse processInvocationResponse(BasePersServiceVersion theServiceDefinition, SrBeanIncomingResponse theResponse) throws InvocationResponseFailedException  {
		
		String responseBody = theResponse.getBody();
		String responseCode;
		try {
			responseCode = PreParser.getFields(responseBody, "MSA-1")[0];
		} catch (HL7Exception e) {
			throw new InvocationResponseFailedException(e, "Failed to parse response: "+e.getMessage(), theResponse);
		}
		
		SrBeanProcessedResponse retVal=new SrBeanProcessedResponse();
		retVal.setResponseBody(responseBody);
		retVal.setResponseContentType(theResponse.getContentType());
		retVal.setResponseHeaders(new HashMap<String, List<String>>());

		ourLog.trace("Successful acknowledgement code: {}", ourLog);
		if ("AA".equals(responseCode) || "CA".equals(responseCode)) {
			retVal.setResponseType(ResponseTypeEnum.SUCCESS);
		}else {
			retVal.setResponseFaultCode(responseCode);
			retVal.setResponseFaultDescription("Non successful acknowledgement code (MSA-2): " + responseCode);
			retVal.setResponseType(ResponseTypeEnum.FAULT);
		}
		
		return retVal;
	}

	@Override
	public IResponseValidator provideInvocationResponseValidator(BasePersServiceVersion theServiceDefinition) {
		return new Hl7OverHttpResponseValidator();
	}

	@Override
	public String obscureMessageForLogs(BasePersServiceVersion theServiceDefinition, String theMessage, Set<String> theElementNamesToRedact)  {
		// TODO: implement
		return theMessage;
	}

}
