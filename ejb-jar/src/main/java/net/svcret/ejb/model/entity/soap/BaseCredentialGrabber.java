package net.svcret.ejb.model.entity.soap;

import net.svcret.ejb.api.ICredentialGrabber;

import org.apache.commons.codec.digest.DigestUtils;

public abstract class BaseCredentialGrabber implements ICredentialGrabber {

	@Override
	public String getCredentialHash() {
		return DigestUtils.sha512Hex(getUsername() + "$" + getPassword());
	}

}
