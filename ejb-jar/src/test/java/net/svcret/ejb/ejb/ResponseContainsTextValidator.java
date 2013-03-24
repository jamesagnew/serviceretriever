package net.svcret.ejb.ejb;

import net.svcret.ejb.api.IResponseValidator;

public class ResponseContainsTextValidator implements IResponseValidator {

	private String myText;

	public ResponseContainsTextValidator(String theText) {
		myText = theText;
	}

	@Override
	public ValidationResponse validate(String theBody, int theStatusCode, String theContentType) {
		if (theBody.contains(myText)) {
			return new ValidationResponse(true);
		}
		return new ValidationResponse(false, "Response does not contain " + myText);
	}

}
