package net.svcret.ejb.ejb;

import net.svcret.ejb.api.IResponseValidator;

public class NullResponseValidator implements IResponseValidator {

	@Override
	public ValidationResponse validate(String theBody, int theStatusCode, String theContentType) {
		return new ValidationResponse(true);
	}

}
