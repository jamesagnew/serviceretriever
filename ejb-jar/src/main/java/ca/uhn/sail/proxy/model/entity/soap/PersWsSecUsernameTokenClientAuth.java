package ca.uhn.sail.proxy.model.entity.soap;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.lang3.ObjectUtils;

import ca.uhn.sail.proxy.model.entity.PersBaseClientAuth;

@Entity
@DiscriminatorValue("WSSEC_UT")
public class PersWsSecUsernameTokenClientAuth extends PersBaseClientAuth<PersWsSecUsernameTokenClientAuth> {

	public PersWsSecUsernameTokenClientAuth(String theUsername, String thePassword) {
		super();
		setUsername(theUsername);
		setPassword(thePassword);
	}

	public PersWsSecUsernameTokenClientAuth() {
		super();
	}

	@Override
	protected boolean relevantPropertiesEqual(PersWsSecUsernameTokenClientAuth theT) {
		return ObjectUtils.equals(getUsername(), theT.getUsername()) && ObjectUtils.equals(getPassword(), theT.getPassword());
	}

}
