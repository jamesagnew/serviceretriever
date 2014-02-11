package net.svcret.core.model.entity.soap;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.core.api.IDao;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersService;

@Entity
@DiscriminatorValue("SOAP11")
public class PersServiceVersionSoap11 extends BasePersServiceVersion {

	private static final long serialVersionUID = 1L;

	static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(PersServiceVersionSoap11.class);

	@Column(name = "WSDL_URL", length = 200)
	private String myWsdlUrl;

	public PersServiceVersionSoap11() {
		super();
	}

	public PersServiceVersionSoap11(long thePid, PersService theService, String theVersionId) {
		setPid(thePid);
		setService(theService);
		setVersionId(theVersionId);
	}

	/**
	 * @return the wsdlUrl
	 */
	public String getWsdlUrl() {
		return myWsdlUrl;
	}

	/**
	 * @param theWsdlUrl
	 *            the wsdlUrl to set
	 */
	public void setWsdlUrl(String theWsdlUrl) {
		myWsdlUrl = theWsdlUrl;
	}

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.SOAP11;
	}

	@Override
	protected void fromDto(BaseDtoServiceVersion theDto, IDao theDao) {
		setWsdlUrl(((DtoServiceVersionSoap11)theDto).getWsdlLocation());
	}

	@Override
	protected BaseDtoServiceVersion createDtoAndPopulateWithTypeSpecificEntries() {
		DtoServiceVersionSoap11 retVal = new DtoServiceVersionSoap11();
		retVal.setWsdlLocation(getWsdlUrl());
		return retVal;
	}




}
