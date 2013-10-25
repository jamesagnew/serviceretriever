package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="Service")
@XmlRootElement(namespace=XmlConstants.DTO_NAMESPACE, name="Service")
@XmlAccessorType(XmlAccessType.FIELD)
public class GService extends BaseDtoServiceCatalogItem {

	private static final long serialVersionUID = 1L;
	
	@XmlElement(name="config_Active")
	private boolean myActive;
	
	@XmlElement(name="config_VersionList")
	private GServiceVersionList myVersionList = new GServiceVersionList();;
	
	private transient Set<Long> myServiceVersionPids = new HashSet<Long>();

	private String myDescription;

	public GServiceVersionList getVersionList() {
		return myVersionList;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return myActive;
	}

	@Override
	public void merge(BaseDtoObject theObject) {
		super.merge(theObject);

		GService obj = (GService)theObject;
		if (obj.getVersionList() != null) {
			getVersionList().mergeResults(obj.getVersionList());
		}

		myServiceVersionPids.clear();
	}

	/**
	 * @param theActive
	 *            the active to set
	 */
	public void setActive(boolean theActive) {
		myActive = theActive;
	}


	public boolean allVersionPidsInThisServiceAreAmongThesePids(Set<Long> theAffectedSvcVerPids) {
		populateSvcVerPids();
		return myServiceVersionPids.containsAll(theAffectedSvcVerPids);
	}

	private void populateSvcVerPids() {
		if (myServiceVersionPids.isEmpty()) {
			for (BaseDtoServiceVersion next : getVersionList()) {
				myServiceVersionPids.add(next.getPid());
			}
		}
	}

	public boolean anyVersionPidsInThisServiceAreAmongThesePids(Set<Long> theAffectedSvcVerPids) {
		populateSvcVerPids();
		for (long next : myServiceVersionPids) {
			if (theAffectedSvcVerPids.contains(next)) {
				return true;
			}
		}
		return false;
	}

	public Collection<Long> getAllServiceVersionPids() {
		HashSet<Long> retVal = new HashSet<Long>();
		for (BaseDtoServiceVersion next : getVersionList()) {
			retVal.add(next.getPid());
		}
		return retVal;
	}

	@Override
	public List<BaseDtoServiceVersion> getAllServiceVersions() {
		ArrayList<BaseDtoServiceVersion> retVal = new ArrayList<BaseDtoServiceVersion>();
		for (BaseDtoServiceVersion next : getVersionList()) {
			retVal.add(next);
		}
		return retVal;
	}


	public void removeVersionList() {
		myVersionList=null;
	}

	public void setDescription(String theDescription) {
		myDescription=theDescription;
	}

	public String getDescription() {
		return myDescription;
	}

}
