package net.svcret.ejb.ejb.jsonrpc;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.StringReader;

import net.svcret.ejb.api.HttpResponseBean;
import net.svcret.ejb.api.InvocationResponseResultsBean;
import net.svcret.ejb.api.InvocationResultsBean;
import net.svcret.ejb.api.InvocationResultsBean.ResultTypeEnum;
import net.svcret.ejb.api.RequestType;
import net.svcret.ejb.api.ResponseTypeEnum;
import net.svcret.ejb.ejb.DefaultAnswer;
import net.svcret.ejb.ex.InternalErrorException;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.ex.UnknownRequestException;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.jsonrpc.PersServiceVersionJsonRpc20;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonRpc20ServiceInvokerTest {

	@Test
	public void testProcessServiceInvocation() throws InternalErrorException, UnknownRequestException, IOException, ProcessingException {
		
		String request = "{\n" + 
				"  \"jsonrpc\": \"2.0\",\n" + 
				"  \"method\": \"getCanonicalMappings\",\n" + 
				"  \"params\": {\n" + 
				"    \"request\": {\n" + 
				"      \"idAuthority\": \"2.16.840.1.113883.3.59.3:736\",\n" + 
				"      \"idExt\": \"87170\"\n" + 
				"    },\n" + 
				"    \"clientId\": \"mock\",\n" + 
				"    \"clientPass\": \"mock\"\n" + 
				"  }\n" + 
				"}";
		StringReader reader = new StringReader(request);
		
		JsonRpc20ServiceInvoker svc = new JsonRpc20ServiceInvoker();
		PersServiceVersionJsonRpc20 def = mock(PersServiceVersionJsonRpc20.class, new DefaultAnswer());
		RequestType reqType=RequestType.POST;
		String path="/";
		String query="";
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class, new DefaultAnswer());
		
		when(def.getMethod("getCanonicalMappings")).thenReturn(method);
		
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
		
		request = "{\n" + 
				"  \"jsonrpc\": \"2.0\",\n" + 
				"  \"method\": \"getCanonicalMappings\",\n" + 
				"  \"params\": null\n" + 
				"}";
		reader = new StringReader(request);

		resp = svc.processInvocation(def, reqType, path, query, reader);
		
		actualBody = resp.getMethodRequestBody();
		Assert.assertEquals(request, actualBody);

		/*
		 * Params as an array
		 */
		
		request = "{\n" +
				"  \"jsonrpc\": \"2.0\",\n" +
				"  \"method\": \"getCanonicalMappings\",\n" +
				"  \"params\": [\n" +
				"    1,\n" +
				"    2,\n" +
				"    3,\n" +
				"    4,\n" +
				"    5\n" +
				"  ]\n" +
				"}";
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
	
}
