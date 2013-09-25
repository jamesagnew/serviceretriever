package net.svcret.ejb.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.enm.AuthorizationHostTypeEnum;

@Entity
@DiscriminatorValue("LOCAL_DB")
public class PersAuthenticationHostLocalDatabase extends BasePersAuthenticationHost {

	private static final long serialVersionUID = 1L;

	public PersAuthenticationHostLocalDatabase() {
		setSupportsPasswordChange(true);
	}

	public PersAuthenticationHostLocalDatabase(String theModuleId) {
		super(theModuleId);
		setSupportsPasswordChange(true);
	}

	@Override
	public AuthorizationHostTypeEnum getType() {
		return AuthorizationHostTypeEnum.LOCAL_DATABASE;
	}

	/**
	 * Returns true for this type
	 */
	@Override
	public boolean isSupportsPasswordChange() {
		return true;
	}

	/**
	 * Must be set to true for this type
	 */
	@Override
	public void setSupportsPasswordChange(boolean theSupportsPasswordChange) {
		if (!theSupportsPasswordChange) {
			throw new IllegalArgumentException("Must be set to true");
		}
		super.setSupportsPasswordChange(theSupportsPasswordChange);
	}


}
