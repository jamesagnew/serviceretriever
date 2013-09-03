package net.svcret.admin.shared.model;

public class GServiceVersionResourcePointer extends BaseGObject {

	private static final long serialVersionUID = 1L;

	private int mySize;
	private String myType;

	private String myUrl;

	/**
	 * @return the size
	 */
	public int getSize() {
		return mySize;
	}

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
	public void merge(BaseGObject theObject) {
		super.merge(theObject);
		
		GServiceVersionResourcePointer obj=(GServiceVersionResourcePointer) theObject;
		setType(obj.getType());
		setUrl(obj.getUrl());
		setSize(obj.getSize());
	}

	public void setSize(int theLength) {
		mySize = theLength;
	}

	/**
	 * @param theType
	 *            the type to set
	 */
	public void setType(String theType) {
		myType = theType;
	}

	/**
	 * @param theUrl
	 *            the url to set
	 */
	public void setUrl(String theUrl) {
		myUrl = theUrl;
	}

}
