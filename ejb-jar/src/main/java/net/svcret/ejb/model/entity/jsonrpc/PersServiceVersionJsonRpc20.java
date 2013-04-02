package net.svcret.ejb.model.entity.jsonrpc;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ca.uhn.sail.proxy.api.ServiceProtocolEnum;
import ca.uhn.sail.proxy.model.entity.BasePersServiceVersion;

@Entity
@DiscriminatorValue("JSONRPC20")
public class PersServiceVersionJsonRpc20 extends BasePersServiceVersion {

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.JSONRPC20;
	}

}
