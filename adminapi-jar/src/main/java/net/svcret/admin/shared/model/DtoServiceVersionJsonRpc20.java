package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;


@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="ServiceVersionJsonRpc20")
@XmlRootElement(namespace=XmlConstants.DTO_NAMESPACE, name="ServiceVersionJsonRpc20")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceVersionJsonRpc20 extends BaseDtoServiceVersion {

	private static final long serialVersionUID = 1L;

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.JSONRPC20;
	}

}
