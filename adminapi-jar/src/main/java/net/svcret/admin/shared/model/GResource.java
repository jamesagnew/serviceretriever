package net.svcret.admin.shared.model;

import java.io.Serializable;

public class GResource extends BaseDtoObject implements Serializable {

	private static final long serialVersionUID = 1L;

	private String myContentType;
	private String myText;
	private String myUrl;

	public GResource() {
	}
	
	public GResource(String theUrl, String theContentType, String theText) {
		setContentType(theContentType);
		setText(theText);
		setUrl(theUrl);
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return myContentType;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return myText;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return myUrl;
	}

	/**
	 * @param theContentType
	 *            the contentType to set
	 */
	public void setContentType(String theContentType) {
		myContentType = theContentType;
	}

	/**
	 * @param theText
	 *            the text to set
	 */
	public void setText(String theText) {
		myText = theText;
	}

	/**
	 * @param theUrl
	 *            the url to set
	 */
	public void setUrl(String theUrl) {
		myUrl = theUrl;
	}

	
	@Override
	public void merge(BaseDtoObject theObject) {
		super.merge(theObject);
		
		GResource obj = (GResource)theObject;
		
		setContentType(obj.getContentType());
		setText(obj.getText());
		setUncommittedSessionId(obj.getUncommittedSessionId());
		setUrl(obj.getUrl());
	}

	public GServiceVersionResourcePointer asPointer() {
		GServiceVersionResourcePointer retVal = new GServiceVersionResourcePointer();
		retVal.setUrl(myUrl);
		retVal.setType(myContentType);
		retVal.setSize(myText.length());
		return retVal;
	}

	
}
