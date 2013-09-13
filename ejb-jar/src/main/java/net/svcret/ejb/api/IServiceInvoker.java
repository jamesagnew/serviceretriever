package net.svcret.ejb.api;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.svcret.ejb.ejb.soap.InvocationFailedException;
import net.svcret.ejb.ex.InvocationFailedDueToInternalErrorException;
import net.svcret.ejb.ex.InvocationRequestFailedException;
import net.svcret.ejb.ex.InvocationResponseFailedException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;

/**
 * 
 * @param <T>
 *            The service definition type
 */
public interface IServiceInvoker {

	/**
	 * @param theIncomingHeaders
	 *            The headers which arrived on the incoming request
	 * @return The headers which should be passed to the backing URL requests
	 */
	Map<String, List<String>> createBackingRequestHeadersForMethodInvocation(Map<String, List<String>> theIncomingHeaders);

	/**
	 * Given a URL to a service definition page (the type of page is dependent on the type of service, but the assumption is that this is something like a SOAP WSDL URL) loads a definition for that
	 * service.
	 * 
	 * @param theUrl
	 * @return
	 * @throws ProcessingException
	 */
	BasePersServiceVersion introspectServiceFromUrl(String theUrl) throws ProcessingException;

	/**
	 * @see BasePersServiceVersion#getObscureRequestElementsInLog()
	 * @see BasePersServiceVersion#getObscureResponseElementsInLog()
	 */
	String obscureMessageForLogs(String theMessage, Set<String> theElementNamesToRedact) throws InvocationFailedDueToInternalErrorException;

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
	 * @throws UnknownRequestException
	 * @throws IOException
	 * @throws ProcessingException
	 * @throws InvocationFailedException
	 */
	InvocationResultsBean processInvocation(BasePersServiceVersion theServiceDefinition, RequestType theRequestType, String thePath, String theQuery, String theContentType, Reader theReader)
			throws UnknownRequestException, InvocationRequestFailedException, InvocationFailedException;

	InvocationResponseResultsBean processInvocationResponse(HttpResponseBean theResponse) throws InvocationResponseFailedException, InvocationFailedDueToInternalErrorException;

	IResponseValidator provideInvocationResponseValidator();

}
