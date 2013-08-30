package net.svcret.ejb.model.entity.hl7;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.DtoServiceVersionHl7OverHttp;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.model.entity.BasePersServiceVersionSingleMethod;

import org.apache.commons.lang3.StringUtils;

@Entity
@DiscriminatorValue("HL7OVERHTTP")
public class PersServiceVersionHl7OverHttp extends BasePersServiceVersionSingleMethod {

	private static final long serialVersionUID = 1L;

	@Column(name="HOH_METHOD_NAME_TMPL", length=100, nullable=true)
	private String myMethodNameTemplate;
	
	public String getMethodNameTemplate() {
		if (StringUtils.isBlank(myMethodNameTemplate)) {
			return DtoServiceVersionHl7OverHttp.DEFAULT_METHOD_NAME_TEMPLATE;
		}
		return myMethodNameTemplate;
	}

	public void setMethodNameTemplate(String theMethodNameTemplate) {
		myMethodNameTemplate = theMethodNameTemplate;
	}

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.HL7OVERHTTP;
	}

}
