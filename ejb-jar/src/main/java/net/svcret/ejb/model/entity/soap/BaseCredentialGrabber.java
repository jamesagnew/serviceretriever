package net.svcret.ejb.model.entity.soap;

import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.ex.ProcessingException;

import org.apache.commons.codec.digest.DigestUtils;

public abstract class BaseCredentialGrabber implements ICredentialGrabber {

	@Override
	public String getCredentialHash() throws ProcessingException {
		return DigestUtils.sha512Hex(getUsername() + "$" + getPassword());
	}

}
