package net.svcret.admin.shared.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class DtoServerSecurityList extends BaseDtoList<BaseDtoServerSecurity> {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="ServerSecurity")
	@Override
	public List<BaseDtoServerSecurity> getListForJaxb() {
		return super.getListForJaxb();
	}

}
