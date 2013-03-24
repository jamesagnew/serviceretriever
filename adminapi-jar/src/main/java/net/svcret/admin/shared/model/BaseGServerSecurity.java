package net.svcret.admin.shared.model;

public abstract class BaseGServerSecurity extends BaseGObject<BaseGServerSecurity> {

	private static final long serialVersionUID = 1L;

	private transient boolean myEditMode;
	
	/**
	 * @return the editMode
	 */
	public boolean isEditMode() {
		return myEditMode;
	}

	/**
	 * @param theEditMode the editMode to set
	 */
	public void setEditMode(boolean theEditMode) {
		myEditMode = theEditMode;
	}

	@Override
	public void merge(BaseGServerSecurity theObject) {
		setPid(theObject.getPid());
	}
	
	public abstract ServerSecurityEnum getType();

}
