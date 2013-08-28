package net.svcret.ejb.ejb.hl7;

import net.svcret.ejb.ejb.soap.BaseResponseValidator;

public class Hl7OverHttpResponseValidator extends BaseResponseValidator {

	public Hl7OverHttpResponseValidator() {
		setAcceptableContentTypes("application/hl7-v2", "application/hl7-v2+xml");
		setAcceptableHttpStatusCodes(200);
	}

}
