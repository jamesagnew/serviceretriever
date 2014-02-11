package net.svcret.core.invoker.hl7;

import net.svcret.core.invoker.soap.BaseResponseValidator;

public class Hl7OverHttpResponseValidator extends BaseResponseValidator {

	public Hl7OverHttpResponseValidator() {
		setAcceptableContentTypes("application/hl7-v2", "application/hl7-v2+xml");
		setAcceptableHttpStatusCodes(200);
	}

}
