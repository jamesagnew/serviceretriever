package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace = XmlConstants.DTO_NAMESPACE, name = "Domain")
@XmlRootElement(namespace = XmlConstants.DTO_NAMESPACE, name = "Domain")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoDomain extends BaseDtoServiceCatalogItem {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "config_Description")
	private String myDescription;

	@XmlElement(name = "config_ServiceList")
	private GServiceList myServiceList = new GServiceList();

	public DtoDomain() {
	}

	public DtoDomain(String theId, String theName) {
		setId(theId);
		setName(theName);
	}

	public Set<Long> getAllServiceVersionPids() {
		HashSet<Long> retVal = new HashSet<Long>();
		for (GService next : getServiceList()) {
			retVal.addAll(next.getAllServiceVersionPids());
		}
		return retVal;
	}

	@Override
	public List<BaseDtoServiceVersion> getAllServiceVersions() {
		ArrayList<BaseDtoServiceVersion> retVal = new ArrayList<BaseDtoServiceVersion>();
		for (GService next : getServiceList()) {
			retVal.addAll(next.getAllServiceVersions());
		}
		return retVal;
	}

	public String getDescription() {
		return myDescription;
	}

	public GServiceList getServiceList() {
		return myServiceList;
	}

	@Override
	public void merge(BaseDtoObject theObject) {
		super.merge(theObject);

		DtoDomain obj = (DtoDomain) theObject;
		if (obj.getServiceList() != null) {
			getServiceList().mergeResults(obj.getServiceList());
		}

	}

	public void setDescription(String theDomainDescription) {
		myDescription = theDomainDescription;
	}

	@Override
	public String toString() {
		return "Domain[" + getPidOrNull() + "/" + getId() + "]";
	}

}
