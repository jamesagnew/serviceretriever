package net.svcret.ejb.invoker.hl7;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.lang3.StringUtils;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanIncomingResponse;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.ex.InvocationRequestFailedException;
import net.svcret.ejb.ex.InvocationResponseFailedException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.invoker.BaseServiceInvoker;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.hl7.PersServiceVersionHl7OverHttp;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.preparser.PreParser;

import com.google.common.annotations.VisibleForTesting;

@Stateless
public class ServiceInvokerHl7OverHttp extends BaseServiceInvoker implements IServiceInvokerHl7OverHttp {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ServiceInvokerHl7OverHttp.class);

	@EJB
	private IDao myDao;

	@EJB
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
	public InvocationResultsBean processInvocation(SrBeanIncomingRequest theRequest, BasePersServiceVersion theServiceDefinition) throws UnknownRequestException, InvocationRequestFailedException {
		PersServiceVersionHl7OverHttp svc = (PersServiceVersionHl7OverHttp)theServiceDefinition;
		
		if (theRequest.getRequestType() != RequestType.POST) {
			throw new UnknownRequestException(theRequest.getPath(), "HL7 over HTTP service at " + theRequest.getPath() + " requires all requests to be of type POST");
		}

		String contentType = theRequest.getContentType();
		contentType = StringUtils.defaultString(contentType);
		int semicolonIndex = contentType.indexOf(';');
		if (semicolonIndex > -1) {
			contentType = contentType.substring(0, semicolonIndex).trim();
		}
		
		if ("application/hl7-v2".equals(contentType)) {
			ourLog.debug("Content type is {}", contentType);
		} else if ("application/hl7-v2+xml".equals(contentType)) {
			ourLog.debug("Content type is {}", contentType);
		} else {
			throw new UnknownRequestException(theRequest.getPath(),"HL7 over HTTP service cannot accept content type: " + contentType);
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

		PersServiceVersionMethod method = theServiceDefinition.getMethod(messageType);
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

		InvocationResultsBean retVal = new InvocationResultsBean();
		retVal.setResultMethod(method, message, contentType);
		
		return retVal;
	}

	@Override
	public InvocationResponseResultsBean processInvocationResponse(BasePersServiceVersion theServiceDefinition, SrBeanIncomingResponse theResponse) throws InvocationResponseFailedException  {
		
		String responseBody = theResponse.getBody();
		String responseCode;
		try {
			responseCode = PreParser.getFields(responseBody, "MSA-1")[0];
		} catch (HL7Exception e) {
			throw new InvocationResponseFailedException(e, "Failed to parse response: "+e.getMessage(), theResponse);
		}
		
		InvocationResponseResultsBean retVal=new InvocationResponseResultsBean();
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
