package net.svcret.ejb.model.registry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace="urn:sail:proxy:registry", name="WsSecurityUsernameServerAuthentication")
@XmlAccessorType(XmlAccessType.FIELD)
public class WsSecUsernameServerAuthentication extends BaseServerAuthentication {
	
	public WsSecUsernameServerAuthentication() {
		super();
	}

	public WsSecUsernameServerAuthentication(String theModuleId) {
		super(theModuleId);
	}

}
