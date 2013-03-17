package ca.uhn.sail.proxy.admin.shared.model;

public class GServiceVersionResourcePointer extends BaseGObject<GServiceVersionResourcePointer> {

	private static final long serialVersionUID = 1L;

	private String myType;
	private String myUrl;

	/**
	 * @return the type
	 */
	public String getType() {
		return myType;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return myUrl;
	}

	@Override
	public void initChildList() {
		// ignore
	}

	@Override
	public void merge(GServiceVersionResourcePointer theObject) {
		setPid(theObject.getPid());
		setType(theObject.getType());
		setUrl(theObject.getUrl());
	}
	
	/**
	 * @param theType the type to set
	 */
	public void setType(String theType) {
		myType = theType;
	}

	/**
	 * @param theUrl the url to set
	 */
	public void setUrl(String theUrl) {
		myUrl = theUrl;
	}

}
