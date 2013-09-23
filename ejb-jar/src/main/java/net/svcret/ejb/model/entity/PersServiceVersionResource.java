package net.svcret.ejb.model.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.svcret.admin.shared.model.GServiceVersionResourcePointer;

import com.google.common.base.Objects;

@Table(name = "PX_SVC_VER_RES", uniqueConstraints = { @UniqueConstraint(columnNames = { "SVC_VERSION_PID", "RES_URL" }) })
@Entity
public class PersServiceVersionResource extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "RES_TYPE", length = 50, nullable = false)
	private String myResourceContentType;

	@Lob()
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "RES_TEXT", nullable = false)
	private String myResourceText;

	@Column(name = "RES_URL", length = 200, nullable = false)
	private String myResourceUrl;

	@ManyToOne()
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID")
	private BasePersServiceVersion myServiceVersion;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof PersServiceVersionResource)) {
			return false;
		}

		return myPid.equals(((PersServiceVersionResource) theObj).getPid());
	}

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the resourceType
	 */
	public String getResourceContentType() {
		return myResourceContentType;
	}

	/**
	 * @return the wsdlText
	 */
	public String getResourceText() {
		return myResourceText;
	}

	/**
	 * @return the resourceUrl
	 */
	public String getResourceUrl() {
		return myResourceUrl;
	}

	/**
	 * @return the serviceVersion
	 */
	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(getPid());
	}

	public void loadAllAssociations() {
		// nothing
	}

	public void merge(PersServiceVersionResource theObj) {
		setResourceContentType(theObj.getResourceContentType());
		setResourceText(theObj.getResourceText());
		setResourceUrl(theObj.getResourceUrl());
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theResourceType
	 *            the resourceType to set
	 */
	public void setResourceContentType(String theResourceType) {
		myResourceContentType = theResourceType;
	}

	/**
	 * @param theResourceText
	 *            the wsdlText to set
	 */
	public void setResourceText(String theResourceText) {
		myResourceText = theResourceText;
	}

	/**
	 * @param theResourceUrl
	 *            the resourceUrl to set
	 */
	public void setResourceUrl(String theResourceUrl) {
		myResourceUrl = theResourceUrl;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceVersion to set
	 */
	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	public GServiceVersionResourcePointer toDao() {
		GServiceVersionResourcePointer retVal = new GServiceVersionResourcePointer();
		retVal.setPid(this.getPid());
		retVal.setSize(this.getResourceText().length());
		retVal.setType(this.getResourceContentType());
		retVal.setUrl(this.getResourceUrl());
		return retVal;
	}

}
