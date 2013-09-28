package net.svcret.admin.shared.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class DtoAuthenticationHostList extends BaseDtoList<BaseDtoAuthenticationHost>{

	private static final long serialVersionUID = 1L;

	@XmlElement(name="AuthHost")
	@Override
	public List<BaseDtoAuthenticationHost> getListForJaxb() {
		return super.getListForJaxb();
	}
	
	public BaseDtoAuthenticationHost getAuthHostByPid(long thePid) {
		for (BaseDtoAuthenticationHost next : this) {
			if (next.getPid() == thePid) {
				return next;
			}
		}
		return null;
	}

}
