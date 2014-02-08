package net.svcret.core.ejb;

import net.svcret.core.api.IResponseValidator;

public class NullResponseValidator implements IResponseValidator {

	@Override
	public ValidationResponse validate(String theBody, int theStatusCode, String theContentType) {
		return new ValidationResponse(true);
	}

}
