package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import net.svcret.admin.shared.enm.AuthorizationHostTypeEnum;
import net.svcret.admin.shared.util.XmlConstants;

@XmlRootElement(namespace=XmlConstants.DTO_NAMESPACE, name="AuthenticationHostLocalDatabase")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoAuthenticationHostLocalDatabase extends BaseDtoAuthenticationHost {

	private static final long serialVersionUID = 1L;

	@Override
	public void merge(BaseDtoAuthenticationHost theObject) {
		super.merge(theObject);
	}

	@Override
	public AuthorizationHostTypeEnum getType() {
		return AuthorizationHostTypeEnum.LOCAL_DATABASE;
	}

	@Override
	public boolean isSupportsPasswordChange() {
		return true;
	}

	@Override
	public void setSupportsPasswordChange(boolean theSupportsPasswordChange) {
		if (theSupportsPasswordChange == false) {
			throw new IllegalArgumentException("Must be true");
		}
	}

}
