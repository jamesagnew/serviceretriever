package net.svcret.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "PX_PROXY_URL_BASE")
public class PersConfigProxyUrlBase extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "URL_BASE")
	private String myUrlBase;

	@ManyToOne()
	@JoinColumn(name = "CONFIG_ID", referencedColumnName = "PID")
	private PersConfig myConfig;

	public PersConfigProxyUrlBase() {
		// nothing
	}

	public PersConfigProxyUrlBase(String  theUrlBase) {
		myUrlBase = theUrlBase;
	}

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the urlBase
	 */
	public String getUrlBase() {
		return myUrlBase;
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theUrlBase
	 *            the urlBase to set
	 */
	public void setUrlBase(String theUrlBase) {
		myUrlBase = theUrlBase;
	}

	public void setConfig(PersConfig thePersConfig) {
		myConfig=thePersConfig;
	}

}
