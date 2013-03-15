package ca.uhn.sail.proxy.model.entity;

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

@Table(name = "PX_SVC_VER_RES", uniqueConstraints = { @UniqueConstraint(columnNames = { "SVC_VERSION_PID", "RES_URL" }) })
@Entity
public class PersServiceVersionResource extends BasePersObject {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Lob()
	@Basic(fetch = FetchType.EAGER)
	@Column(name = "RES_TEXT")
	private String myResourceText;

	@Column(name = "RES_TYPE", length = 50)
	private String myResourceType;
	
	@Column(name = "RES_URL", length = 200)
	private String myResourceUrl;
	
	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID")
	private BasePersServiceVersion myServiceVersion;

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the wsdlText
	 */
	public String getResourceText() {
		return myResourceText;
	}

	/**
	 * @return the resourceType
	 */
	public String getResourceType() {
		return myResourceType;
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

	public void loadAllAssociations() {
		// nothing
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theResourceText
	 *            the wsdlText to set
	 */
	public void setResourceText(String theResourceText) {
		myResourceText = theResourceText;
	}

	/**
	 * @param theResourceType the resourceType to set
	 */
	public void setResourceType(String theResourceType) {
		myResourceType = theResourceType;
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

}
