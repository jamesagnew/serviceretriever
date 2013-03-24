package net.svcret.ejb.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * LDAP authentication host
 */
@Entity
@DiscriminatorValue("LDAP")
public class PersAuthenticationHostLdap extends BasePersAuthenticationHost {

	public PersAuthenticationHostLdap() {
		super();
	}
	
	public PersAuthenticationHostLdap(String theModuleId) {
		super(theModuleId);
	}
	

	
	

}
