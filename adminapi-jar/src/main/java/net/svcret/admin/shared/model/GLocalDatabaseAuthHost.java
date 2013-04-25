package net.svcret.admin.shared.model;

public class GLocalDatabaseAuthHost extends BaseGAuthHost {

	private static final long serialVersionUID = 1L;

	@Override
	public void merge(BaseGAuthHost theObject) {
		super.merge(theObject);
	}

	@Override
	public AuthorizationHostTypeEnum getType() {
		return AuthorizationHostTypeEnum.LOCAL_DATABASE;
	}

	@Override
	public boolean isSupportsPasswordChange() {
		return true;
	}

	@Override
	public void setSupportsPasswordChange(boolean theSupportsPasswordChange) {
		if (theSupportsPasswordChange == false) {
			throw new IllegalArgumentException("Must be true");
		}
	}

}
