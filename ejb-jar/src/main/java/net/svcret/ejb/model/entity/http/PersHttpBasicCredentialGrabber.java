package net.svcret.ejb.model.entity.http;

import java.util.List;
import java.util.Map;

import net.svcret.ejb.model.entity.soap.BaseCredentialGrabber;

public class PersHttpBasicCredentialGrabber extends BaseCredentialGrabber {

	private String myPassword;
	private String myUsername;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(PersHttpBasicCredentialGrabber.class);

	public PersHttpBasicCredentialGrabber(Map<String, List<String>> theRequestHeaders) {
		List<String> nextValues = theRequestHeaders.get("authorization");
		if (nextValues==null||nextValues.size()==0) {
			return;
		}
		String nextValue = nextValues.get(0);
		
		int spaceIndex = nextValue.indexOf(' ');
		if (spaceIndex == -1) {
			ourLog.info("Invalid authorization header detected - Format must be 'basic [authorization]' but no space found");
			return;
		}
		String type = nextValue.substring(0, spaceIndex);
		if ("basic".equalsIgnoreCase(type)) {
			String encodedCredentials = nextValue.substring(spaceIndex + 1);
			byte[] decodedCredentials = org.apache.commons.codec.binary.Base64.decodeBase64(encodedCredentials);
			String credentialsString = new String(decodedCredentials);
			int colonIndex = credentialsString.indexOf(':');
			if (colonIndex == -1) {
				myUsername=(credentialsString);
			} else {
				myUsername=(credentialsString.substring(0, colonIndex));
				myPassword=(credentialsString.substring(colonIndex + 1));
			}
		} else {
			ourLog.info("Invalid authorization type. Only basic authorization is supported.");
			return;
		}
	}

	@Override
	public String getUsername() {
		return myUsername;
	}

	@Override
	public String getPassword() {
		return myPassword;
	}

	
	
}
