package net.svcret.ejb.ejb.jsonrpc;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.IResponseValidator;
import net.svcret.ejb.api.IServiceInvokerJsonRpc20;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.jsonrpc.NamedParameterJsonRpcCredentialGrabber;
import net.svcret.ejb.model.entity.jsonrpc.NamedParameterJsonRpcServerAuth;
import net.svcret.ejb.model.entity.jsonrpc.PersServiceVersionJsonRpc20;
import net.svcret.ejb.util.Validate;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

@Stateless()
public class JsonRpc20ServiceInvoker implements IServiceInvokerJsonRpc20 {

	static final String TOKEN_ID = "id";
	static final String TOKEN_PARAMS = "params";
	static final String TOKEN_METHOD = "method";

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(JsonRpc20ServiceInvoker.class);

	static final String TOKEN_JSONRPC = "jsonrpc";
	static final String TOKEN_RESULT = "result";
	static final String TOKEN_ERROR = "error";

	public static void consumeEqually(IJsonReader theJsonReader, IJsonWriter theJsonWriter) throws IOException, ProcessingException {
		int objectDepth = 0;
		int arrayDepth = 0;

		JsonToken next = theJsonReader.peek();
		switch (next) {
		case NULL:
			theJsonReader.nextNull();
			theJsonWriter.nullValue();
			return;
		case BEGIN_OBJECT:
			theJsonReader.beginObject();
			theJsonWriter.beginObject();
			objectDepth++;
			break;
		case BEGIN_ARRAY:
			theJsonReader.beginArray();
			theJsonWriter.beginArray();
			arrayDepth++;
			break;
		case BOOLEAN:
			theJsonWriter.value(theJsonReader.nextBoolean());
			return;
		case NUMBER:
			consumeNumberEqually(theJsonReader, theJsonWriter);
			return;
		case STRING:
			theJsonWriter.value(theJsonReader.nextString());
			break;
		case NAME:
		case END_ARRAY:
		case END_DOCUMENT:
		case END_OBJECT:
		default:
			throw new ProcessingException("Found token of " + next + " in an unexpected place");
		}

		do {
			next = theJsonReader.peek();
			switch (next) {
			case NULL:
				theJsonReader.nextNull();
				theJsonWriter.nullValue();
				break;
			case BEGIN_ARRAY:
				theJsonReader.beginArray();
				theJsonWriter.beginArray();
				arrayDepth++;
				break;
			case BEGIN_OBJECT:
				theJsonReader.beginObject();
				theJsonWriter.beginObject();
				objectDepth++;
				break;
			case BOOLEAN:
				theJsonWriter.value(theJsonReader.nextBoolean());
				break;
			case END_ARRAY:
				theJsonReader.endArray();
				theJsonWriter.endArray();
				arrayDepth--;
				break;
			case END_DOCUMENT:
				break;
			case END_OBJECT:
				theJsonReader.endObject();
				theJsonWriter.endObject();
				objectDepth--;
				break;
			case NAME:
				String nextName = theJsonReader.nextName();
				theJsonWriter.name(nextName);
				break;
			case NUMBER:
				consumeNumberEqually(theJsonReader, theJsonWriter);
				break;
			case STRING:
				String nextString = theJsonReader.nextString();
				theJsonWriter.value(nextString);
				break;
			}
		} while ((objectDepth > 0 || arrayDepth > 0));

	}

	private static void consumeNumberEqually(IJsonReader theJsonReader, IJsonWriter theJsonWriter) throws IOException, ProcessingException {
		String value = theJsonReader.nextString();
		if (value.contains(".")) {
			try {
				theJsonWriter.value(Double.parseDouble(value));
			} catch (NumberFormatException e) {
				throw new ProcessingException("Invalid double value found in JSON: " + value);
			}
		} else {
			try {
				theJsonWriter.value(Long.parseLong(value));
			} catch (NumberFormatException e) {
				throw new ProcessingException("Invalid long value found in JSON: " + value);
			}
		}
	}

	public static void consumeEqually(JsonReader theJsonReader) throws IOException, ProcessingException {
		int objectDepth = 0;
		int arrayDepth = 0;

		JsonToken next = theJsonReader.peek();
		switch (next) {
		case BEGIN_OBJECT:
			theJsonReader.beginObject();
			objectDepth++;
			break;
		case BEGIN_ARRAY:
			theJsonReader.beginArray();
			arrayDepth++;
			break;
		case NULL:
			theJsonReader.nextNull();
			return;
		case BOOLEAN:
			theJsonReader.nextBoolean();
			return;
		case END_ARRAY:
		case END_DOCUMENT:
		case END_OBJECT:
		case NAME:
			throw new IllegalStateException("Not expected here");
		case NUMBER:
			theJsonReader.nextString(); // avoid long/double problems by just using string
			return;
		case STRING:
			theJsonReader.nextString();
			return;
		}

		do {
			next = theJsonReader.peek();
			switch (next) {
			case NULL:
				theJsonReader.nextNull();
				break;
			case BEGIN_ARRAY:
				theJsonReader.beginArray();
				arrayDepth++;
				break;
			case BEGIN_OBJECT:
				theJsonReader.beginObject();
				objectDepth++;
				break;
			case BOOLEAN:
				theJsonReader.nextBoolean();
				break;
			case END_ARRAY:
				theJsonReader.endArray();
				arrayDepth--;
				break;
			case END_DOCUMENT:
				break;
			case END_OBJECT:
				theJsonReader.endObject();
				objectDepth--;
				break;
			case NAME:
				theJsonReader.nextName();
				break;
			case NUMBER:
				theJsonReader.nextString(); // avoid long/double problems by just using string
				break;
			case STRING:
				theJsonReader.nextString();
				break;
			}
		} while ((objectDepth > 0 || arrayDepth > 0));

	}

	@Override
	public InvocationResultsBean processInvocation(PersServiceVersionJsonRpc20 theServiceDefinition, RequestType theRequestType, String thePath, String theQuery, String theContentType,Reader theReader)
			throws ProcessingException {
		Validate.notNull(theReader, "Reader");
		if (theRequestType != RequestType.POST) {
			throw new ProcessingException("This service requires all requests to be of type HTTP POST");
		}

		InvocationResultsBean retVal;
		try {
			retVal = doProcessInvocation(theServiceDefinition, theReader);
		} catch (IOException e) {
			throw new ProcessingException(e);
		}

		return retVal;
	}

	private InvocationResultsBean doProcessInvocation(PersServiceVersionJsonRpc20 theServiceDefinition, Reader theReader) throws IOException, ProcessingException {
		InvocationResultsBean retVal = new InvocationResultsBean();

		// Writer
		StringWriter stringWriter = new StringWriter();
		IJsonWriter jsonWriter = new MyJsonWriter(stringWriter);

		// Reader
		Reader inputReader;

		String inputMessage = IOUtils.toString(theReader);
		ourLog.trace("Input message:\n{}", inputMessage);
		inputReader = new StringReader(inputMessage);

		IJsonReader jsonReader = new MyJsonReader(inputReader);

		/*
		 * Create security pipeline if needed
		 */
		for (PersBaseServerAuth<?, ?> next : theServiceDefinition.getServerAuths()) {
			if (next instanceof NamedParameterJsonRpcServerAuth) {
				NamedParameterJsonRpcCredentialGrabber grabber = ((NamedParameterJsonRpcServerAuth) next).newCredentialGrabber(jsonReader, jsonWriter);
				jsonWriter = grabber.getWrappedWriter();
				jsonReader = grabber.getWrappedReader();
				retVal.addCredentials(next, grabber);
			} else {
				jsonWriter.close();
				jsonReader.close();
				throw new InternalErrorException("Don't know how to handle server auth of type: " + next);
			}
		}

		jsonWriter.setLenient(true);
		jsonWriter.setSerializeNulls(true);
		jsonWriter.setIndent("  ");

		jsonReader.setLenient(true);

		String method = null;

		jsonReader.beginObject();
		jsonWriter.beginObject();

		while (jsonReader.hasNext()) {

			String nextName = jsonReader.nextName();
			jsonWriter.name(nextName);

			if (TOKEN_JSONRPC.equals(nextName)) {

				String rpcVal = jsonReader.nextString();
				jsonWriter.value(rpcVal);
				ourLog.debug("JsonRpc version in request: {}", rpcVal);

			} else if (TOKEN_METHOD.equals(nextName)) {

				method = jsonReader.nextString();
				jsonWriter.value(method);
				ourLog.debug("JsonRpc method name: {}", method);

			} else if (TOKEN_PARAMS.equals(nextName)) {

				jsonReader.beginJsonRpcParams();
				consumeEqually(jsonReader, jsonWriter);
				jsonReader.endJsonRpcParams();

			} else if (TOKEN_ID.equals(nextName)) {

				String requestId = jsonReader.nextString();
				jsonWriter.value(requestId);
				ourLog.debug("JsonRpc request ID: {}", requestId);

			}

		}

		jsonReader.endObject();
		jsonWriter.endObject();

		jsonReader.close();
		jsonWriter.close();

		String requestBody = stringWriter.toString();
		String contentType = "application/json";
		Map<String, String> headers = new HashMap<String, String>();
		PersServiceVersionMethod methodDef = theServiceDefinition.getMethod(method);
		if (methodDef == null) {
			throw new ProcessingException("Unknown method \"" + method + "\" for Service \"" + theServiceDefinition.getService().getServiceName() + "\"");
		}

		retVal.setResultMethod(methodDef, requestBody, contentType, headers);
		return retVal;
	}

	@Override
	public InvocationResponseResultsBean processInvocationResponse(HttpResponseBean theResponse) throws ProcessingException {
		InvocationResponseResultsBean retVal = new InvocationResponseResultsBean();
		retVal.setResponseHeaders(theResponse.getHeaders());

		String body = theResponse.getBody();
		StringReader reader = new StringReader(body);

		ourLog.trace("JSON Response: {}", body);

		JsonReader jsonReader = new JsonReader(reader);
		try {
			jsonReader.beginObject();

			while (jsonReader.hasNext()) {

				String nextName = jsonReader.nextName();

				if (JsonRpc20ServiceInvoker.TOKEN_JSONRPC.equals(nextName)) {

					String rpcVal = jsonReader.nextString();
					ourLog.debug("JSON-RPC version in response: {}", rpcVal);

				} else if (JsonRpc20ServiceInvoker.TOKEN_RESULT.equals(nextName)) {

					JsonRpc20ServiceInvoker.consumeEqually(jsonReader);

				} else if (JsonRpc20ServiceInvoker.TOKEN_ERROR.equals(nextName)) {

					ourLog.debug("Response is fault");

					Gson gson = new GsonBuilder().create();
					JsonErrorType error = gson.fromJson(jsonReader, JsonErrorType.class);
					assert error != null;

					retVal.setResponseType(ResponseTypeEnum.FAULT);
					retVal.setResponseFaultCode(Integer.toString(error.getCode()));
					retVal.setResponseFaultDescription(error.getMessage());

				} else if (JsonRpc20ServiceInvoker.TOKEN_ID.equals(nextName)) {

					String requestId = jsonReader.nextString();
					ourLog.debug("JsonRpc request ID: {}", requestId);

				}

			}

		} catch (IOException e) {
			throw new ProcessingException(e);
		} finally {
			IOUtils.closeQuietly(jsonReader);
		}

		retVal.setResponseBody(body);
		retVal.setResponseContentType("application/json");

		if (retVal.getResponseType() == null) {
			retVal.setResponseType(ResponseTypeEnum.SUCCESS);
		}

		return retVal;
	}

	@Override
	public IResponseValidator provideInvocationResponseValidator() {
		return new JsonRpc20ResponseValidator();
	}

	@Override
	public PersServiceVersionJsonRpc20 introspectServiceFromUrl(String theUrl) throws ProcessingException {
		throw new UnsupportedOperationException();
	}

}
