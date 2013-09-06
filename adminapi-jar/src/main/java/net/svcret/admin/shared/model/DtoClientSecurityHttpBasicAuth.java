package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="ClientSecurityBasicAuth")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoClientSecurityHttpBasicAuth extends BaseGClientSecurity {

	private static final long serialVersionUID = 1L;

	@Override
	public ClientSecurityEnum getType() {
		return ClientSecurityEnum.HTTP_BASICAUTH;
	}

}
