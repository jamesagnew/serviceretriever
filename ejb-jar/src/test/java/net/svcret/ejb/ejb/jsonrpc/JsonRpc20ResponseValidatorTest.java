package net.svcret.ejb.ejb.jsonrpc;

import net.svcret.ejb.ejb.jsonrpc.JsonRpc20ResponseValidator;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uhn.sail.proxy.api.IResponseValidator.ValidationResponse;

public class JsonRpc20ResponseValidatorTest {

	private JsonRpc20ResponseValidator myVal;

	@Before
	public void before() {
		myVal = new JsonRpc20ResponseValidator();
	}
	
	@Test
	public void testValidResponse() {
		
		String response = "{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}";
		ValidationResponse actual = myVal.validate(response, 200, "application/json");
		Assert.assertEquals(actual.getFailureExplanation(), true, actual.isValidates());
		
	}
	
	@Test
	public void testBadCode() {
		
		String response = "{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}";
		ValidationResponse actual = myVal.validate(response, 201, "application/json");
		
		Assert.assertEquals(actual.getFailureExplanation(), false, actual.isValidates());
		Assert.assertThat(actual.getFailureExplanation(), Matchers.containsString("HTTP 201"));
		
	}

	@Test
	public void testEmptyResponse() {
		
		String response = "";
		ValidationResponse actual = myVal.validate(response, 200, "application/json");
		
		Assert.assertEquals(actual.getFailureExplanation(), false, actual.isValidates());
		Assert.assertThat(actual.getFailureExplanation(), Matchers.containsString("Failed to parse"));
		
	}

	@Test
	public void testNonJsonResponse() {
		
		String response = "<html/>";
		ValidationResponse actual = myVal.validate(response, 200, "application/json");
		
		Assert.assertEquals(actual.getFailureExplanation(), false, actual.isValidates());
		Assert.assertThat(actual.getFailureExplanation(), Matchers.containsString("Failed to parse"));
		
	}

	
}
