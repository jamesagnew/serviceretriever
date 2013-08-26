package net.svcret.ejb.model.entity.forward;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.model.entity.BasePersServiceVersion;

@Entity
@DiscriminatorValue("FORWARDER")
public class PersServiceVersionForwarder extends BasePersServiceVersion {

	private static final long serialVersionUID = 1L;

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.FORWARDER;
	}

}
