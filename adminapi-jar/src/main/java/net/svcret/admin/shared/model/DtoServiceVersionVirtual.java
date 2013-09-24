package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;


@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="ServiceVersionVirtual")
@XmlRootElement(namespace=XmlConstants.DTO_NAMESPACE, name="ServiceVersionVirtual")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceVersionVirtual extends BaseGServiceVersion {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="config_TargetServiceVersionPid")
	private long myTargetServiceVersionPid;
	
	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.VIRTUAL;
	}

	public long getTargetServiceVersionPid() {
		return myTargetServiceVersionPid;
	}

	public void setTargetServiceVersionPid(long theTargetServiceVersionPid) {
		myTargetServiceVersionPid = theTargetServiceVersionPid;
	}


}
