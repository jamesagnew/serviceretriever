package net.svcret.core.invoker;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.svcret.admin.api.ProcessingException;
import net.svcret.core.api.IResponseValidator;
import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanIncomingResponse;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.api.SrBeanProcessedResponse;
import net.svcret.core.ex.InvalidRequestException;
import net.svcret.core.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.core.ex.InvocationRequestFailedException;
import net.svcret.core.ex.InvocationResponseFailedException;
import net.svcret.core.invoker.soap.InvocationFailedException;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersBaseServerAuth;
import net.svcret.core.model.entity.PersHttpClientConfig;

/**
 * 
 */
public interface IServiceInvoker {

	/**
	 * @param theServiceVersion
	 * @param theIncomingHeaders
	 *            The headers which arrived on the incoming request
	 * @return The headers which should be passed to the backing URL requests
	 */
	Map<String, List<String>> createBackingRequestHeadersForMethodInvocation(BasePersServiceVersion theServiceVersion, Map<String, List<String>> theIncomingHeaders);

	/**
	 * Given a URL to a service definition page (the type of page is dependent on the type of service, but the assumption is that this is something like a SOAP WSDL URL) loads a definition for that
	 * service.
	 * 
	 * @param theUrl
	 * @return
	 * @throws ProcessingException
	 */
	BasePersServiceVersion introspectServiceFromUrl(PersHttpClientConfig theHttpConfig, String theUrl) throws ProcessingException;

	/**
	 * @see BasePersServiceVersion#getObscureRequestElementsInLog()
	 * @see BasePersServiceVersion#getObscureResponseElementsInLog()
	 */
	String obscureMessageForLogs(BasePersServiceVersion theServiceDefinition, String theMessage, Set<String> theElementNamesToRedact) throws InvocationFailedDueToInternalErrorException;

	/**
	 * 
	 * @param theServiceDefinition
	 *            The service definition which this request corresponds to
	 * @param theRequestType
	 *            The HTTP request type
	 * @param thePath
	 *            The request URL portion which is the path
	 * @param theQuery
	 *            The request URL portion which comes after the path
	 * @param theContentType
	 *            The content type (may be null)
	 * @param theReader
	 *            A reader which can be used to stream in any request content
	 * @param theUrlPool
	 *            The URL Pool to pass to the HTTP client which will process service requests as they are proxied
	 * @return
	 * @throws InternalErrorException
	 * @throws InvalidRequestException
	 * @throws IOException
	 * @throws ProcessingException
	 * @throws InvocationFailedException
	 */
	SrBeanProcessedRequest processInvocation(SrBeanIncomingRequest theRequest, BasePersServiceVersion theServiceDefinition) throws InvalidRequestException, InvocationRequestFailedException,
			InvocationFailedException;

	SrBeanProcessedResponse processInvocationResponse(BasePersServiceVersion theServiceDefinition, SrBeanIncomingRequest theRequest, SrBeanIncomingResponse theResponse) throws InvocationResponseFailedException,
			InvocationFailedDueToInternalErrorException;

	IResponseValidator provideInvocationResponseValidator(BasePersServiceVersion theServiceDefinition);

	List<PersBaseServerAuth<?, ?>> provideServerAuthorizationModules(BasePersServiceVersion theServiceVersion);

}
