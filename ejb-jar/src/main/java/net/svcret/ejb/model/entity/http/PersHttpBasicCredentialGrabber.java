package net.svcret.ejb.model.entity.http;

import java.util.List;
import java.util.Map;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.soap.BaseCredentialGrabber;

public class PersHttpBasicCredentialGrabber extends BaseCredentialGrabber {

	public PersHttpBasicCredentialGrabber(Map<String, List<String>> theRequestHeaders) {
		
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

}
