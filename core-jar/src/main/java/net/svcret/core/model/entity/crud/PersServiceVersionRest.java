package net.svcret.core.model.entity.crud;

import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoServiceVersionRest;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.core.api.IDao;
import net.svcret.core.model.entity.BasePersServiceVersion;

import org.apache.commons.lang.StringUtils;

@Entity
@DiscriminatorValue("REST")
public class PersServiceVersionRest extends BasePersServiceVersion {

	private static final long serialVersionUID = 1L;

	@Column(name = "CRUD_REQ_CONTENT_TYPES", length = 1000, nullable = true)
	private String myAcceptableRequestContentTypes;

	@Column(name = "CRUD_RESP_CONTENT_TYPES", length = 1000, nullable = true)
	private String myAcceptableResponseContentTypes;

	@Column(name = "CRUD_REWRITE_URLS", nullable = true)
	private Boolean myRewriteUrls;

	@Override
	protected BaseDtoServiceVersion createDtoAndPopulateWithTypeSpecificEntries() {
		DtoServiceVersionRest retVal = new DtoServiceVersionRest();
		retVal.setAcceptableRequestContentTypes(toArray(myAcceptableRequestContentTypes));
		retVal.setAcceptableResponseContentTypes(toArray(myAcceptableResponseContentTypes));
		retVal.setRewriteUrls(Boolean.TRUE.equals(myRewriteUrls));
		return retVal;
	}

	@Override
	public boolean isAllowSubUrls() {
		return true;
	}

	@Override
	protected void fromDto(BaseDtoServiceVersion theDto, IDao theDao) {
		DtoServiceVersionRest dto = (DtoServiceVersionRest) theDto;
		myRewriteUrls = dto.isRewriteUrls();
		myAcceptableRequestContentTypes = fromArray(dto.getAcceptableRequestContentTypes());
		myAcceptableResponseContentTypes = fromArray(dto.getAcceptableResponseContentTypes());
	}

	public String getAcceptableRequestContentTypes() {
		return myAcceptableRequestContentTypes;
	}

	public String getAcceptableResponseContentTypes() {
		return myAcceptableResponseContentTypes;
	}

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.REST;
	}

	public Boolean getRewriteUrls() {
		return myRewriteUrls;
	}

	public void setAcceptableRequestContentTypes(String theAcceptableRequestContentTypes) {
		myAcceptableRequestContentTypes = theAcceptableRequestContentTypes;
	}

	public void setAcceptableResponseContentTypes(String theAcceptableResponseContentTypes) {
		myAcceptableResponseContentTypes = theAcceptableResponseContentTypes;
	}

	public void setRewriteUrls(Boolean theRewriteUrls) {
		myRewriteUrls = theRewriteUrls;
	}

	private static String fromArray(Set<String> theSet) {
		StringBuilder b = new StringBuilder();
		if (theSet != null) {
			for (String string : theSet) {
				if (StringUtils.isBlank(string)) {
					continue;
				}
				if (b.length() > 0) {
					b.append(",");
				}
				b.append(string);
			}
		}
		return b.toString();
	}

	private static Set<String> toArray(String theAcceptableResponseContentTypes) {
		Set<String> retVal = new TreeSet<>();
		if (theAcceptableResponseContentTypes != null) {
			for (String next : theAcceptableResponseContentTypes.split(",")) {
				if (StringUtils.isNotBlank(next)) {
					retVal.add(next);
				}
			}
		}
		return retVal;
	}

}
