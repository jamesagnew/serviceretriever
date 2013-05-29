package net.svcret.ejb.model.entity.http;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.ServerSecurityEnum;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.jsonrpc.NamedParameterJsonRpcCredentialGrabber;
import net.svcret.ejb.model.entity.jsonrpc.NamedParameterJsonRpcServerAuth;

@Entity
@DiscriminatorValue("HTTP_BASIC")
public class PersHttpBasicServerAuth  extends PersBaseServerAuth<PersHttpBasicServerAuth, PersHttpBasicCredentialGrabber> {

	@Override
	public ServerSecurityEnum getAuthType() {
		return ServerSecurityEnum.HTTP_BASIC_AUTH;
	}

	@Override
	public Class<? extends PersHttpBasicCredentialGrabber> getGrabberClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void merge(PersBaseServerAuth<?, ?> theObj) {
		// TODO Auto-generated method stub
		
	}

}
