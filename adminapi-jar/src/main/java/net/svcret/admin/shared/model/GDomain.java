package net.svcret.admin.shared.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="Domain")
@XmlAccessorType(XmlAccessType.FIELD)
public class GDomain extends BaseDtoServiceCatalogItem {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="ServiceList")
	private GServiceList myServiceList = new GServiceList();


	public GServiceList getServiceList() {
		return myServiceList;
	}

	@Override
	public void merge(BaseGObject theObject) {
		super.merge(theObject);

		GDomain obj=(GDomain) theObject;
		if (obj.getServiceList() != null) {
			getServiceList().mergeResults(obj.getServiceList());
		}

	}

	public Set<Long> getAllServiceVersionPids() {
		HashSet<Long> retVal = new HashSet<Long>();
		for (GService next : getServiceList()) {
			retVal.addAll(next.getAllServiceVersionPids());
		}
		return retVal;
	}


}
