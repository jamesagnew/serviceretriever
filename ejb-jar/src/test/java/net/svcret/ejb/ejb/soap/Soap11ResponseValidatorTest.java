package net.svcret.ejb.ejb.soap;

import static org.junit.Assert.*;

import net.svcret.ejb.ejb.soap.Soap11ResponseValidator;

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
		
		Soap11ResponseValidator val = new Soap11ResponseValidator();
		assertEquals(true, val.validate(msg, 200, "text/xml").isValidates());
		
		assertEquals(false, val.validate(msg, 201, "text/xml").isValidates());
		assertEquals(false, val.validate(msg, 200, "text/html").isValidates());
		assertEquals(false, val.validate("", 200, "text/xml").isValidates());
		assertEquals(false, val.validate("<element></element>", 200, "text/xml").isValidates());
		
		
	}
	
}
