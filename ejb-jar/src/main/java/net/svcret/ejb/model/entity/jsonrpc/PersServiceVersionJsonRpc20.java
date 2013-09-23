package net.svcret.ejb.model.entity.jsonrpc;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.DtoServiceVersionJsonRpc20;
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

	@Override
	protected BaseGServiceVersion createDtoAndPopulateWithTypeSpecificEntries() {
		DtoServiceVersionJsonRpc20 retVal = new DtoServiceVersionJsonRpc20();
		return retVal;
	}

}
