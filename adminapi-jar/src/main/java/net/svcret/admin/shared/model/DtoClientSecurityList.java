package net.svcret.admin.shared.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class DtoClientSecurityList extends BaseDtoList<BaseDtoClientSecurity> {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="ClientSecurity")
	@Override
	public List<BaseDtoClientSecurity> getListForJaxb() {
		return super.getListForJaxb();
	}

}
