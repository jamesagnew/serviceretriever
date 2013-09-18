package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;
import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="MonitorRuleActive")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoMonitorRuleActive extends BaseGMonitorRule {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="config_Checks")
	private DtoMonitorRuleActiveCheckList myCheckList = new DtoMonitorRuleActiveCheckList();

	public DtoMonitorRuleActiveCheckList getCheckList() {
		return myCheckList;
	}

	@Override
	public MonitorRuleTypeEnum getRuleType() {
		return MonitorRuleTypeEnum.ACTIVE;
	}

}
