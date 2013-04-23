package net.svcret.ejb.model.entity.jsonrpc;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.model.entity.BasePersServiceVersion;

@Entity
@DiscriminatorValue("JSONRPC20")
public class PersServiceVersionJsonRpc20 extends BasePersServiceVersion {

	private static final long serialVersionUID = 1L;

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.JSONRPC20;
	}

}
