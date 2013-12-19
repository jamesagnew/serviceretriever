package net.svcret.admin.shared.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceVersionPropertyCapture implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "config_PropertyName", required = true)
	private String myPropertyName;

	@XmlElement(name = "config_XPathExpression")
	private String myXpathExpression;

	public String getPropertyName() {
		return myPropertyName;
	}

	public void setPropertyName(String thePropertyName) {
		myPropertyName = thePropertyName;
	}

	public String getXpathExpression() {
		return myXpathExpression;
	}

	public void setXpathExpression(String theXpathExpression) {
		myXpathExpression = theXpathExpression;
	}

}
