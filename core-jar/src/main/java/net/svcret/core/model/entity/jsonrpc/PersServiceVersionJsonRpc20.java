package net.svcret.core.model.entity.jsonrpc;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoServiceVersionJsonRpc20;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.core.model.entity.BasePersServiceVersion;

@Entity
@DiscriminatorValue("JSONRPC20")
public class PersServiceVersionJsonRpc20 extends BasePersServiceVersion {

	private static final long serialVersionUID = 1L;

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.JSONRPC20;
	}

	@Override
	protected BaseDtoServiceVersion createDtoAndPopulateWithTypeSpecificEntries() {
		DtoServiceVersionJsonRpc20 retVal = new DtoServiceVersionJsonRpc20();
		return retVal;
	}

}
