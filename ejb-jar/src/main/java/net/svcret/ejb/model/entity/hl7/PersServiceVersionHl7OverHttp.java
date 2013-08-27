package net.svcret.ejb.model.entity.hl7;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.BasePersServiceVersionSingleMethod;

@Entity
@DiscriminatorValue("HL7OVERHTTP")
public class PersServiceVersionHl7OverHttp extends BasePersServiceVersionSingleMethod {

	private static final long serialVersionUID = 1L;

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.HL7OVERHTTP;
	}

}
