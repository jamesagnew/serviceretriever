package net.svcret.ejb.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("LOCAL_DB")
public class PersAuthenticationHostLocalDatabase extends BasePersAuthenticationHost {

	public PersAuthenticationHostLocalDatabase() {
		// super
	}

	public PersAuthenticationHostLocalDatabase(String theModuleId) {
		super(theModuleId);
	}

	@Override
	public AuthorizationHostTypeEnum getType() {
		return AuthorizationHostTypeEnum.LOCAL_DATABASE;
	}

}
