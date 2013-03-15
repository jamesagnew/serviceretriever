package ca.uhn.sail.proxy.model.registry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace="urn:sail:proxy:registry", name="WsSecurityUsernameClientAuthentication")
@XmlAccessorType(XmlAccessType.FIELD)
public class WsSecUsernameClientAuthentication extends BaseClientAuthentication {
	
	public WsSecUsernameClientAuthentication() {
		super();
	}
	
	public WsSecUsernameClientAuthentication(String theUsername, String thePassword) {
		super();
		setUsername(theUsername);
		setPassword(thePassword);
	}

}
