package net.svcret.ejb.invoker.jsonrpc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.InvocationResultsBean.ResultTypeEnum;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.ejb.DefaultAnswer;
import net.svcret.ejb.ex.InvocationRequestFailedException;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.jsonrpc.NamedParameterJsonRpcClientAuth;
import net.svcret.ejb.model.entity.jsonrpc.NamedParameterJsonRpcServerAuth;
import net.svcret.ejb.model.entity.jsonrpc.PersServiceVersionJsonRpc20;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonRpc20ServiceInvokerTest {

	@Test
	public void testObscureDocument() throws Exception{
		String request = "{\n" + "  \"jsonrpc\": \"2.0\",\n" + "  \"method\": \"getCanonicalMappings\",\n" + "  \"params\": {\n" + "    \"request\": {\n"
				+ "      \"idAuthority\": \"2.16.840.1.113883.3.59.3:736\",\n" + "      \"idExt\": \"87170\"\n" + "    },\n" + "    \"clientId\": \"mockuser\",\n"
				+ "    \"clientPass\": \"mockpass\"\n" + "  }\n" + "}";

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
		svc.setPrettyPrintModeForUnitTest(true);
		Set<String> obscure = new HashSet<String>();
		String obscured = svc.obscureMessageForLogs(request, obscure);
		ourLog.info("Not obscured: {}", obscured);

		obscure.clear();
		obscure.add("idExt");
		obscured = svc.obscureMessageForLogs(request, obscure);
		ourLog.info("Not obscured: {}", obscured);
		assertTrue(obscured.contains("\"idExt\": \"**REDACTED**\""));

		obscure.clear();
		obscure.add("request");
		obscured = svc.obscureMessageForLogs(request, obscure);
		ourLog.info("Not obscured: {}", obscured);
		assertTrue(obscured.contains("\"request\": \"**REDACTED**\""));
		assertFalse(obscured.contains("\"idExt\""));

		//@formatter:off
		request = "{\n" + 
				"    \"jsonrpc\": \"2.0\",\n" + 
				"    \"method\": \"getNODRPatientByMrn\",\n" + 
				"    \"params\": {\n" + 
				"        \"auth\": \"UHN\",\n" + 
				"        \"mrn\": \"\",\n" + 
				"        \"auditSourceId\": \"FORM_VIEWER\",\n" + 
				"        \"user\": {\n" + 
				"            \"lastName\": \"King\",\n" + 
				"            \"lastLoginDate\": \"Aug 27, 2013 03:51:44 PM\",\n" + 
				"            \"location\": \"UHN\",\n" + 
				"            \"firstName\": \"Gered\",\n" + 
				"            \"attributes\": {},\n" + 
				"            \"distinguishedName\": \"CN=King\\\\, Gered,OU=AdvancedUsers,OU=UHNPeople,DC=uhn,DC=ca\",\n" + 
				"            \"email\": \"Gered.King@uhn.ca\",\n" + 
				"            \"uid\": \"t35103uhn\",\n" + 
				"            \"fullName\": \"King, Gered\",\n" + 
				"            \"clientIp\": \"10.7.7.167\",\n" + 
				"            \"description\": \"Developer - Medical Informatics, SIMS\"\n" + 
				"        },\n" + 
				"        \"clientId\": \"clipdevsvc\",\n" + 
				"        \"clientPass\": \"Hospital20\"\n" + 
				"    }\n" + 
				"}";
		//@formatter:on

		obscure.clear();
		obscure.add("password");
		obscure.add("clientPass");
		obscured = svc.obscureMessageForLogs(request, obscure);
		ourLog.info("Obscured: {}", obscured);
		assertTrue(obscured.contains("\"clientPass\": \"**REDACTED**\""));
		assertTrue(obscured.contains("\"clientId\""));

	}

	@Test
	public void testNamedParemeterCredentialGrabber() throws Exception {

		String request = "{\n" + "  \"jsonrpc\": \"2.0\",\n" + "  \"method\": \"getCanonicalMappings\",\n" + "  \"params\": {\n" + "    \"request\": {\n"
				+ "      \"idAuthority\": \"2.16.840.1.113883.3.59.3:736\",\n" + "      \"idExt\": \"87170\"\n" + "    },\n" + "    \"clientId\": \"mockuser\",\n"
				+ "    \"clientPass\": \"mockpass\"\n" + "  }\n" + "}";
		StringReader reader = new StringReader(request);

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();

		PersServiceVersionJsonRpc20 def = mock(PersServiceVersionJsonRpc20.class);
		ArrayList<PersBaseServerAuth<?, ?>> serverAuths = new ArrayList<PersBaseServerAuth<?, ?>>();
		serverAuths.add(new NamedParameterJsonRpcServerAuth("clientId", "clientPass"));
		when(def.getServerAuths()).thenReturn(serverAuths);
		RequestType reqType = RequestType.POST;
		String path = "/";
		String query = "";
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class);

		when(def.getMethod("getCanonicalMappings")).thenReturn(method);

		DefaultAnswer.setRunTime();
		InvocationResultsBean resp = svc.processInvocation(def, reqType, path, query, "application/json", reader);

		ICredentialGrabber grabber = resp.getCredentialsInRequest(serverAuths.get(0));
		assertEquals("mockpass", grabber.getPassword());
		assertEquals("mockuser", grabber.getUsername());

	}

	@Test
	public void testNamedParemeterCredentialGrabberServerSecurityAndNamedParameterClientSecurity() throws Exception{

		//@formatter:off
		String request = 
				"{\n" + 
				"  \"jsonrpc\": \"2.0\",\n" + 
				"  \"method\": \"getCanonicalMappings\",\n" +
				"  \"params\": {\n" +
				"    \"request\": {\n" +
				"      \"idAuthority\": \"2.16.840.1.113883.3.59.3:736\",\n" + 
				"      \"idExt\": \"87170\"\n" + 
				"    },\n" + 
				"    \"clientId\": \"mockuser\",\n" +
				"    \"clientPass\": \"mockpass\"\n" + 
				"  }\n" + 
				"}";
		//@formatter:on

		StringReader reader = new StringReader(request);

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();

		PersServiceVersionJsonRpc20 def = mock(PersServiceVersionJsonRpc20.class, new DefaultAnswer());
		ArrayList<PersBaseServerAuth<?, ?>> serverAuths = new ArrayList<PersBaseServerAuth<?, ?>>();
		serverAuths.add(new NamedParameterJsonRpcServerAuth("clientId", "clientPass"));
		when(def.getServerAuths()).thenReturn(serverAuths);

		ArrayList<PersBaseClientAuth<?>> clientAuths = new ArrayList<PersBaseClientAuth<?>>();
		clientAuths.add(new NamedParameterJsonRpcClientAuth("newUsername", "clientId", "newPassword", "clientPass"));
		when(def.getClientAuths()).thenReturn(clientAuths);

		RequestType reqType = RequestType.POST;
		String path = "/";
		String query = "";
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class, new DefaultAnswer());

		when(def.getMethod("getCanonicalMappings")).thenReturn(method);

		DefaultAnswer.setRunTime();
		InvocationResultsBean resp = svc.processInvocation(def, reqType, path, query, "application/json", reader);

		ICredentialGrabber grabber = resp.getCredentialsInRequest(serverAuths.get(0));
		assertEquals("mockpass", grabber.getPassword());
		assertEquals("mockuser", grabber.getUsername());

		String newRequest = resp.getMethodRequestBody();
		ourLog.info("New request: {}", newRequest);
		assertTrue(newRequest.contains("\"clientId\": \"newUsername\""));
		assertTrue(newRequest.contains("\"clientPass\": \"newPassword\""));

		/*
		 * Now with a request containing no credentials in it to begin with (so they need to be added)
		 */

		//@formatter:off
		request = 
				"{\n" + 
				"  \"jsonrpc\": \"2.0\",\n" + 
				"  \"method\": \"getCanonicalMappings\",\n" +
				"  \"params\": {\n" +
				"    \"request\": {\n" +
				"      \"idAuthority\": \"2.16.840.1.113883.3.59.3:736\",\n" + 
				"      \"idExt\": \"87170\"\n" + 
				"    }\n" + 
				"  }\n" + 
				"}";
		//@formatter:on

		reader = new StringReader(request);

		svc = new JsonRpc20ServiceInvoker();

		def = mock(PersServiceVersionJsonRpc20.class);
		serverAuths = new ArrayList<PersBaseServerAuth<?, ?>>();
		serverAuths.add(new NamedParameterJsonRpcServerAuth("clientId", "clientPass"));
		when(def.getServerAuths()).thenReturn(serverAuths);

		clientAuths = new ArrayList<PersBaseClientAuth<?>>();
		clientAuths.add(new NamedParameterJsonRpcClientAuth("newUsername", "clientId", "newPassword", "clientPass"));
		when(def.getClientAuths()).thenReturn(clientAuths);

		method = mock(PersServiceVersionMethod.class);

		when(def.getMethod("getCanonicalMappings")).thenReturn(method);

		DefaultAnswer.setRunTime();
		resp = svc.processInvocation(def, reqType, path, query, "application/json", reader);

		grabber = resp.getCredentialsInRequest(serverAuths.get(0));
		assertEquals(null, grabber.getPassword());
		assertEquals(null, grabber.getUsername());

		newRequest = resp.getMethodRequestBody();
		ourLog.info("New request: {}", newRequest);
		assertTrue(newRequest.contains("\"clientId\": \"newUsername\""));
		assertTrue(newRequest.contains("\"clientPass\": \"newPassword\""));

	}

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(JsonRpc20ServiceInvokerTest.class);

	/**
	 * A failing message
	 */
	@Test
	public void testNullParameterValues() throws Exception{

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
		InvocationResultsBean resp = svc.processInvocation(def, reqType, path, query, "application/json", reader);

		Assert.assertEquals("application/json", resp.getMethodContentType());
		Assert.assertEquals(ResultTypeEnum.METHOD, resp.getResultType());
		Assert.assertSame(method, resp.getMethodDefinition());

		svc.obscureMessageForLogs(request, createObscureRequest()); // just to try

	}

	/**
	 * A failing message
	 */
	@Test
	public void testPartialMessage() throws Exception{

		String request = "{\"jsonrpc\":\"2.0\",\"method\":\"getActsByVisit\",\"params\":{\"auth\":\"UHN\",\"convertFromAppTerminology\":false,\"clientId\":\"clipdevsvc\",\"auditSourceId\":\"FORM_VIEWER\",\"visitId\":\"26200\n"
				+ "0218\",\"actToTerminologyLevel\":\"NONE\",\"convertObsToAppTerm\":false,\"statusCodes\":null,\"returnLoadedConcept\":false,\"loaded\":false,\"user\":{\"uid\":\"userId\",\"clientIp\":\"127.0.0.\n"
				+ "1\",\"attributes\":{}},\"clientPass\":\"Hospital20\",\"procCodes\":[\"1881\",\"1756\",\"1757\",\"1758\",\"5119\"]}}";

		ourLog.info("Request:\n{}", request);

		for (int i = 0; i < request.length(); i++) {

			String substring = request.substring(0, request.length() - i);
			StringReader reader = new StringReader(substring);

			JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
			PersServiceVersionJsonRpc20 def = mock(PersServiceVersionJsonRpc20.class, new DefaultAnswer());
			RequestType reqType = RequestType.POST;
			String path = "/";
			String query = "";
			PersServiceVersionMethod method = mock(PersServiceVersionMethod.class, new DefaultAnswer());

			when(def.getMethod("getActsByVisit")).thenReturn(method);
			when(def.getServerAuths()).thenReturn(new ArrayList<PersBaseServerAuth<?, ?>>());

			try {
				svc.processInvocation(def, reqType, path, query, "application/json", reader);
				svc.obscureMessageForLogs(substring, createObscureRequest());
			} catch (InvocationRequestFailedException e) {
				// this is ok
			}

		}

	}

	private Set<String> createObscureRequest(String... theStrings) {
		HashSet<String> retVal = new HashSet<String>();
		retVal.add("AAAA");
		for (String string : theStrings) {
			retVal.add(string);
		}
		return retVal;
	}

	@Test
	public void testProcessInvocationWithNumbers() throws Exception{

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
		PersServiceVersionJsonRpc20 def = mock(PersServiceVersionJsonRpc20.class);
		RequestType reqType = RequestType.POST;
		String path = "/";
		String query = "";
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class);

		when(def.getMethod("someMethod")).thenReturn(method);
		when(def.getServerAuths()).thenReturn(new ArrayList<PersBaseServerAuth<?, ?>>());

		DefaultAnswer.setRunTime();

		@SuppressWarnings("unused")
		InvocationResultsBean resp;

		//@formatter:off
		String request = // -
		"{ \"jsonrpc\": \"2.0\",\n" + // -
				"  \"method\": \"someMethod\",\n" + // -
				"  \"params\": 123\n" + // -
				"}"; // -
		//@formatter:on

		resp = svc.processInvocation(def, reqType, path, query, "application/json", new StringReader(request));
		svc.obscureMessageForLogs(request, createObscureRequest()); // just to try
		svc.obscureMessageForLogs(request, createObscureRequest("params")); // just to try

		//@formatter:off
		request = // -
		"{ \"jsonrpc\": \"2.0\",\n" + // -
				"  \"method\": \"someMethod\",\n" + // -
				"  \"params\": -123.456\n" + // -
				"}"; // -
		//@formatter:on

		resp = svc.processInvocation(def, reqType, path, query, "application/json", new StringReader(request));
		svc.obscureMessageForLogs(request, createObscureRequest()); // just to try
		svc.obscureMessageForLogs(request, createObscureRequest("params")); // just to try

		//@formatter:off
		request = // -
		"{ \"jsonrpc\": \"2.0\",\n" + // -
				"  \"method\": \"someMethod\",\n" + // -
				"  \"params\": [ 123.456, 876.543 ] \n" + // -
				"}"; // -
		//@formatter:on

		resp = svc.processInvocation(def, reqType, path, query, "application/json", new StringReader(request));
		svc.obscureMessageForLogs(request, createObscureRequest()); // just to try
		svc.obscureMessageForLogs(request, createObscureRequest("params")); // just to try

		//@formatter:off
		request = // -
		"{ \"jsonrpc\": \"2.0\",\n" + // -
				"  \"method\": \"someMethod\",\n" + // -
				"  \"params\": [123.456, 876.543, \"222.222\"] \n" + // -
				"}"; // -
		//@formatter:on

		ourLog.info(request);
		resp = svc.processInvocation(def, reqType, path, query, "application/json", new StringReader(request));
		svc.obscureMessageForLogs(request, createObscureRequest()); // just to try
		svc.obscureMessageForLogs(request, createObscureRequest("params")); // just to try

		//@formatter:off
		request = // -
		"{ \"jsonrpc\": \"2.0\",\n" + // -
				"  \"method\": \"someMethod\",\n" + // -
				"  \"params\": { \"hello\" :  876.543 } \n" + // -
				"}"; // -
		//@formatter:on

		resp = svc.processInvocation(def, reqType, path, query, "application/json", new StringReader(request));
		svc.obscureMessageForLogs(request, createObscureRequest()); // just to try
		svc.obscureMessageForLogs(request, createObscureRequest("params")); // just to try

	}

	@Test
	public void testProcessServiceInvocation() throws Exception{

		String request = "{\n" + "  \"jsonrpc\": \"2.0\",\n" + "  \"method\": \"getCanonicalMappings\",\n" + "  \"params\": {\n" + "    \"request\": {\n"
				+ "      \"idAuthority\": \"2.16.840.1.113883.3.59.3:736\",\n" + "      \"idExt\": \"87170\"\n" + "    },\n" + "    \"clientId\": \"mock\",\n" + "    \"clientPass\": \"mock\"\n"
				+ "  }\n" + "}";
		StringReader reader = new StringReader(request);

		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
		PersServiceVersionJsonRpc20 def = mock(PersServiceVersionJsonRpc20.class);
		RequestType reqType = RequestType.POST;
		String path = "/";
		String query = "";
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class);

		when(def.getMethod("getCanonicalMappings")).thenReturn(method);
		when(def.getServerAuths()).thenReturn(new ArrayList<PersBaseServerAuth<?, ?>>());

		DefaultAnswer.setRunTime();
		InvocationResultsBean resp = svc.processInvocation(def, reqType, path, query, "application/json", reader);

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

		resp = svc.processInvocation(def, reqType, path, query, "application/json", reader);

		actualBody = resp.getMethodRequestBody();
		Assert.assertEquals(request, actualBody);

		/*
		 * Params as an array
		 */

		request = "{\n" + "  \"jsonrpc\": \"2.0\",\n" + "  \"method\": \"getCanonicalMappings\",\n" + "  \"params\": [\n" + "    1,\n" + "    2,\n" + "    3,\n" + "    4,\n" + "    5\n" + "  ]\n"
				+ "}";
		reader = new StringReader(request);

		resp = svc.processInvocation(def, reqType, path, query, "application/json", reader);

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
	public void testProcessInvocationResponse() throws Exception{

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
	public void testProcessInvocationResponseWithNumbers() throws Exception{

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
	public void testProcessLargeInvocationResponse() throws Exception{

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
