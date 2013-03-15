package ca.uhn.sail.proxy.api;

import java.io.IOException;
import java.io.Reader;

import ca.uhn.sail.proxy.ex.InternalErrorException;
import ca.uhn.sail.proxy.ex.ProcessingException;
import ca.uhn.sail.proxy.ex.UnknownRequestException;
import ca.uhn.sail.proxy.model.entity.BasePersServiceVersion;

/**
 * 
 * @param <T> The service definition type
 */
public interface IServiceInvoker<T extends BasePersServiceVersion> {

	/**
	 * 
	 * @param theServiceDefinition The service definition which this request corresponds to
	 * @param theRequestType The HTTP request type
	 * @param thePath The request URL portion which is the path
	 * @param theQuery The request URL portion which comes after the path
	 * @param theReader A reader which can be used to stream in any request content
	 * @param theUrlPool The URL Pool to pass to the HTTP client which will process service requests as they are proxied
	 * @return
	 * @throws InternalErrorException
	 * @throws UnknownRequestException
	 * @throws IOException
	 * @throws ProcessingException
	 */
	InvocationResultsBean processInvocation(T theServiceDefinition, RequestType theRequestType, String thePath, String theQuery, Reader theReader) throws InternalErrorException, UnknownRequestException, IOException, ProcessingException; 
	
	InvocationResponseResultsBean processInvocationResponse(HttpResponseBean theResponse) throws ProcessingException;

	IResponseValidator provideInvocationResponseValidator();
	
}

