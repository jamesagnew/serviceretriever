package net.svcret.admin.shared.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class DtoMonitorRuleActiveCheckList extends BaseDtoList<DtoMonitorRuleActiveCheck> {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="ActiveCheck")
	@Override
	public List<DtoMonitorRuleActiveCheck> getListForJaxb() {
		return super.getListForJaxb();
	}

}
