package net.svcret.ejb.ejb.jsonrpc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.InvocationResultsBean.ResultTypeEnum;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.ejb.DefaultAnswer;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.jsonrpc.NamedParameterJsonRpcCredentialGrabber;
import net.svcret.ejb.model.entity.jsonrpc.NamedParameterJsonRpcServerAuth;
import net.svcret.ejb.model.entity.jsonrpc.PersServiceVersionJsonRpc20;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonRpc20ServiceInvokerTest {

	@Test
	public void testNamedParemeterCredentialGrabber() throws InternalErrorException, UnknownRequestException, IOException, ProcessingException {

		String request = "{\n" + "  \"jsonrpc\": \"2.0\",\n" + "  \"method\": \"getCanonicalMappings\",\n" + "  \"params\": {\n" + "    \"request\": {\n"
				+ "      \"idAuthority\": \"2.16.840.1.113883.3.59.3:736\",\n" + "      \"idExt\": \"87170\"\n" + "    },\n" + "    \"clientId\": \"mockuser\",\n"
				+ "    \"clientPass\": \"mockpass\"\n" + "  }\n" + "}";
		StringReader reader = new StringReader(request);

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();

		PersServiceVersionJsonRpc20 def = mock(PersServiceVersionJsonRpc20.class, new DefaultAnswer());
		ArrayList<PersBaseServerAuth<?, ?>> serverAuths = new ArrayList<PersBaseServerAuth<?, ?>>();
		serverAuths.add(new NamedParameterJsonRpcServerAuth("clientId", "clientPass"));
		when(def.getServerAuths()).thenReturn(serverAuths);
		RequestType reqType = RequestType.POST;
		String path = "/";
		String query = "";
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class, new DefaultAnswer());

		when(def.getMethod("getCanonicalMappings")).thenReturn(method);

		DefaultAnswer.setRunTime();
		InvocationResultsBean resp = svc.processInvocation(def, reqType, path, query, reader);

		ICredentialGrabber grabber = resp.getCredentialsInRequest(NamedParameterJsonRpcCredentialGrabber.class);
		assertEquals("mockpass", grabber.getPassword());
		assertEquals("mockuser", grabber.getUsername());

	}

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(JsonRpc20ServiceInvokerTest.class);

	
	
	/**
	 * A failing message
	 */
	@Test
	public void testNullParameterValues() throws InternalErrorException, UnknownRequestException, IOException, ProcessingException {

		String request = "{\"jsonrpc\":\"2.0\",\"method\":\"getActsByVisit\",\"params\":{\"auth\":\"UHN\",\"convertFromAppTerminology\":false,\"clientId\":\"clipdevsvc\",\"auditSourceId\":\"FORM_VIEWER\",\"visitId\":\"26200\n"
				+ "0218\",\"actToTerminologyLevel\":\"NONE\",\"convertObsToAppTerm\":false,\"statusCodes\":null,\"returnLoadedConcept\":false,\"loaded\":false,\"user\":{\"uid\":\"userId\",\"clientIp\":\"127.0.0.\n"
				+ "1\",\"attributes\":{}},\"clientPass\":\"Hospital20\",\"procCodes\":[\"1881\",\"1756\",\"1757\",\"1758\",\"5119\"]}}";

		ourLog.info("Request:\n{}", request);

		StringReader reader = new StringReader(request);

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
		PersServiceVersionJsonRpc20 def = mock(PersServiceVersionJsonRpc20.class);
		RequestType reqType = RequestType.POST;
		String path = "/";
		String query = "";
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class);

		when(def.getMethod("getActsByVisit")).thenReturn(method);
		when(def.getServerAuths()).thenReturn(new ArrayList<PersBaseServerAuth<?, ?>>());

		DefaultAnswer.setRunTime();
		InvocationResultsBean resp = svc.processInvocation(def, reqType, path, query, reader);

		Assert.assertEquals("application/json", resp.getMethodContentType());
		Assert.assertEquals(ResultTypeEnum.METHOD, resp.getResultType());
		Assert.assertSame(method, resp.getMethodDefinition());

		
	}

	/**
	 * A failing message
	 */
	@Test
	public void testPartialMessage() throws InternalErrorException, UnknownRequestException, IOException, ProcessingException {

		String request = "{\"jsonrpc\":\"2.0\",\"method\":\"getActsByVisit\",\"params\":{\"auth\":\"UHN\",\"convertFromAppTerminology\":false,\"clientId\":\"clipdevsvc\",\"auditSourceId\":\"FORM_VIEWER\",\"visitId\":\"26200\n"
				+ "0218\",\"actToTerminologyLevel\":\"NONE\",\"convertObsToAppTerm\":false,\"statusCodes\":null,\"returnLoadedConcept\":false,\"loaded\":false,\"user\":{\"uid\":\"userId\",\"clientIp\":\"127.0.0.\n"
				+ "1\",\"attributes\":{}},\"clientPass\":\"Hospital20\",\"procCodes\":[\"1881\",\"1756\",\"1757\",\"1758\",\"5119\"]}}";

		ourLog.info("Request:\n{}", request);

		for (int i = 0; i < request.length(); i++) {

			StringReader reader = new StringReader(request.substring(0, request.length() - i));

			JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
			PersServiceVersionJsonRpc20 def = mock(PersServiceVersionJsonRpc20.class, new DefaultAnswer());
			RequestType reqType = RequestType.POST;
			String path = "/";
			String query = "";
			PersServiceVersionMethod method = mock(PersServiceVersionMethod.class, new DefaultAnswer());

			when(def.getMethod("getActsByVisit")).thenReturn(method);
			when(def.getServerAuths()).thenReturn(new ArrayList<PersBaseServerAuth<?, ?>>());

			try {
				svc.processInvocation(def, reqType, path, query, reader);
			} catch (ProcessingException e) {
				// this is ok
			}
		

		}

	}

	@Test
	public void testProcessInvocationWithNumbers() throws InternalErrorException, UnknownRequestException, IOException, ProcessingException {

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
		PersServiceVersionJsonRpc20 def = mock(PersServiceVersionJsonRpc20.class, new DefaultAnswer());
		RequestType reqType = RequestType.POST;
		String path = "/";
		String query = "";
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class, new DefaultAnswer());

		when(def.getMethod("someMethod")).thenReturn(method);
		when(def.getServerAuths()).thenReturn(new ArrayList<PersBaseServerAuth<?, ?>>());

		DefaultAnswer.setRunTime();
		
		@SuppressWarnings("unused")
		InvocationResultsBean resp;

		String request = //- 
				"{ \"jsonrpc\": \"2.0\",\n" + //- 
				"  \"method\": \"someMethod\",\n" + //- 
				"  \"params\": 123\n" + //-
				"}"; //-
		resp = svc.processInvocation(def, reqType, path, query, new StringReader(request));

		request = //- 
				"{ \"jsonrpc\": \"2.0\",\n" + //- 
				"  \"method\": \"someMethod\",\n" + //- 
				"  \"params\": -123.456\n" + //-
				"}"; //-
		resp = svc.processInvocation(def, reqType, path, query, new StringReader(request));

		request = //- 
				"{ \"jsonrpc\": \"2.0\",\n" + //- 
				"  \"method\": \"someMethod\",\n" + //- 
				"  \"params\": [123.456, 876.543] \n" + //-
				"}"; //-
		resp = svc.processInvocation(def, reqType, path, query, new StringReader(request));

		request = //- 
				"{ \"jsonrpc\": \"2.0\",\n" + //- 
				"  \"method\": \"someMethod\",\n" + //- 
				"  \"params\": [123.456, 876.543, '222.222'] \n" + //-
				"}"; //-
		resp = svc.processInvocation(def, reqType, path, query, new StringReader(request));

		request = //- 
				"{ \"jsonrpc\": \"2.0\",\n" + //- 
				"  \"method\": \"someMethod\",\n" + //- 
				"  \"params\": { \"hello\" :  876.543 } \n" + //-
				"}"; //-
		resp = svc.processInvocation(def, reqType, path, query, new StringReader(request));

		
	}
	
	
	@Test
	public void testProcessServiceInvocation() throws InternalErrorException, UnknownRequestException, IOException, ProcessingException {

		String request = "{\n" + "  \"jsonrpc\": \"2.0\",\n" + "  \"method\": \"getCanonicalMappings\",\n" + "  \"params\": {\n" + "    \"request\": {\n"
				+ "      \"idAuthority\": \"2.16.840.1.113883.3.59.3:736\",\n" + "      \"idExt\": \"87170\"\n" + "    },\n" + "    \"clientId\": \"mock\",\n" + "    \"clientPass\": \"mock\"\n"
				+ "  }\n" + "}";
		StringReader reader = new StringReader(request);

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
		PersServiceVersionJsonRpc20 def = mock(PersServiceVersionJsonRpc20.class, new DefaultAnswer());
		RequestType reqType = RequestType.POST;
		String path = "/";
		String query = "";
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class, new DefaultAnswer());

		when(def.getMethod("getCanonicalMappings")).thenReturn(method);
		when(def.getServerAuths()).thenReturn(new ArrayList<PersBaseServerAuth<?, ?>>());

		DefaultAnswer.setRunTime();
		InvocationResultsBean resp = svc.processInvocation(def, reqType, path, query, reader);

		String actualBody = resp.getMethodRequestBody();
		Assert.assertEquals(request, actualBody);
		Assert.assertEquals("application/json", resp.getMethodContentType());
		Assert.assertEquals(ResultTypeEnum.METHOD, resp.getResultType());
		Assert.assertSame(method, resp.getMethodDefinition());

		/*
		 * Try with null method params
		 */

		request = "{\n" + "  \"jsonrpc\": \"2.0\",\n" + "  \"method\": \"getCanonicalMappings\",\n" + "  \"params\": null\n" + "}";
		reader = new StringReader(request);

		resp = svc.processInvocation(def, reqType, path, query, reader);

		actualBody = resp.getMethodRequestBody();
		Assert.assertEquals(request, actualBody);

		/*
		 * Params as an array
		 */

		request = "{\n" + "  \"jsonrpc\": \"2.0\",\n" + "  \"method\": \"getCanonicalMappings\",\n" + "  \"params\": [\n" + "    1,\n" + "    2,\n" + "    3,\n" + "    4,\n" + "    5\n" + "  ]\n"
				+ "}";
		reader = new StringReader(request);

		resp = svc.processInvocation(def, reqType, path, query, reader);

		actualBody = resp.getMethodRequestBody();
		Assert.assertEquals(request, actualBody);
	}

	@Before
	public void before() {
		DefaultAnswer.setDesignTime();
	}

	@After
	public void after() {
		DefaultAnswer.setRunTime();
	}

	@Test
	public void testProcessInvocationResponse() throws ProcessingException {

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
		HttpResponseBean respBean = new HttpResponseBean();

		String response = "{\"jsonrpc\": \"2.0\", \"result\": -19, \"id\": 2}";
		respBean.setBody(response);
		InvocationResponseResultsBean resp = svc.processInvocationResponse(respBean);
		Assert.assertEquals(ResponseTypeEnum.SUCCESS, resp.getResponseType());

		response = "{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -32601, \"message\": \"Method not found\"}, \"id\": \"1\"}";
		respBean.setBody(response);
		resp = svc.processInvocationResponse(respBean);
		Assert.assertEquals(ResponseTypeEnum.FAULT, resp.getResponseType());
		Assert.assertEquals("-32601", resp.getResponseFaultCode());
		Assert.assertEquals("Method not found", resp.getResponseFaultDescription());

	}

	
	@Test
	public void testProcessInvocationResponseWithNumbers() throws ProcessingException {

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
		HttpResponseBean respBean = new HttpResponseBean();

		respBean.setBody("{\"jsonrpc\": \"2.0\", \"result\": -19, \"id\": 2}");
		InvocationResponseResultsBean resp = svc.processInvocationResponse(respBean);
		Assert.assertEquals(ResponseTypeEnum.SUCCESS, resp.getResponseType());

		respBean.setBody("{\"jsonrpc\": \"2.0\", \"result\": -19.9, \"id\": 2}");
		resp = svc.processInvocationResponse(respBean);
		Assert.assertEquals(ResponseTypeEnum.SUCCESS, resp.getResponseType());

		respBean.setBody("{\"jsonrpc\": \"2.0\", \"result\": [ -19.1, 22.2 ], \"id\": 2}");
		resp = svc.processInvocationResponse(respBean);
		Assert.assertEquals(ResponseTypeEnum.SUCCESS, resp.getResponseType());

		respBean.setBody("{\"jsonrpc\": \"2.0\", \"result\": { \"hello\": 22.2 }, \"id\": 2}");
		resp = svc.processInvocationResponse(respBean);
		Assert.assertEquals(ResponseTypeEnum.SUCCESS, resp.getResponseType());


	}
	

	@Test
	public void testProcessLargeInvocationResponse() throws ProcessingException, IOException {

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
		HttpResponseBean respBean = new HttpResponseBean();

		String response = IOUtils.toString(JsonRpc20ResponseValidatorTest.class.getResourceAsStream("/badjsonresponse.json"));
		respBean.setBody(response);
		InvocationResponseResultsBean resp = svc.processInvocationResponse(respBean);
		Assert.assertEquals(ResponseTypeEnum.SUCCESS, resp.getResponseType());
		
		
		String actual = resp.getResponseBody();
		assertEquals(response, actual);
		
	}

}
