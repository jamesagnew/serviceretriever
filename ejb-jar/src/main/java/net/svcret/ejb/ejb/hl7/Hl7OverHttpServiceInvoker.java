package net.svcret.ejb.ejb.hl7;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJB;

import com.google.common.annotations.VisibleForTesting;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IServiceInvokerHl7OverHttp;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.hl7.PersServiceVersionHl7OverHttp;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.preparser.PreParser;

public class Hl7OverHttpServiceInvoker implements IServiceInvokerHl7OverHttp {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(Hl7OverHttpServiceInvoker.class);

	@EJB
	private IDao myDao;

	@EJB
	private IServiceRegistry myServiceRegistry;

	public Hl7OverHttpServiceInvoker() {
		// DefaultHapiContext hapiContext = new DefaultHapiContext();
		// hapiContext.setValidationContext(new ValidationContextImpl());
		// GenericParser parser = hapiContext.getGenericParser();
	}

	@VisibleForTesting
	void setDaoForUnitTest(IDao theDao) {
		myDao = theDao;
	}

	@VisibleForTesting
	void setServiceRegistryForUnitTest(IServiceRegistry theServiceRegistry) {
		myServiceRegistry = theServiceRegistry;
	}

	@Override
	public PersServiceVersionHl7OverHttp introspectServiceFromUrl(String theUrl) throws ProcessingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InvocationResultsBean processInvocation(PersServiceVersionHl7OverHttp theServiceDefinition, RequestType theRequestType, String thePath, String theQuery, String theContentType,
			Reader theReader) throws ProcessingException, UnknownRequestException {

		if (theRequestType != RequestType.POST) {
			throw new ProcessingException("HL7 over HTTP service at " + thePath + " requires all requests to be of type POST");
		}

		if (theContentType.equals("application/hl7-v2")) {
			ourLog.debug("Content type is {}", theContentType);
		} else if (theContentType.equals("application/hl7-v2+xml")) {
			ourLog.debug("Content type is {}", theContentType);
		} else {
			throw new ProcessingException("HL7 over HTTP service cannot accept content type: " + theContentType);
		}

		String message;
		try {
			message = org.apache.commons.io.IOUtils.toString(theReader);
		} catch (IOException e) {
			throw new ProcessingException(e);
		}

		String messageType;
		try {
			String[] messageTypeParts = PreParser.getFields(message, "MSH-9-1", "MSH-9-2");
			messageType = theServiceDefinition.getMethodNameTemplate().replace("${messageType}", messageTypeParts[0]).replace("${messageVersion}", messageTypeParts[1]);
		} catch (HL7Exception e) {
			throw new ProcessingException("Failed to parse message, error was: " + e.getMessage(), e);
		}

		PersServiceVersionMethod method = theServiceDefinition.getMethod(messageType);
		if (method == null) {
			ourLog.info("Creating new method '{}' for service version {}", messageType, theServiceDefinition.getPid());
			BasePersServiceVersion dbSvcVer = myDao.getServiceVersionByPid(theServiceDefinition.getPid());
			dbSvcVer.getOrCreateAndAddMethodWithName(messageType);
			dbSvcVer = myServiceRegistry.saveServiceVersion(dbSvcVer);
			method = dbSvcVer.getMethod(messageType);
			ourLog.info("Created new method '{}' and got PID {}", messageType, method.getPid());
		}

		InvocationResultsBean retVal = new InvocationResultsBean();
		retVal.setResultMethod(method, message, theContentType, new HashMap<String, String>());

		return retVal;
	}

	@Override
	public InvocationResponseResultsBean processInvocationResponse(HttpResponseBean theResponse) throws ProcessingException {
		
		String responseBody = theResponse.getBody();
		String responseCode;
		try {
			responseCode = PreParser.getFields(responseBody, "MSA-1")[0];
		} catch (HL7Exception e) {
			throw new ProcessingException("Failed to parse response: "+e.getMessage(), e);
		}
		
		InvocationResponseResultsBean retVal=new InvocationResponseResultsBean();
		retVal.setResponseBody(responseBody);
		retVal.setResponseContentType(theResponse.getContentType());
		
		ourLog.trace("Successful acknowledgement code: {}", ourLog);
		if ("AA".equals(responseCode) || "CA".equals(responseCode)) {
			retVal.setResponseType(ResponseTypeEnum.SUCCESS);
		}else {
			retVal.setResponseFaultCode(responseCode);
			retVal.setResponseFaultDescription("Non successful acknowledgement code (MSA-2): " + responseCode);
			retVal.setResponseType(ResponseTypeEnum.FAULT);
		}
		
		retVal.setResponseHeaders(new HashMap<String, List<String>>());

		return retVal;
	}

	@Override
	public IResponseValidator provideInvocationResponseValidator() {
		return new Hl7OverHttpResponseValidator();
	}

}
