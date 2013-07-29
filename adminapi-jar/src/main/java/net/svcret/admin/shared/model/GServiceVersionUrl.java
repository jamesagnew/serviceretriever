package net.svcret.admin.shared.model;

public class GServiceVersionUrl extends BaseGObject<GServiceVersionUrl> {

	private static final long serialVersionUID = 1L;

	private String myId;
	private String myUrl;

	public GServiceVersionUrl() {
		// nothing
	}

	public GServiceVersionUrl(String theId, String theUrl) {
		myId = theId;
		myUrl = theUrl;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return myId;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return myUrl;
	}

	@Override
	public void merge(GServiceVersionUrl theObject) {
		setPid(theObject.getPid());
		setId(theObject.getId());
		setUrl(theObject.getUrl());
	}

	/**
	 * @param theId
	 *            the id to set
	 */
	public void setId(String theId) {
		myId = theId;
	}

	/**
	 * @param theUrl
	 *            the url to set
	 */
	public void setUrl(String theUrl) {
		myUrl = theUrl;
	}

}
