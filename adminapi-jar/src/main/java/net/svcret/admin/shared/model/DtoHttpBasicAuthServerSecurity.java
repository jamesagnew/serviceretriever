package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="HttpBasicAuthServerSecurity")
@XmlRootElement(namespace=XmlConstants.DTO_NAMESPACE, name="HttpBasicAuthServerSecurity")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoHttpBasicAuthServerSecurity extends BaseDtoServerSecurity {

	private static final long serialVersionUID = 1L;

	@Override
	public ServerSecurityEnum getType() {
		return ServerSecurityEnum.HTTP_BASIC_AUTH;
	}

}
