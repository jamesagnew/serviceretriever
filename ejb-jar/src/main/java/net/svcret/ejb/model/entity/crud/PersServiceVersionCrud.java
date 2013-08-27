package net.svcret.ejb.model.entity.crud;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.model.entity.BasePersServiceVersion;

@Entity
@DiscriminatorValue("CRUD")
public class PersServiceVersionCrud extends BasePersServiceVersion {

	private static final long serialVersionUID = 1L;

	@Column(name="CRUD_REQ_CONTENT_TYPES", length=1000, nullable=true)
	private String myAcceptableRequestContentTypes;

	@Column(name="CRUD_RESP_CONTENT_TYPES", length=1000, nullable=true)
	private String myAcceptableResponseContentTypes;

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.FORWARDER;
	}

	
	
}
