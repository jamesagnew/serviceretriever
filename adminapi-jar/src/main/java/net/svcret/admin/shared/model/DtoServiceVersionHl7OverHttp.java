package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;


@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="ServiceVersionHl7OverHttp")
@XmlRootElement(namespace=XmlConstants.DTO_NAMESPACE, name="ServiceVersionHl7OverHttp")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceVersionHl7OverHttp extends BaseDtoServiceVersion {

	public static final String DEFAULT_METHOD_NAME_TEMPLATE = "${messageType}";
	public static final String FANCY_METHOD_NAME_TEMPLATE = "${messageType}^${messageVersion}";
	
	private static final long serialVersionUID = 1L;

	@XmlElement(name="MethodNameTemplate")
	private String myMethodNameTemplate;

	public String getMethodNameTemplate() {
		return myMethodNameTemplate;
	}

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.HL7OVERHTTP;
	}

	public void setMethodNameTemplate(String theMethodNameTemplate) {
		myMethodNameTemplate=theMethodNameTemplate;
		
	}


}
