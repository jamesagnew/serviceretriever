package net.svcret.ejb.invoker.soap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.svcret.ejb.Messages;
import net.svcret.ejb.api.IResponseValidator;

public abstract class BaseResponseValidator implements IResponseValidator {

	private Set<String> myAcceptableContentTypes;
	private Set<Integer> myAcceptableHttpStatusCodes;
	private Collection<String> myRequiredBodyFragments;

	public void setAcceptableContentTypes(String... theStrings) {
		myAcceptableContentTypes = new HashSet<String>();
		for (String string : theStrings) {
			myAcceptableContentTypes.add(string);
		}
	}

	public void setAcceptableHttpStatusCodes(int... theCodes) {
		myAcceptableHttpStatusCodes = new HashSet<Integer>();
		for (int i : theCodes) {
			myAcceptableHttpStatusCodes.add(i);
		}
	}


	@Override
	public ValidationResponse validate(String theBody, int theStatusCode, String theContentType) {
		if (myAcceptableHttpStatusCodes != null) {
			if (!myAcceptableHttpStatusCodes.contains(theStatusCode)) {
				return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.statusFail", theStatusCode));
			}
		}
	
		if (myAcceptableContentTypes != null) {
			if (!myAcceptableContentTypes.contains(theContentType)) {
				return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.contentTypeFail", theContentType));
			}
		}
	
		if (myRequiredBodyFragments != null) {
			for (String next : myRequiredBodyFragments) {
				if (!theBody.contains(next)) {
					return new ValidationResponse(false, Messages.getString("Soap11ResponseValidator.bodyFragmentFail", next));
				}
			}
		}
		
		return new ValidationResponse(true);
	}

}
