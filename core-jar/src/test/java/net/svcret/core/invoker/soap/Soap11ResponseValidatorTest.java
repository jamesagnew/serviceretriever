package net.svcret.core.invoker.soap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import net.svcret.core.invoker.soap.BaseResponseValidator;
import net.svcret.core.invoker.soap.Soap11ResponseValidator;

import org.junit.Test;

public class Soap11ResponseValidatorTest {

	@Test
	public void testValidate() {
		
		String msg = "<env:Envelope  xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\" >\n" + //- 
				"    <env:Body>\n" +  //-
				"        <env:Fault>\n" + //- 
				"\n" +  //-
				"          <env:Code>\n" +  //-
				"            <env:Value>SomeCode</env:Value>\n" + //- 
				"          </env:Code>\n" + //- 
				"\n" +  //-
				"          <env:Reason>\n" +  //-
				"            <env:Text xml:lang=\"en-US\">ColorfulIssue</env:Text>\n" +  //-
				"            <env:Text xml:lang=\"en-GB\">ColourfulIssue</env:Text>\n" + //- 
				"          </env:Reason>\n" + //- 
				"\n" + //-
				"        </env:Fault>\n" + //- 
				"    </env:Body>\n" + //- 
				"</env:Envelope>"; //-
		
		BaseResponseValidator val = new Soap11ResponseValidator();
		assertEquals(true, val.validate(msg, 200, "text/xml").isValidates());
		
		assertEquals(false, val.validate(msg, 201, "text/xml").isValidates());
		assertEquals(false, val.validate(msg, 200, "text/html").isValidates());
		assertEquals(false, val.validate("", 200, "text/xml").isValidates());
		assertEquals(false, val.validate("<element></element>", 200, "text/xml").isValidates());
		
		
	}

	
	
	@Test
	public void testValidateWrongSoapVersion() {
		
		String msg = "<?xml version=\"1.0\"?>\n" +  //-
				"<env:Envelope  xmlns:env=\"http://www.w3.org/2001/12/soap-envelope\" >\n" + //- 
				"    <env:Body>\n" +  //-
				"        <env:Fault>\n" + //- 
				"\n" +  //-
				"          <env:Code>\n" +  //-
				"            <env:Value>SomeCode</env:Value>\n" + //- 
				"          </env:Code>\n" + //- 
				"\n" +  //-
				"          <env:Reason>\n" +  //-
				"            <env:Text xml:lang=\"en-US\">ColorfulIssue</env:Text>\n" +  //-
				"            <env:Text xml:lang=\"en-GB\">ColourfulIssue</env:Text>\n" + //- 
				"          </env:Reason>\n" + //- 
				"\n" + //-
				"        </env:Fault>\n" + //- 
				"    </env:Body>\n" + //- 
				"</env:Envelope>"; //-
		
		Soap11ResponseValidator val = new Soap11ResponseValidator();
		assertEquals(false, val.validate(msg, 200, "text/xml").isValidates());
		assertThat(val.validate(msg, 200, "text/xml").getFailureExplanation(), containsString("version"));
		
		
		
	}

}
