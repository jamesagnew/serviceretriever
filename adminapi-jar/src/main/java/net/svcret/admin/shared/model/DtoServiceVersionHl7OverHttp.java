package net.svcret.admin.shared.model;


public class DtoServiceVersionHl7OverHttp extends BaseGServiceVersion {

	public static final String DEFAULT_METHOD_NAME_TEMPLATE = "${messageType}^${messageVersion}";
	
	private static final long serialVersionUID = 1L;

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
